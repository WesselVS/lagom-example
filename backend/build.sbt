import java.nio.file.Files
import java.nio.file.StandardCopyOption

import sbt.Resolver.bintrayRepo
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.packager.docker._

organization in ThisBuild := "nl.saltro"

lagomCassandraPort in ThisBuild := 9042

lagomServicesPortRange in ThisBuild := PortRange(40000, 45000)

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val buildVersion = sys.props.getOrElse("buildVersion", "1.0.0-SNAPSHOT")

version in ThisBuild := buildVersion

val dockerSettings = Seq(
  dockerRepository := sys.props.get("dockerRepository"),
  memory := 512 * 1024 * 1024,
  cpu := 0.25
)

lazy val `todos-api` = (project in file("todos/todos-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  ).dependsOn(`projects-api`)

lazy val `todos-impl` = (project in file("todos/todos-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`todos-api`)

lazy val `users-api` = (project in file("users/users-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `users-impl` = (project in file("users/users-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`users-api`)
  
lazy val `projects-api` = (project in file("projects/projects-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `projects-impl` = (project in file("projects/projects-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`projects-api`)


lazy val root = Project("saltro-example", file("."))
	.enablePlugins(LagomScala)
	.aggregate(`users-api`, `users-impl`, `projects-api`, `projects-impl`, `todos-api`, `todos-impl`)
	.dependsOn(`users-api`, `users-impl`, `projects-api`, `projects-impl`, `todos-api`, `todos-impl`)

// do not delete database files on start
lagomCassandraCleanOnStart in ThisBuild := false

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := true

licenses in ThisBuild := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
