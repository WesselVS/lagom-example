package nl.saltro.todos.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

/**
* This is an event sourced entity. It has a state, [[TodoState]], which
* stores what the greeting should be (eg, "Hello").
*
* Event sourced entities are interacted with by sending them commands. This
* entity supports two commands, a [[UseGreetingMessage]] command, which is
* used to change the greeting, and a [[Hello]] command, which is a read
* only command which returns a greeting to the name specified by the command.
*
* Commands get translated to events, and it's the events that get persisted by
* the entity. Each event will have an event handler registered for it, and an
* event handler simply applies an event to the current state. This will be done
* when the event is first created, and it will also be done when the entity is
* loaded from the database - each event will be replayed to recreate the state
* of the entity.
*
* This entity defines one event, the [[GreetingMessageChanged]] event,
* which is emitted when a [[UseGreetingMessage]] command is received.
*/
class TodoEntity extends PersistentEntity {

  override type Command = TodosCommand[_]
  override type Event = TodosEvent
  override type State = Option[TodoState]

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: Option[TodoState] = None

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => Actions().onCommand[NewTodoCommand, Done] {

      // Command handler for the UseGreetingMessage command
      case (NewTodoCommand(name), ctx, state) =>
        // In response to this command, we want to first persist it as a
        // GreetingMessageChanged event
        ctx.thenPersist(
          NewTodoEvent(name)
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(Done)
        }

    }.onEvent {

      // Event handler for the GreetingMessageChanged event
      case (NewTodoEvent(name), state) =>
        // We simply update the current state to use the greeting message from
        // the event.
        Some(TodoState(name, LocalDateTime.now().toString))

    }
    case Some(state) => Actions().onReadOnlyCommand[GetTodoCommand, String] {

      // Command handler for the Hello command
      case (GetTodoCommand(name), ctx, state) =>
        // Reply with a message built from the current message, and the name of
        // the person we're meant to say hello to.
        ctx.reply(s"${state.get.name}")

    }
  }
}

/**
  * The current state held by the persistent entity.
  */
case class TodoState(name: String, timestamp: String)

object TodoState {
  /**
    * Format for the hello state.
    *
    * Persisted entities get snapshotted every configured number of events. This
    * means the state gets stored to the database, so that when the entity gets
    * loaded, you don't need to replay all the events, just the ones since the
    * snapshot. Hence, a JSON format needs to be declared so that it can be
    * serialized and deserialized when storing to and from the database.
    */
  implicit val format: Format[TodoState] = Json.format
}

/**
  * This interface defines all the events that the TodosEntity supports.
  */
sealed trait TodosEvent extends AggregateEvent[TodosEvent] {
  def aggregateTag = TodosEvent.Tag
}

object TodosEvent {
  val Tag = AggregateEventTag[TodosEvent]
}

/**
  * An event that represents a change in greeting message.
  */
case class NewTodoEvent(name: String) extends TodosEvent

object NewTodoEvent {

  /**
    * Format for the greeting message changed event.
    *
    * Events get stored and loaded from the database, hence a JSON format
    * needs to be declared so that they can be serialized and deserialized.
    */
  implicit val format: Format[NewTodoEvent] = Json.format
}

/**
  * This interface defines all the commands that the HelloWorld entity supports.
  */
sealed trait TodosCommand[R] extends ReplyType[R]

/**
  * A command to switch the greeting message.
  *
  * It has a reply type of [[Done]], which is sent back to the caller
  * when all the events emitted by this command are successfully persisted.
  */
case class NewTodoCommand(name: String) extends TodosCommand[Done]

object NewTodoCommand {

  /**
    * Format for the use greeting message command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[NewTodoCommand] = Json.format
}

case class GetTodoCommand(name: String) extends TodosCommand[String]

object GetTodoCommand {

  /**
    * Format for the use greeting message command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[GetTodoCommand] = Json.format
}
/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object TodosSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[GetTodoCommand],
    JsonSerializer[NewTodoCommand],
    JsonSerializer[NewTodoEvent],
    JsonSerializer[TodoState]
  )
}
