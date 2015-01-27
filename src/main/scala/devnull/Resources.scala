package devnull

import javax.servlet.http.HttpServletRequest

import linx.Root
import unfiltered.directives.Directive
import unfiltered.directives.Directives._
import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.filter.request.ContextPath
import unfiltered.request.{GET, HttpRequest}
import unfiltered.response._

class Resources extends Plan {
  type ResponseDirective = Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]

  override def intent: Intent = Intent {
    case Root() => handleRoot()
//    case Feedbacks() => handleRoot()
//    case Feedback(id) => handleRoot()
    case _ => failure(NotFound)
  }

  def handleRoot(): ResponseDirective = {
    val get = for {
      _ <- GET
    } yield {
      JsonContent ~> ResponseString("""{ "ping":"ok"} """)
    }
    get
  }

  val Intent = Mapping[String] { case ContextPath(_, path) => path}

  case class Mapping[X](from: HttpRequest[HttpServletRequest] => X) {
    def apply(intent: PartialFunction[X, Directive[HttpServletRequest, ResponseFunction[Any], ResponseFunction[Any]]]): unfiltered.Cycle.Intent[HttpServletRequest, Any] =
      Directive.Intent {
        case req if intent.isDefinedAt(from(req)) => intent(from(req))
      }
  }

}

object Resources {

  def apply() = new Resources()

}
