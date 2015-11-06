package devnull.rest.helpers

import java.io.{InputStream, Reader}

import devnull.rest.MIMEType
import devnull.rest.helpers.ContentTypeResolver.withContentTypes
import org.scalatest.{FunSpec, Matchers}
import unfiltered.directives.Result
import unfiltered.directives.Result.{Error, Success}
import unfiltered.request.HttpRequest
import unfiltered.response.{ResponseFunction, UnsupportedMediaType}

class ContentTypeResolverSpec extends FunSpec with Matchers {

  describe("context-type") {
    it("should return with success without charset") {
      val result: Result[ResponseFunction[Any], MIMEType] = withContentTypes(List(MIMEType.Json))
          .apply(new HttpRequestStub() {
            override def headers(name: String): Iterator[String] = name match {
              case "Content-Type" => Iterator("application/json")
              case _ => Iterator.empty
            }
          })
      result should be(Success(MIMEType.Json))
    }

    it("should return with success with charset") {
      val result: Result[ResponseFunction[Any], MIMEType] = withContentTypes(List(MIMEType.Json))
          .apply(new HttpRequestStub() {
            override def headers(name: String): Iterator[String] = name match {
              case "Content-Type" => Iterator("application/json; charset=UTF-8")
              case _ => Iterator.empty
            }
          })
      result should be(Success(MIMEType.Json.copy(params = Map("charset" -> "UTF-8"))))
    }

    it("should return with failure") {
      val result: Result[ResponseFunction[Any], MIMEType] = withContentTypes(List(MIMEType.Json))
          .apply(new HttpRequestStub() {
            override def headers(name: String): Iterator[String] = name match {
              case "Content-Type" => Iterator("does/notexist")
              case _ => Iterator.empty
            }
          })
      result should be(Error(UnsupportedMediaType))
    }
  }

  class HttpRequestStub() extends HttpRequest[Unit](()) {
    override def inputStream: InputStream = ???

    override def headerNames: Iterator[String] = ???

    override def reader: Reader = ???

    override def isSecure: Boolean = ???

    override def uri: String = ???

    override def remoteAddr: String = ???

    override def parameterValues(param: String): Seq[String] = ???

    override def method: String = ???

    override def protocol: String = ???

    override def parameterNames: Iterator[String] = ???

    override def headers(name: String): Iterator[String] = ???
  }

}
