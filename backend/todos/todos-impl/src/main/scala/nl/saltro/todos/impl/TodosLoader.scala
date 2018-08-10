package nl.saltro.todos.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import nl.saltro.todos.api.TodosService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._
import nl.saltro.projects.api.ProjectsService

class TodosLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new TodosApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new TodosApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[TodosService])
}

abstract class TodosApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  lazy val projectsService = serviceClient.implement[ProjectsService]


  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[TodosService](wire[TodosServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = TodosSerializerRegistry

  // Register the todos persistent entity
  persistentEntityRegistry.register(wire[TodoEntity])
}
