package devnull.rest.dto

class FaultResponse(val source: String, val message: String)

object FaultResponse {
  def apply(throwable: Throwable): FaultResponse = {
    new FaultResponse(throwable.getClass.getName, throwable.getMessage)
  }

  def apply(source: String, message: String): FaultResponse = {
    new FaultResponse(source, message)
  }

}
