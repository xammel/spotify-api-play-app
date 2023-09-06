package controllers

import models.RepoModel.Repo
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.mvc.Controller
import play.api.mvc.BaseController.Action

class GithubAPI extends Controller {

  // Some dummy data.
  val data = List[Repo](
    Repo("dotty", "Scala", true, 14315),
    Repo("frontend", "JavaScript", true, 392)
  )

  // Typeclass for converting Repo -> JSON
  implicit val writesRepos = new Writes[Repo] {
    def writes(repo:Repo) = Json.obj(
      "name" -> repo.name,
      "language" -> repo.language,
      "is_fork" -> repo.isFork,
      "size" -> repo.size
    )
  }

  // The controller
  def repos(username:String) = Action {

    val repoArray = Json.toJson(data)
    // toJson(data) relies on existence of
    // `Writes[List[Repo]]` type class in scope

    Ok(repoArray)
  }

}
