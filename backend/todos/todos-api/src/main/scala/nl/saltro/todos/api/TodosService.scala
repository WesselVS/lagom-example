package nl.saltro.todos.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

/**
  * The projects service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the TodosService.
  */
trait TodosService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def get(id: String): ServiceCall[NotUsed, String]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def create(): ServiceCall[CreateTodoMessage, Done]

  def version(): ServiceCall[NotUsed, String]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("todos")
      .withCalls(
        pathCall("/api/todos/get/:id", get _),
        pathCall("/api/todos/create/:id", create _),
        pathCall("/api/todos/version", version _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

/**
  * The greeting message class.
  */
case class CreateTodoMessage(name: String, members: List[String])

object CreateTodoMessage {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[CreateTodoMessage] = Json.format[CreateTodoMessage]
}



/**
  * The greeting message class used by the topic stream.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class TodoAdded(name: String)

object TodoAdded {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[TodoAdded] = Json.format[TodoAdded]
}
