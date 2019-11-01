package com.shivashriti.admin.utils

import spray.json._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.shivashriti.admin.models.{ErrorResponse, ItemStatus, ServiceResponse}

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
          (StatusCodes.OK, a.toJson.toString())
      }
    }

  def toEntity(note: ItemStatus)(implicit encoder: RootJsonFormat[ItemStatus])= note.toJson
}
