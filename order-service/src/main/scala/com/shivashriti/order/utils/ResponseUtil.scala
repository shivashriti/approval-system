package com.shivashriti.order.utils

import spray.json._
import akka.http.scaladsl.model.{StatusCodes, StatusCode}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.shivashriti.order.{ApprovalItem, ErrorResponse, ServiceResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ResponseUtil {
  def respondWith[A](response: Future[ServiceResponse[A]], successCode: StatusCode = StatusCodes.OK)
                    (implicit ee: JsonWriter[ErrorResponse], rr: JsonWriter[A]): StandardRoute =
    complete {
      response map {
        case Left(er) =>
          (StatusCodes.custom(er.code, ""), er.toJson.toString())
        case Right(a) =>
          (successCode, a.toJson.toString())
      }
    }


  def toEntity(item: ApprovalItem)(implicit encoder: RootJsonFormat[ApprovalItem])= item.toJson
}
