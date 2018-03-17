lazy val core = Project("l2g-core", file("l2g-core"))
  .settings(Common.settings)

lazy val persistence = Project("l2g-persistence", file("l2g-persistence"))
  .settings(Common.settings)
  .dependsOn(core)

lazy val authenticator = Project("l2g-authenticator", file("l2g-authenticator"))
  .settings(Common.settings)
  .dependsOn(core)

lazy val http = Project("l2g-rest-api", file("l2g-rest-api"))
  .settings(Common.settings)
  .dependsOn(core, persistence, authenticator)

lazy val root = Project("l2g", file("."))
  .settings(Common.settings)
  .settings(
    name := "l2g",
    publishArtifact := false
  ).aggregate(core, persistence, http)