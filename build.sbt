import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "nyc-protobuf",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.apache.kafka" % "kafka_2.11" % "1.1.1",
    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.0",
    libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.5",
    libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.5"
  )

// set the main class for 'sbt run'
// mainClass in (Compile, run) := Some("com.example.ConsumerExample")

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

// (optional) If you need scalapb/scalapb.proto or anything from
// google/protobuf/*.proto
//libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"