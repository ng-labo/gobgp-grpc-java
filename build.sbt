import sbt.Keys.javacOptions

ThisBuild / organization := "ng-labo"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.15"

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty-shaded" % "1.50.1" % "runtime",
  "io.grpc" % "grpc-protobuf" % "1.50.1",
  "io.grpc" % "grpc-stub" % "1.50.1",
  "javax.annotation" % "javax.annotation-api" % "1.3.2"
)

resolvers ++= Seq(
  "Artima Maven Repository" at "https://repo.artima.com/releases"
)

assembly / mainClass := Some("gobgpapi.example.Client")

val root = (project in file("."))
  .enablePlugins(ProtocPlugin)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "gobgp-grpc-java",
    Compile / javacOptions ++= Seq("-source", "1.8")
  )

