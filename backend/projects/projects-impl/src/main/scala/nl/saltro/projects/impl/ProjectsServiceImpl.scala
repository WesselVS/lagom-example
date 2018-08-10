package nl.saltro.projects.impl

import java.util.UUID

import akka.{Done, NotUsed}
import nl.saltro.projects.api
import nl.saltro.projects.api.{CreateProject, Project, ProjectsService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the ProjectsService.
  */
class ProjectsServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, cassandraSession: CassandraSession)(implicit executionContext: ExecutionContext) extends ProjectsService {
 val VERSION = "0.1"

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  override def get(id: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>

    val ref = persistentEntityRegistry.refFor[ProjectEntity](id)
    ref.ask(GetProjectCommand(id))
  }

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  override def create(): ServiceCall[CreateProject, Done] = { proj =>
    val ref = persistentEntityRegistry.refFor[ProjectEntity](UUID.randomUUID().toString)
    ref.ask(NewProjectCommand(proj.name, proj.members))
  }

  override def getAll(): ServiceCall[NotUsed, List[Project]] = ServiceCall { _ =>
    cassandraSession.selectAll("SELECT id, name, members FROM project")
      .map(rs => rs.map{r => Project(r.getString("id"), r.getString("name"), r.getSet("members", classOf[String]).asScala.toList)}.toList)

  }

  override def version(): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful(VERSION)
  }

  override def projectTopic(): Topic[Project] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(ProjectEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(projectEvent: EventStreamElement[ProjectEvent]): Project = {
    projectEvent.event match {
      case msg: NewProjectEvent => Project(msg.id, msg.name, msg.members)
    }
  }

}
