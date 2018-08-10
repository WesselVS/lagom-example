package nl.saltro.projects.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import play.api.libs.json.{Format, Json}

object ProjectService  {
  val TOPIC_NAME = "projects"
}
/**
  * The projects service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the ProjectsService.
  */
trait ProjectsService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def get(id: String): ServiceCall[NotUsed, String]

  def version(): ServiceCall[NotUsed, String]

  def getAll(): ServiceCall[NotUsed, List[Project]]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def create(): ServiceCall[CreateProject, Done]

  def projectTopic() : Topic[Project]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("projects")
      .withCalls(
        pathCall("/api/projects/get/:id", get _),
        pathCall("/api/projects/create/:id", create _),
        pathCall("/api/projects/all", getAll _),
        pathCall("/api/projects/version", version _)
      )
      .withTopics(topic(ProjectService.TOPIC_NAME, projectTopic))
      .withAutoAcl(true)
    // @formatter:on
  }
}


/**
  * The greeting message class.
  */
case class CreateProject(name: String, members: List[String])

object CreateProject {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[CreateProject] = Json.format[CreateProject]
}



/**
  * The greeting message class used by the topic stream.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class ProjectAdded(name: String)

object ProjectAdded {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[ProjectAdded] = Json.format[ProjectAdded]
}
