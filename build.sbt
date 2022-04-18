import sbt._
import Keys._

name := "aLys"

version := "0.3"

scalaVersion := "2.13.8"

resolvers += Resolver.JCenterRepository


libraryDependencies += "net.katsstuff" %% "ackcord" % "0.17.1" //For high level API, includes all the other modules
libraryDependencies += "net.katsstuff" %% "ackcord-core" % "0.17.1" //Low level core API
libraryDependencies += "com.github.pureconfig" % "pureconfig_2.13" % "0.14.1"
// https://mvnrepository.com/artifact/org.openpnp/opencv
libraryDependencies += "org.openpnp" % "opencv" % "4.5.1-2"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.8.0-beta4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"
libraryDependencies += "com.google.api-client" % "google-api-client" % "1.31.4"
libraryDependencies += "com.google.oauth-client" % "google-oauth-client-jetty" % "1.31.5"
libraryDependencies += "com.google.apis" % "google-api-services-sheets" % "v4-rev581-1.25.0"


logLevel := Level.Warn
Compile / mainClass := Some("com.github.majestic.alys.App")

assembly / mainClass := Some("com.github.majestic.alys.App")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _        => MergeStrategy.first
}

//enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)

