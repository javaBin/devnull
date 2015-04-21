package devnull.storage

import doobie.free.connection.ConnectionIO
import doobie.imports._
import doobie.util.analysis.{AlignmentError, Analysis}
import doobie.util.pretty._
import org.scalatest.matchers.{MatchResult, Matcher}

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

trait DoobieAnalysisMatcher {

  def matchDatabaseSchemaTypesQuery[A]()(implicit transactor: Transactor[Task]) =
    new DatabaseTypeMatcher[Query0[A]]()(transactor) {
      override def analysis(a: Query0[A]): ConnectionIO[Analysis] = a.analysis
    }

  def matchDatabaseSchemaTypesUpdate()(implicit transactor: Transactor[Task]) =
    new DatabaseTypeMatcher[Update0]()(transactor) {
      override def analysis(a: Update0): ConnectionIO[Analysis] = a.analysis
    }

  abstract class DatabaseTypeMatcher[A]()(implicit transactor: Transactor[Task]) extends Matcher[A] {

    def analysis(a: A): ConnectionIO[Analysis]

    override def apply(left: A): MatchResult = {
      val (fail, msg) = runAnalysis(analysis(left))
      MatchResult(fail, msg, msg)
    }

    private def runAnalysis(analysis: ConnectionIO[Analysis]): (Boolean, String) = {
      transactor.trans(analysis).attemptRun match {
        case -\/(e) => println(s"Ex: ${e.getMessage} ::: $e"); (false, "The query does match the schema")
        case \/-(a) => {
          val filterNonEmpty: ((String, List[AlignmentError])) => Boolean = {
            case (s, es) => es.nonEmpty
          }
          val columnDesc: List[(String, List[AlignmentError])] = a.columnDescriptions.filter(filterNonEmpty)
          val paramDesc: List[(String, List[AlignmentError])] = a.paramDescriptions.filter(filterNonEmpty)

          if (columnDesc.isEmpty && paramDesc.isEmpty) (true, "The query does not have any errors")
          else (false, s"SQL Compiles and Typechecks\n${toDesc(columnDesc)} \n ${toDesc(paramDesc)}")
        }
      }
    }

    private def toDesc(descList: List[(String, List[AlignmentError])]): String = {
      descList.map { case (s, es) => s"${es.map(formatError).mkString("\n")}" }.mkString("\n")
    }

    private def formatError(e: AlignmentError): String =
      (wrap(80)(e.msg) match {
        case s :: ss => s"x $s " :: ss.map(str => " " + str.trim)
        case Nil => Nil
      }).mkString("\n")
  }

}
