import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.rwtodd",
      scalaVersion := "2.12.6",
      version      := "1.0"
    )),
    name := "asciipic",
    libraryDependencies += argparse
  )
