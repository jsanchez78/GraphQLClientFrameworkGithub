package gitHubObject

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import sun.security.krb5.internal.AuthorizationData

import scala.concurrent.{ExecutionContext, Future}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import scala.io.Source.fromInputStream

sealed trait gitHubObject

object gitHubObject {
  //Trait is mandatory when creating a gitHubObject for the user
  sealed trait authCode extends gitHubObject

  val BASE_GHQL_URL = "https://api.github.com/graphql"
  val temp = "{viewer {email login url}}"
  ///Builder hiding complexity but demanding these traits
  type MandatoryInfo = authCode
}

case class Github[I <: gitHubObject](key:String = ""){
  /*  User must build with partial (Auth Code)
  *
  *   Can add customizations here
  *
  *
  * */
  def withAuthCode(key:String):Github[I with gitHubObject.authCode] =
    this.copy(key = key)

  def build(implicit ev: I =:= gitHubObject.MandatoryInfo): Option[GHQLResponse] = {

    val httpUriRequest = new HttpPost(gitHubObject.BASE_GHQL_URL)
    /// gqlReq => function to set any entity based on given (String)
    // Then execute and return json response

      ///setAuthorization in builder pattern
      httpUriRequest.addHeader("Authorization", "Bearer " + key)
      httpUriRequest.addHeader("Accept", "application/json")

    Some(GHQLResponse(httpUriRequest))
  }

  //Build entire object for user with all mandatory info
}
case class GHQLResponse(httpUriRequest:HttpPost){
  /*
  *   Create connection for user to receive queries
  *
  *   setAndGet(str: String) will send the response back to the user when called via a GithubObject
  * */
  def setAndGet(str: String): String = {
    val closeable_connection: CloseableHttpClient = HttpClientBuilder.create.build
    httpUriRequest.setEntity(new StringEntity(str))
    val response = closeable_connection.execute(httpUriRequest)
    val setAndGet = Logger(LoggerFactory.getLogger("logger"))
    //Response successful or fails
    response.getEntity match {
      case null => {
        setAndGet.trace("Sending null data")
        "Response entity is null"
      }
      case x if x != null => {
        setAndGet.trace("Connection built successfully and sending the request!")
        fromInputStream(x.getContent).mkString
      }

    }
  }
  val GithubBuilder: Logger = Logger(LoggerFactory.getLogger("logger"))
  GithubBuilder.trace("Building Github object")
}
