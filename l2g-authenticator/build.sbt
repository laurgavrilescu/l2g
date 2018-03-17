name := "lg2-authenticator"

publishArtifact in Test := true

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % versions.value.jodaTimeVersion % "provided",
  "org.joda" % "joda-convert" % versions.value.jodaConvertVersion % "provided"
)