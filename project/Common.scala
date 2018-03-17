import sbt.Keys._
import sbt._


object Common {
  lazy val settings: Seq[Setting[_]] = Seq(
    scalacOptions += "-target:jvm-1.8",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalaVersion := DependencyVersions.scalaVersion,
    publishMavenStyle := false
  )
}