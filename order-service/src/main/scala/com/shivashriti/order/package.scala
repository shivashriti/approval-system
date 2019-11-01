package com.shivashriti

import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

package object order extends SprayJsonSupport with DefaultJsonProtocol {

  type ServiceResponse[T] = Either[ErrorResponse, T]

  case class ErrorResponse(message: String, code: Int)

  case class InputOrder(customerName: String, amount: Double)

  case class Order(id: UUID, customerName: String, amount: Double, status: String)

  case class ApprovalItem(id: UUID, name: String, status: String, description: String, serviceUrl: String)

  case class Note(status: String)

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit val inputOrderFormat = jsonFormat2(InputOrder)
  implicit val orderFormat = jsonFormat4(Order)
  implicit val approvalItemFormat = jsonFormat5(ApprovalItem)
  implicit val noteFormat = jsonFormat1(Note)
  implicit val errorFormat = jsonFormat2(ErrorResponse)

  def config: Config = ConfigFactory.load()

  val amountLimit: Double = 10000
  val approvalServiceUri = config.getString("approval.service-url")
  val notifyUri = config.getString("approval.notify-url")
  val notificationReceived = "Notification Accepted"
}
