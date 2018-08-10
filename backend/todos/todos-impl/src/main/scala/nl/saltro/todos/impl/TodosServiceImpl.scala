package nl.saltro.todos.impl

import java.util.UUID

import akka.stream.scaladsl.{Flow, Sink}
import akka.{Done, NotUsed}
import nl.saltro.todos.api
import nl.saltro.todos.api.{CreateTodoMessage, TodosService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import nl.saltro.projects.api.{Project, ProjectsService}
import nl.saltro.todos.api.CreateTodoMessage
import nl.saltro.todos.impl.{GetTodoCommand, NewTodoCommand, TodoEntity}

import scala.concurrent.Future

/**
  * Implementation of the TodosService.
  */
class TodosServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, projectsService: ProjectsService) extends TodosService {
  val VERSION = "1.0"

  projectsService
      .projectTopic()
    .subscribe // <-- you get back a Subscriber instance
    .atLeastOnce(
    Flow[Project].mapAsync(5){processNewProject}
  )

  def processNewProject(project: Project): Future[Done] = {
    val ref = persistentEntityRegistry.refFor[TodoEntity](UUID.randomUUID().toString)

    ref.ask(NewTodoCommand(project.name))
  }
  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  override def get(id: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>

    val ref = persistentEntityRegistry.refFor[TodoEntity](id)
    ref.ask(GetTodoCommand(id))
  }

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  override def create(): ServiceCall[CreateTodoMessage, Done] = { todo =>
    val ref = persistentEntityRegistry.refFor[TodoEntity](UUID.randomUUID().toString)
    ref.ask(NewTodoCommand(todo.name))
  }

  override def version(): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful(VERSION)
  }
}
