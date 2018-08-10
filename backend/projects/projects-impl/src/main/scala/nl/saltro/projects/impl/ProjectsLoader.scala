package nl.saltro.projects.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import nl.saltro.projects.api.ProjectsService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class ProjectsLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ProjectsApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ProjectsApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ProjectsService])
}

abstract class ProjectsApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[ProjectsService](wire[ProjectsServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = ProjectsSerializerRegistry

  // Register the projects persistent entity
  persistentEntityRegistry.register(wire[ProjectEntity])
  readSide.register[ProjectEvent](new ProjectEventProcessor(cassandraSession, cassandraReadSide))

}
