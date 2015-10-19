package core.marshallers

import java.io.{ByteArrayInputStream, InputStreamReader}
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.httpx.unmarshalling.Unmarshaller

import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}

object CustomUnmarshallers extends CustomUnmarshallers

trait CustomUnmarshallers {

  implicit val GXMLUnmarshaller =
    Unmarshaller[NodeSeq](`text/xml`, `application/xml`, `text/html`, `application/xhtml+xml`) {
    case HttpEntity.NonEmpty(contentType, data) =>
      val parserFactory = SAXParserFactory.newInstance()
      parserFactory.setValidating(true)
      parserFactory.setNamespaceAware(true)

      val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      parserFactory.setSchema(schemaFactory.newSchema(new StreamSource(getClass.getResourceAsStream("/rss/RSS20.xsd"))))

      val parser = parserFactory.newSAXParser()
      val source = new InputStreamReader(new ByteArrayInputStream(data.toByteArray), contentType.charset.nioCharset)
      val validation = Try(parser.getSchema.newValidator().validate(new StreamSource(source)))

      validation match {
        case Success(_) =>
          val xmlLoader = XML.withSAXParser(parser)
          xmlLoader.load(source)
        case Failure(validationError) =>
          throw validationError
      }
    case HttpEntity.Empty => NodeSeq.Empty
  }

}
