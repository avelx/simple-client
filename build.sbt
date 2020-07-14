name := "simple-client"

version := "0.1"

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.7"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "2.0.1"