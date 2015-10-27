package core

import java.net.URL


case class ParsingError(line: Integer, column: Integer, publicId: String, message: String,
    errType: ErrorType.Value)

sealed abstract class ProcessingState(url: URL)

case class ValidationSuccess(url: URL) extends ProcessingState(url)

case class ValidationFailed(url: URL, errors: Seq[ParsingError]) extends ProcessingState(url)

case class DownloadFailed(url: URL, error: String) extends ProcessingState(url)

object ErrorType extends Enumeration {
  val Warning, Error, Fatal = Value
}
