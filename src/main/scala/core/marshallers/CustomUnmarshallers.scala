package core.marshallers

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import javax.xml.transform.stream.StreamSource

import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.httpx.unmarshalling.Unmarshaller


object CustomUnmarshallers {
  val unmarshaller =
    Unmarshaller[StreamSource](`text/xml`, `application/xml`, `text/html`, `application/xhtml+xml`) {
      case HttpEntity.NonEmpty(contentType, data) =>


        val source = new InputStreamReader(new ByteArrayInputStream(data.toByteArray),
          contentType.charset.nioCharset)
        new StreamSource(source)
  }
}
