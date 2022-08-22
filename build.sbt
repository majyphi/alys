import sbt._
import Keys._

name := "aLys"

version := "2.0-snapshot"

scalaVersion := "2.13.8"

resolvers += Resolver.JCenterRepository


libraryDependencies += "net.katsstuff" %% "ackcord" % "0.18.1" exclude("com.sedmelluq", "lavaplayer") //For high level API, includes all the other modules
// https://mvnrepository.com/artifact/org.openpnp/opencv
libraryDependencies += "org.openpnp" % "opencv" % "4.5.1-2"
libraryDependencies += "de.vandermeer" % "asciitable" % "0.3.2"

//DB
// https://mvnrepository.com/artifact/net.xdob.h2db/h2
//libraryDependencies += "net.xdob.h2db" % "h2" % "2.0.0"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.3",
  // https://mvnrepository.com/artifact/com.h2database/h2
  "com.h2database" % "h2" % "2.1.214"
)

libraryDependencies ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "2.1.0"
)

//test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test"
libraryDependencies += "com.github.pureconfig" % "pureconfig_2.13" % "0.17.1"
//Logging
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.0"

logLevel := Level.Warn
Compile / mainClass := Some("com.github.majestic.alys.App")

assembly / mainClass := Some("com.github.majestic.alys.App")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

//enablePlugins(JavaAppPackaging)
//enablePlugins(DockerPlugin)

