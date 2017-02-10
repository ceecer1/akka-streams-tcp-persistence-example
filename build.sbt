name := "akka_programming"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

libraryDependencies ++= {
  val akkaVersion = "2.4.9-RC2"
  val scalaTestVersion = "3.0.0"

  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.github.krasserm" %% "akka-persistence-cassandra" % "0.6",
    "ch.qos.logback"    %  "logback-classic"  % "1.0.13",
    "org.mockito"       % "mockito-all" % "1.10.19" % "test",
    "org.scalatest"     %% "scalatest" % scalaTestVersion   % "test",
    "io.spray"                    %%  "spray-json"     % "1.3.2",
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.6-RC2"

  )
}



