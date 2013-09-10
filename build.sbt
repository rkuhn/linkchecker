name := "linkchecker"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.ning" % "async-http-client" % "1.7.19",
  "ch.qos.logback" % "logback-classic" % "1.0.7")

