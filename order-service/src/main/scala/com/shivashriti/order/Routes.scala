package com.shivashriti.order


import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern._
import com.shivashriti.order.OrderCommandActor._
import com.shivashriti.order.utils.ResponseUtil

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ApiV1 {
  val apiPrefix: Directive[Unit] = pathPrefix("api" / "v1")
}

trait Routes extends ApiV1 {

  def commandHandler: ActorRef

  implicit val timeout: akka.util.Timeout

  val routes: Route =
    apiPrefix {
      pathPrefix("order") {
        pathEndOrSingleSlash {
          (post & entity(as[InputOrder])) { order =>

            val result = (commandHandler ? CreateOrderRequest(order))
              .mapTo[CreateOrderResponse]
              .map { r =>
                r.id match {
                  case Some(id) => Right(id)
                  case None => Left(ErrorResponse("Could not create Order", 500))
                }
              }

            ResponseUtil.respondWith(result, StatusCodes.Created)
          } ~
          {
            get {
              val result = (commandHandler ? QueryAllItems)
                .mapTo[OrderQueryResponse]
                .map { r =>
                  r.orders match {
                    case Nil => Left(ErrorResponse("No Orders found", 404))
                    case list => Right(list)
                  }
                }

              ResponseUtil.respondWith(result)
            }
          }
        } ~ {
          path( JavaUUID ) { id =>
            pathEndOrSingleSlash {
              get {
                val result = (commandHandler ? OrdersById(id))
                  .mapTo[OrderQueryResponse]
                  .map { r =>
                    r.orders match {
                      case Nil => Left(ErrorResponse(s"No Order with Id: $id", 404))
                      case list => Right(list)
                    }
                  }

                ResponseUtil.respondWith(result)
              }
            }
          }
        } ~
          {
          path( JavaUUID / "notifications") { id =>
            pathEndOrSingleSlash {
              (post & entity(as[Note])) { note =>
                (commandHandler ! NotifyRequest(id, note))
                complete(
                  Future.successful(StatusCodes.Accepted, notificationReceived)
                )
              }
            }
          }
        }
      }
    }
}
