package devnull.rest.dto

case class FaultResponse(source: String, message: String)

object FaultResponse {
  def apply(throwable: Throwable): FaultResponse = {
    new FaultResponse(throwable.getClass.getName, throwable.getMessage)
  }
}
