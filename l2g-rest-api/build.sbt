name := "lg2-rest-api"

publishArtifact in Test := true


libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % versions.value.jodaTimeVersion,
  "org.joda" % "joda-convert" % versions.value.jodaConvertVersion,
  "org.mindrot" % "jbcrypt" % versions.value.jbcryptVersion,
  "org.reactivemongo" %% "play2-reactivemongo" % versions.value.reactiveMongoVersion,
  "com.typesafe" % "config" % versions.value.configVersion,
  "log4j" % "log4j" % versions.value.log4jVersion,
  "com.typesafe.akka" %% "akka-slf4j" % versions.value.akkaslf4jVersion,
  "com.typesafe.akka" %% "akka-http" % versions.value.akkaHttpVersion,
  "com.github.swagger-akka-http" %% "swagger-akka-http" % versions.value.akkaHttpSwaggerVersion,
  "com.typesafe.play" %% "play-json" % versions.value.playJsonVersion,
  "com.typesafe.play" %% "play-json-joda" % versions.value.playJsonVersion,

  //Runtime dependencies
  "org.slf4j" % "slf4j-api" % versions.value.slf4jApiVersion,
  "org.slf4j" % "slf4j-log4j12" % versions.value.slf4jApiVersion
)


assemblyOutputPath in assembly := new File("repository/l2g-rest-api.jar")
assemblyExcludedJars in assembly := ((fullClasspath in assembly) map {
  _ filter { cp => List(

  ).exists(cp.data.getName.startsWith)
  }
}).value

assembleArtifact in assemblyPackageScala := true
assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case "git-commit-sha.txt" => MergeStrategy.concat
  case _ => MergeStrategy.first
}