name := """super-simple-stocks"""

version := "1.0-SNAPSHOT"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

startYear := Some(2015)

description := "Super Simple Stocks JPMorgan take home assignement"

scalaVersion := "2.11.6"

developers := List(Developer("arturopala","Artur Opala","opala.artur@gmail.com",url("https://pl.linkedin.com/in/arturopala")))

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.12" % Test,
  "org.scalatest" %% "scalatest" % "2.2.5" % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.2" % Test
)

fork := true

scalariformSettings

mainClass in (Compile, run) := Some("me.arturopala.stockexchange.GBCEApplication")
