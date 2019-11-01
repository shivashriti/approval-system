package com.shivashriti.admin

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import com.shivashriti.admin.Status.Status

object Status extends Enumeration {
  type Status = Value
  val Pending = Value("Pending")
  val Approved = Value("Approved")
  val Rejected = Value("Rejected")
}

package object models extends SprayJsonSupport with DefaultJsonProtocol {

  type ServiceResponse[T] = Either[ErrorResponse, T]

  case class ErrorResponse(message: String, code: Int)

  case class ItemStatus(status: Status)

  case class ApprovalItem(id: UUID, name: String, status : Status, description: String, serviceUrl: String)

  class EnumerationFormat[A](enum: Enumeration) extends RootJsonFormat[A] {
    def write(obj: A): JsValue = JsString(obj.toString)

    def read(json: JsValue): A = json match {
      case JsString(str) => enum.withName(str).asInstanceOf[A]
      case x => throw new RuntimeException(s"unknown enumeration value: $x")
    }
  }

  implicit object statusFormat extends EnumerationFormat[Status.Value](Status)

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _  => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit val approvalItem = jsonFormat5(ApprovalItem)
  implicit val itemStatus = jsonFormat1(ItemStatus)
  implicit val errorFormat = jsonFormat2(ErrorResponse)
}

