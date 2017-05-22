name := "pgn-2-graph"

version := "0.0"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.anormcypher" %% "anormcypher" % "0.9.1",
  "org.specs2" %% "specs2-core" % "3.8.9" % "test",
  "org.scalaz" %% "scalaz-core" % "7.2.12",
  "org.scalaz" %% "scalaz-effect" % "7.2.12"
)

