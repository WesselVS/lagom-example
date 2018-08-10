package nl.saltro.projects.api

import play.api.libs.json.{Format, Json}

case class Project(id: String, name: String, members: List[String])

object Project {
  implicit val format: Format[Project] = Json.format[Project]

}
