package gitHubObject

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.HttpClientBuilder

import scala.io.Source.fromInputStream
import com.typesafe.config.ConfigFactory
import java.io.File

import Queries.RequestType.{MyContributedToRepos, MyRepos, SpecificUser}
import Queries.{GithubQuery, QueryInfo}

object test extends App {

  val test = new Github[gitHubObject]().withAuthCode(client_data.GetAuthCodeFromConfig()).build

  //println(test.flatMap().setAndGet(client_data.repos))
  val Github = (new Github[gitHubObject]).withAuthCode(client_data.GetAuthCodeFromConfig()).build

  val MyRepoQuery = GithubQuery[QueryInfo]().withQueryType(MyRepos)
  val OptionReposList = Github.flatMap(MyRepoQuery.build)
  val ReposList = OptionReposList.get

  /*
  val MyContributedRepoQuery = GithubQuery[QueryInfo]().withQueryType(MyContributedToRepos)
  val OptionReposList2 = Github.flatMap(MyContributedRepoQuery.build)
  val ReposList2 = OptionReposList2.get

  val SpecificUserQuery = GithubQuery[QueryInfo]().withQueryType(SpecificUser).withSpecificUser("wisabi")
  val OptionReposList3 = Github.flatMap(SpecificUserQuery.build)
  val ReposList3 = OptionReposList3.get
  */

  for (e <- ReposList){
    e.printRepoInfo
  }

}