package com.shivashriti.admin.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.pattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shivashriti.admin.models._
import com.shivashriti.admin.utils.ResponseUtil._
import com.shivashriti.admin.actors.ItemCommandActor._

import scala.concurrent.ExecutionContext.Implicits.global

trait CommandRoutes extends ApiV1 {

  def commandHandler: ActorRef

  implicit val timeout: akka.util.Timeout

  val commandRoutes: Route =
    apiPrefix {

      pathPrefix("item"){
        pathEndOrSingleSlash {
          (post & entity(as[ApprovalItem])) { item =>

            val result = (commandHandler ? CreateItemRequest(item))
              .mapTo[CreateItemResponse]
              .map { r =>
                r.item match {
                  case Some(id) => Right(id)
                  case None => Left(ErrorResponse("Could not create Item", 500))
                }
              }
            respondWith(result, StatusCodes.Created)
          }
        } ~
        path(JavaUUID / "status") { id =>
          pathEndOrSingleSlash {

            (post & entity(as[ItemStatus])) { itemStatus =>
              val result = (commandHandler ? StatusChangeRequest(id, itemStatus.status))
                .mapTo[StatusChangeResponse]
                .map { r =>
                  r.value match {
                    case Some(v) => Right(v)
                    case None => Left(ErrorResponse("Could not update status", 500))
                  }
                }
              respondWith(result, StatusCodes.Created)
            }
          }
        }
      }
    }
}
