package devnull.rest

import javax.activation.MimeType

case class MIMEType(
    major: String,
    minor: String,
    params: Map[String, String] = Map.empty
) {
  def includes(mt: MIMEType) = {
    (major, minor) match {
      case ("*", "*") => true
      case (maj, "*") => mt.major == maj
      case (maj, min) => mt.major == maj && mt.minor == min
    }
  }
  override def toString =
    "%s/%s%s".format(major, minor, params.map {
      case (a, b) => "; %s=%s".format(a, b)
    }.mkString(""))
}

object MIMEType {

  val All            = MIMEType("*", "*")
  val Json           = MIMEType("application", "json")
  val CollectionJson = MIMEType("application", "vnd.collection+json")

  def apply(mimeType: String): Option[MIMEType] =
    scala.util.control.Exception.allCatch.opt {
      val mime = new MimeType(mimeType)
      import collection.JavaConverters._
      val keys =
        mime.getParameters.getNames.asInstanceOf[java.util.Enumeration[String]].asScala
      val params = keys.foldLeft(Map[String, String]())(
        (a, b) => a.updated(b, mime.getParameters.get(b))
      )
      MIMEType(mime.getPrimaryType, mime.getSubType, params)
    }
}
