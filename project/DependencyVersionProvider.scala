import sbt.{Def, _}

object DependencyVersionProvider extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    lazy val versions: SettingKey[DependencyVersionRegistry] = settingKey[DependencyVersionRegistry]("Bundles dependency versions")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    versions := DependencyVersions
  )
}
