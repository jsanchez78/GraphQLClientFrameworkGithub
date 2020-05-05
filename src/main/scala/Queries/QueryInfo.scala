package Queries

import RepoParser.JSONParser.Repo
import RepoParser.JSONParser
import RequestType.MyRepos
import RequestType.SpecificUser
import RequestType.MyContributedToRepos
import gitHubObject.GHQLResponse

/*
   Design Pattern:  Builder design pattern using a phantom type
                    (Reference: https://pedrorijo.com/blog/typesafe-builder/)
              Use:  Use to build queries to send to github's graphQL endpoint. This builder generates a Query (case class) with
                    a fully constructed query. It then returns a partially call function to be called later, which will be invoked
                    on a GHQLResponse object.
            Notes: • Only a QueryRequest is strictly necessary.
                   • Repositories filters may be added during the query building portion of the code with the function withFilter.
                   • If the query type is SpecificUser and no user login was provided for the query, then the default query type
                     will be used (MyRepos).
 */

sealed trait QueryInfo

object QueryInfo {
  sealed trait QueryType extends QueryInfo
  sealed trait Request extends QueryInfo
  type EssentialInfo = QueryType
}

case class GithubQuery [I <: QueryInfo ](queryType: RequestType.QueryRequest = MyRepos, userLogin: String = "",filterFunctions: List[Repo => Boolean] = List() ){

  def withQueryType (queryType: RequestType.QueryRequest) :  GithubQuery[I with QueryInfo.QueryType] = // Needed
    this.copy(queryType = queryType)


  def withFilter(f: Repo => Boolean): GithubQuery[I] = // Not strictly needed, No trait to attach
    this.copy(filterFunctions = filterFunctions ++ List(f) )


  def withSpecificUser(userLogin: String): GithubQuery[I] = // Not strictly needed, No trait to attach
    this.copy(userLogin = userLogin)


  // The function to return to flatMap(...), which will be invoked on a GHQLResponse object
  def GenerateList(Q: Query)(gitHub: GHQLResponse): Option[Seq[Repo]] = {
    //TODO: userLogin may not be a valid github user, client dependent. Read github error message to confirm
    val json = gitHub.setAndGet(Q.query)
    val RepoList = if (Q.queryType == SpecificUser) JSONParser.getSpecificUserRepo(json) else JSONParser.getUserOwnRepo(json)

    // Applying all filters (in filterFunction) to the Repos received from JSONParser
    var currentSate = RepoList
    for (function <- Q.filterFunctions) {
      currentSate = currentSate.filter(function)
    }
    Some(currentSate)
  }

  def build(implicit ev : I =:= QueryInfo.EssentialInfo) : GHQLResponse => Option[Seq[Repo]] = {
    queryType match {
      case SpecificUser if userLogin.equals("") => GenerateList(Query(MyRepos.toString, filterFunctions, MyRepos))
      case SpecificUser if !userLogin.equals("") => GenerateList(Query(queryType.toString + userLogin + "\"} }", filterFunctions, SpecificUser))
      case MyRepos =>  GenerateList(Query(queryType.toString, filterFunctions, MyRepos))
      case MyContributedToRepos =>  GenerateList(Query(queryType.toString, filterFunctions, MyContributedToRepos))
    }
  }
}

// Contains actual query for github's GraphQL endpoint and filter to apply once obtaining Repos
case class Query(query: String = "", filterFunctions: List[Repo => Boolean] = List(), queryType: RequestType.QueryRequest)
