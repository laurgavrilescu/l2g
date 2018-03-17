object DependencyVersions extends DependencyVersionRegistry

sealed trait DependencyVersionRegistry {
  lazy val scalaVersion = "2.11.11"

  lazy val log4jVersion = "1.2.17"
  lazy val slf4jApiVersion = "1.7.25"

  lazy val configVersion = "1.3.3"
  lazy val reactiveMongoVersion = "0.12.7-play26"

  lazy val jodaConvertVersion = "1.8.1"
  lazy val jodaTimeVersion = "2.9.4"

  lazy val jbcryptVersion = "0.4"
  lazy val playJsonVersion = "2.6.8"

  lazy val akkaHttpVersion = "10.0.11"
  lazy val akkaslf4jVersion = "2.5.11"
  lazy val akkaHttpSwaggerVersion = "0.14.0"
}