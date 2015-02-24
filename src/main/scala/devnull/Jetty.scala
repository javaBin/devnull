package devnull

object Jetty extends App {

  val port = 8080
  val contextPath = "/server"

  private val server = unfiltered.jetty.Server.http(port).context(contextPath) {
    _.plan(Resources())
  }.requestLogging("access.log")

  server.underlying.setSendDateHeader(true)
  server.run( _ => println("Running server at " + port))

}
