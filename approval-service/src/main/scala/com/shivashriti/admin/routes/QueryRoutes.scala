package com.shivashriti.admin.routes

import akka.actor.ActorRef
import akka.pattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shivashriti.admin.actors.ItemQueryActor._
import com.shivashriti.admin.Status
import com.shivashriti.admin.models._
import com.shivashriti.admin.utils.ResponseUtil._
import scala.concurrent.ExecutionContext.Implicits.global

trait QueryRoutes extends ApiV1 {

  implicit val timeout: akka.util.Timeout

  def queryHandler: ActorRef

  val queryRoutes: Route =
    apiPrefix {

      pathPrefix("item"){

        pathPrefix(JavaUUID){ id =>

          pathPrefix("status"){

            get {
              val result = (queryHandler ? ItemsById(id))
                .mapTo[ItemQueryResponse]
                .map { r =>
                  r.items match {
                    case Nil => Left(ErrorResponse(s"No Items found with Id: $id", 404))
                    case list => Right(list.map(_.status))
                  }
                }

              respondWith(result)
            }
          } ~
            get {

              val result = (queryHandler ? ItemsById(id))
                .mapTo[ItemQueryResponse]
                .map { r =>
                  r.items match {
                    case Nil => Left(ErrorResponse(s"No Items with Id: $id", 404))
                    case list => Right(list)
                  }
                }

              respondWith(result)
            }

        } ~
          get {

            parameters('status.as[String]){status =>

              val result = (queryHandler ? ItemsByStatus(Status.withName(status)))
                .mapTo[ItemQueryResponse]
                .map { r =>
                  r.items match {
                    case Nil => Left(ErrorResponse(s"No Items found with Status: $status", 404))
                    case list => Right(list)
                  }
                }

              respondWith(result)
            }
          } ~
          get {

            val result = (queryHandler ? QueryAllItems())
              .mapTo[ItemQueryResponse]
              .map { r =>
                r.items match {
                  case Nil => Left(ErrorResponse("No Items found", 404))
                  case list => Right(list)
                }
              }

            respondWith(result)
          }
      }
    }
}
