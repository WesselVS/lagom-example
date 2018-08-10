package nl.saltro.users.impl

import java.util.UUID

import akka.{Done, NotUsed}
import nl.saltro.users.api
import nl.saltro.users.api.UsersService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import nl.saltro.users.api.{CreateUserMessage, UsersService}
import nl.saltro.users.impl.{GetUserCommand, NewUserCommand, UserEntity}

import scala.concurrent.Future

/**
  * Implementation of the UsersService.
  */
class UsersServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends UsersService {
  val VERSION = "1.0"
  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  override def get(id: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>

    val ref = persistentEntityRegistry.refFor[UserEntity](id)
    ref.ask(GetUserCommand(id))
  }

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  override def create(): ServiceCall[CreateUserMessage, Done] = { user =>
    val ref = persistentEntityRegistry.refFor[UserEntity](UUID.randomUUID().toString)
    ref.ask(NewUserCommand(user.name))
  }

  override def version(): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful(VERSION)
  }
}
