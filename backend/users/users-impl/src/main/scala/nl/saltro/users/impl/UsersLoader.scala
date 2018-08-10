package nl.saltro.users.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import nl.saltro.users.api.UsersService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class UsersLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new UsersApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new UsersApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[UsersService])
}

abstract class UsersApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[UsersService](wire[UsersServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = UsersSerializerRegistry

  // Register the users persistent entity
  persistentEntityRegistry.register(wire[UserEntity])
}
