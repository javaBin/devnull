val commonSettings = Seq(
  organization := "no.java.devnull",
  scalaVersion := "2.11.6",
  scalacOptions := Seq("-deprecation", "-feature")
)

lazy val AllTests = config("all") extend Test
lazy val DbTests = config("db") extend Test
def testArg(key: String, value: String ) = Tests.Argument(TestFrameworks.ScalaTest, key, value)

lazy val root = (project in file(".")).
  configs(AllTests, DbTests).
  settings(commonSettings: _*).
  settings(inConfig(AllTests)(Defaults.testTasks): _*).
  settings(inConfig(DbTests)(Defaults.testTasks): _*).
  settings(
    testOptions in Test     := Seq(testArg("-l", "devnull.tag.db"), testArg( "-l", "devnull.tag.slow")),
    testOptions in DbTests  := Seq(testArg("-n", "devnull.tag.db")),
    testOptions in AllTests := Seq()
    /*
    Arguments: http://www.scalatest.org/user_guide/using_the_runner
    -l   exclude tag
    -n   include tag
    -w   package with sub packages
    */
  )

 resolvers ++= Seq(
  "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

val joda = Seq(
  "joda-time" % "joda-time" % "2.2",
  "org.joda" % "joda-convert" % "1.2"
)

lazy val unfilteredVersion = "0.8.3"

val unfiltered = Seq(
  "net.databinder" %% "unfiltered-filter"     % unfilteredVersion,
  "net.databinder" %% "unfiltered-directives" % unfilteredVersion,
  "net.databinder" %% "unfiltered-jetty"      % unfilteredVersion,
  "com.jteigen"    %% "linx"                  % "0.2",
  "org.slf4j"      %  "slf4j-api"             % "1.7.7",
  "org.slf4j"      %  "slf4j-simple"          % "1.7.7"
)

lazy val doobieVersion = "0.2.1"

val database = Seq(
  "org.flywaydb"   %  "flyway-core"               % "3.2.1",
  "org.tpolecat"   %% "doobie-core"               % doobieVersion          withSources(),
  "org.tpolecat"   %% "doobie-contrib-postgresql" % doobieVersion          withSources()
)

libraryDependencies ++= joda ++ unfiltered ++ database ++ Seq(
  "org.ini4j"              %  "ini4j"                 % "0.5.2",
  "org.constretto"         %% "constretto-scala"      % "1.1",
  "net.hamnaberg.rest"     %% "scala-json-collection" % "2.3",
  "commons-io"             %  "commons-io"            % "2.3",
  "org.scalatest"          %  "scalatest_2.11"        % "2.2.4"   % "test"
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

Revolver.settings
