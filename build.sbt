name := "linkchecker"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3-M1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3-M1",
  "com.typesafe.akka" %% "akka-cluster" % "2.3-M1",
  "org.scalatest" %% "scalatest" % "1.9.2-SNAP2" % "test",
  "com.ning" % "async-http-client" % "1.7.19",
  "ch.qos.logback" % "logback-classic" % "1.0.7")

retrieveManaged := true

EclipseKeys.relativizeLibs := true

