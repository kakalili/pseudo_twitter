import play.Project._

name := "pseudo_twitter"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
  )     

playScalaSettings
