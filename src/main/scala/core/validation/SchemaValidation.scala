package core.validation

import javax.xml.XMLConstants
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Schema
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException
import javax.xml.validation.Validator
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import core.ParsingError
import core.ErrorType

object ExceptionMessageExtractor {
  def extract(message: String): String = {
    val invalidElementPattern = """.*Invalid content was found starting with element '(\w+:\w+|\w+)?'.*""".r

    message match {
      case invalidElementPattern(group) => {
        s"Invalid content was found starting with element '$group'"
      }
      case _ => {
        message
      }
    }
  }
}

class SeqErrorHandler extends ErrorHandler {
  val formatter = ExceptionMessageExtractor
  val errors: collection.mutable.Buffer[ParsingError] = collection.mutable.Buffer[ParsingError]()

  def hasErrors: Boolean = errors.nonEmpty

  def proccessError(ex: SAXParseException, errType: ErrorType.Value): Unit = {
    val message = ExceptionMessageExtractor.extract(ex.getMessage)
    errors += ParsingError(ex.getLineNumber, ex.getColumnNumber, ex.getPublicId,
      message, errType)
  }

  def warning(ex: SAXParseException): Unit = {
    proccessError(ex, ErrorType.Warning)
  }

  def error(ex: SAXParseException): Unit = {
    proccessError(ex, ErrorType.Error)
  }

  def fatalError(ex: SAXParseException): Unit = {
    proccessError(ex, ErrorType.Fatal)
  }
}

trait SchemaValidator {
  implicit val schema: Schema

  def validate(xml: StreamSource): Option[Seq[ParsingError]] = {
    val validator = schema.newValidator
    val errorHandler = new SeqErrorHandler()
    validator.setErrorHandler(errorHandler)
    val validation = validator.validate(xml)

    if (errorHandler.hasErrors) Some(errorHandler.errors) else None
  }
}

object GXMLSchemaValidator extends SchemaValidator {
  implicit val schema: Schema = {
    val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    val chaordicSchemaSource: Source = new StreamSource(getClass.getResourceAsStream("/rss/chaordic.xsd"))
    val rssSchemaSource: Source = new StreamSource(getClass.getResourceAsStream("/rss/RSS20.xsd"))
    schemaFactory.newSchema(Array(chaordicSchemaSource, rssSchemaSource))
  }
}