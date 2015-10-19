package core.validation

import java.net.URL

import core.validation.ProcessStatus.ProcessStatus

case class ValidationResult(url: URL, processStatus: ProcessStatus, msg: String)

object ProcessStatus extends Enumeration {
  type ProcessStatus = Value
  val Success, Failure = Value
}
