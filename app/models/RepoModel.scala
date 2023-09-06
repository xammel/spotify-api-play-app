package models

object RepoModel {
  case class Repo(val name: String, val language: String, val isFork: Boolean, val size: Long)
}
