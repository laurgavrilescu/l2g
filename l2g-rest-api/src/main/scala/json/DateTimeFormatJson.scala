package json

import java.util.concurrent.TimeUnit

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import play.api.libs.json._

import scala.concurrent.duration.FiniteDuration

object DateTimeFormatJson {
  implicit object FiniteDurationFormat extends Format[FiniteDuration] {
    override def reads(json: JsValue): JsResult[FiniteDuration] = json match {
      case JsNumber(millis) => JsSuccess(new FiniteDuration(millis.toLongExact, TimeUnit.MILLISECONDS))
      case _ => JsError.apply("Expected JsNumber.")
    }

    override def writes(o: FiniteDuration): JsValue = {
      JsNumber(o.toMillis)
    }
  }

  implicit object DateTimeFormat extends Format[DateTime] {

    val formatter: DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis

    def writes(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsString(s) => try {
        JsSuccess(formatter.parseDateTime(s))
      } catch {
        case t: Throwable => JsError.merge(error(s), JsError(t.getMessage))
      }
      case _ =>
        error(json.toString())
    }

    private def error(v: Any): JsError = {
      val example = formatter.print(0)
      JsError(f"[DateTimeFormat] '$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }
}
