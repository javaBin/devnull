import Dependencies._
import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoPackage

inThisBuild(Seq(
  organization := "no.java.devnull",
  scalaVersion := "2.12.6",
  scalacOptions := Seq("-deprecation", "-feature"),
  crossPaths := false,
))

lazy val devnull = (project in file(".")).
  configs(TestSettings.Config : _*).
  settings(TestSettings.Settings: _*).
  settings(
    name := "devnull",
    resolvers ++= Dependencies.ProjectResolvers,
    libraryDependencies ++= joda ++ unfiltered ++ database ++ logging ++
      Seq(caffine, scalaTest)
  ).
  settings(
    assemblyJarName := "devnull.jar",
    assemblyMergeStrategy in assembly := {
      case "META-INF/io.netty.versions.properties" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    buildInfoPackage := "devnull"
  ).
  enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](
  scalaVersion,
  BuildInfoKey.action("version") { (version in ThisBuild ).value },
  BuildInfoKey.action("buildTime") { System.currentTimeMillis },
  BuildInfoKey.action("branch"){ Git.branch },
  BuildInfoKey.action("sha"){ Git.sha }
)
