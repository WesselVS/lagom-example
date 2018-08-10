package nl.saltro.projects.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import scala.collection.JavaConverters._

import scala.concurrent.{ExecutionContext, Future, Promise}

class ProjectEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext) extends ReadSideProcessor[ProjectEvent] {
  private val insertProjectPromise = Promise[PreparedStatement] // initialized in prepare
  private def insertProject: Future[PreparedStatement] = insertProjectPromise.future

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[ProjectEvent] = {
    val builder = readSide.builder[ProjectEvent]("projectevent")
    builder.setGlobalPrepare(() => createTable())
    builder.setPrepare(tag => prepareInsertProject())
    builder.setEventHandler[NewProjectEvent](processProjectCreated)
    builder.build()
  }

  override def aggregateTags: Set[AggregateEventTag[ProjectEvent]] = {
    Set(ProjectEvent.Tag)
  }

  private def createTable(): Future[Done] =
    session.executeCreateTable("CREATE TABLE IF NOT EXISTS project ( " +
      "id text, name text, members set<text>, PRIMARY KEY (id))")

  private def prepareInsertProject(): Future[Done] = {
    val f = session.prepare("INSERT INTO project (id, name, members) VALUES (?, ?, ?)")
    insertProjectPromise.completeWith(f)
    f.map(_ => Done)
  }

  private def processProjectCreated(eventElement: EventStreamElement[NewProjectEvent]): Future[List[BoundStatement]] = {
    insertProject.map { ps =>
      val bindProject = ps.bind()
      bindProject.setString("id", eventElement.event.id)
      bindProject.setString("name", eventElement.event.name)
      bindProject.setSet("members", eventElement.event.members.toSet.asJava)
      List(bindProject)
    }
  }

}