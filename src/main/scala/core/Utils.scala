package core

import java.net.URL
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization
import org.json4s.ext.EnumNameSerializer

class URLSerializer extends CustomSerializer[URL](format => (
  {
    case JString(s) => new URL(s)
  },
  {
    case x: URL => JString(x.toString)
}))


object ResultSerializer {
  val classes = List(classOf[ValidationSuccess], classOf[ValidationFailed], classOf[DownloadFailed])
  val serializer = Serialization.formats(ShortTypeHints(classes)) + 
      new EnumNameSerializer(ErrorType) + new URLSerializer
  
}
