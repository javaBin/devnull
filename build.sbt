organization := "no.java.devnull"

scalaVersion := "2.11.4"

scalacOptions := Seq("-deprecation", "-feature")


val joda = Seq(
  "joda-time" % "joda-time" % "2.2",
  "org.joda" % "joda-convert" % "1.2"
)

lazy val unfilteredVersion = "0.8.3"

val unfiltered = Seq(
  "net.databinder" %% "unfiltered-filter" % unfilteredVersion,
  "net.databinder" %% "unfiltered-directives" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty" % unfilteredVersion,
  "com.jteigen" %% "linx" % "0.2",
  "org.slf4j" % "slf4j-api" % "1.7.7"
)

val database = Seq(
  "org.flywaydb"   %  "flyway-core"               % "3.2.1"
)

libraryDependencies ++= joda ++ unfiltered ++ database ++ Seq(
  "org.ini4j" % "ini4j" % "0.5.2",
  "org.constretto" %% "constretto-scala" % "1.1",
  "net.hamnaberg.rest" %% "scala-json-collection" % "2.3",
  "com.andersen-gott" %% "scravatar" % "1.0.3",
  "com.sksamuel.scrimage" %% "scrimage-core" % "1.4.2",
  "org.jsoup" % "jsoup" % "1.7.2",
  "commons-io" % "commons-io" % "2.3",
  "org.specs2" %% "specs2" % "2.4.2" % "test"
)

pomIncludeRepository := {
  x => false
}

crossPaths := false

aetherPublishBothSettings

appAssemblerSettings

appOutput in App := target.value / "appmgr" / "root"

appmgrSettings

appmgrBuild <<= appmgrBuild.dependsOn(appAssemble)

aetherArtifact <<= (aetherArtifact, appmgrBuild) map { (art, build) =>
  art.attach(build, "appmgr", "zip")
}

