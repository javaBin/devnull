import sbt.Keys.{parallelExecution, testOptions}
import sbt.{Def, _}

/*
Arguments: http://www.scalatest.org/user_guide/using_the_runner
-l   exclude tag
-n   include tag
-w   package with sub packages
*/
object TestSettings {

  lazy val AllTests = config("all") extend Test
  lazy val DbTests = config("db") extend Test

  private def testArg(key: String, value: String) = Tests.Argument(TestFrameworks.ScalaTest, key, value)

  val Config = Seq(AllTests, DbTests)

  val Settings: Seq[Def.Setting[_]] =
    inConfig(AllTests)(Defaults.testTasks) ++
    inConfig(DbTests)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(testArg("-l", "devnull.tag.db"), testArg("-l", "devnull.tag.slow")),
      testOptions in DbTests := Seq(testArg("-n", "devnull.tag.db")),
      testOptions in AllTests := Seq(),
      parallelExecution in DbTests := false,
      parallelExecution in AllTests := false
    )
}
