import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import scala.io.Source.fromInputStream

object Main extends App {

  val BASE_GHQL_URL = "https://api.github.com/graphql"
  val temp="{viewer {email login url}}"
  //implicit val formats = DefaultFormats
  val repos =  "query ObtainRepos($allRepos: Boolean!){ " +
                " viewer {" +
                "    ...AllRepos @include(if: $allRepos) "+
                "  } " +
                "}" +
                " fragment AllRepos on User {  "+
                "    Users_Own_Repos: repositories {  "+
                "      ...repoInfo  "+
                "    }  "+
                "    Contributed_To_Repos: repositoriesContributedTo{ "+
                "      ...repoInfo  "+
                "    }  "+
                " }  "+
                " fragment repoInfo on RepositoryConnection {  " +
                "    ListOfRepos: edges {  " +
                "      Repo: node { "+
                "        Owner_and_Repo: nameWithOwner "+
                "        Created: createdAt "+
                "        Last_Pushed: pushedAt "+
                "        Description: description "+
                "        Disk_Usage : diskUsage "+
                "        forks {  "+
                "          totalCount "+
                "        }  "+
                "        pullRequests {  "+
                "          totalCount "+
                "        }  "+
                "        LanguagesUsed: languages(first: 3){ "+
                "          ListOfLanguages: edges { "+
                "            ProgramingLanguage: node { "+
                "              Language: name "+
                "            } " +
                "          } " +
                "        } " +
                "     } " +
                "   } " +
                " } "


  val client = HttpClientBuilder.create.build
  val httpUriRequest = new HttpPost(BASE_GHQL_URL)
  httpUriRequest.addHeader("Authorization", "Bearer 9664303b5648c5c66d68a5b10880dd490c9ac832")
  httpUriRequest.addHeader("Accept", "application/json")
  val gqlReq = new StringEntity("{" +
                                          "   \"query\":      \"" + repos + "\", " +
                                          "   \"operationName\": \"ObtainRepos\",  " +
                                          "   \"variables\":  { \"allRepos\": true } " +
                                          "}" )
  httpUriRequest.setEntity(gqlReq)

  val response = client.execute(httpUriRequest)
  System.out.println("Response : " + response)
  response.getEntity match {
    case null => System.out.println("Response entity is null")
    case x if x != null => {
      val respJson = fromInputStream(x.getContent).mkString
      print("respJSON: " + respJson)
     /* val respJson = fromInputStream(x.getContent).getLines.mkString
      System.out.println(respJson)
      val viewer = parse(respJson).extract[Data]
      System.out.println(viewer)
      System.out.println(write(viewer))*/
    }
  }
}
