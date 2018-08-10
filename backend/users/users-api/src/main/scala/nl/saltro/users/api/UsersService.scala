package nl.saltro.users.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait UsersService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def get(id: String): ServiceCall[NotUsed, String]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def create(): ServiceCall[CreateUserMessage, Done]

  def version(): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("users")
      .withCalls(
        pathCall("/api/users/get/:id", get _),
        pathCall("/api/users/create/:id", create _),
        pathCall("/api/users/version", version _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

/**
  * The greeting message class.
  */
case class CreateUserMessage(name: String, members: List[String])

object CreateUserMessage {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[CreateUserMessage] = Json.format[CreateUserMessage]
}



/**
  * The greeting message class used by the topic stream.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class UserAdded(name: String)

object UserAdded {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[UserAdded] = Json.format[UserAdded]
}
