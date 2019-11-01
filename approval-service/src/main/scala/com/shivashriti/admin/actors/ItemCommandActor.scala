package com.shivashriti.admin.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.Materializer
import com.shivashriti.admin.persistance._
import com.shivashriti.admin.models._
import com.shivashriti.admin.Status.Status
import com.shivashriti.admin.utils.ResponseUtil._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ItemCommandActor(itemDaoComponent: ItemDaoComponent)(implicit system: ActorSystem, materializer: Materializer)
  extends Actor with ActorLogging {

  import ItemCommandActor._

  override def preStart(): Unit = log.info("ItemCommandActor started")
  override def postStop(): Unit = log.info("ItemCommandActor stopped")

  implicit val cd = context.dispatcher

  override def receive: Receive = {
    case CreateItemRequest(item) => {
      log.info(s"[${item.id}] :: New ItemCommandRequest for item: $item")

      // insert item
      itemDaoComponent
        .itemDao
        .insert(item)
        .map(i => CreateItemResponse(Some(i)))
        .pipeTo(sender())
    }

    case StatusChangeRequest(itemId, status) => {
      log.info(s"[$itemId] :: New StatusChangeRequest")

      // get item by id
      itemDaoComponent
        .itemDao
        .byId(itemId)
        .flatMap {

          // update status if item found
          case Some(item) =>
            itemDaoComponent
              .itemDao
              .update(itemId, item.copy(status = status))
              .map(_ => StatusChangeResponse(Some("Success")))
              .andThen{

                case Success(v) =>
                  // notify service about status change
                  val entity = toEntity(ItemStatus(status))
                    Http().singleRequest(
                      HttpRequest(
                        method = HttpMethods.POST,
                        uri = item.serviceUrl + itemId + "/notifications",
                      entity = HttpEntity(ContentTypes.`application/json`, entity.toString())
                    )
                  )
                    .map(_=> v)

                case Failure(e) =>
                  log.info(s"[$itemId] failed to change status Error :: ${e.getMessage}")
                  Future.successful(StatusChangeResponse(Some("Failed")))
              }

          case None => Future.successful(StatusChangeResponse(Some(s"No entry found for $itemId")))

        }
        .pipeTo(sender())
    }
  }
}

object ItemCommandActor {

  def props(itemDao: ItemDaoComponent)(implicit system: ActorSystem, materializer: Materializer)  = Props(classOf[ItemCommandActor], itemDao, system, materializer)

  case class CreateItemRequest(item: ApprovalItem)
  case class CreateItemResponse(item: Option[ApprovalItem])

  case class StatusChangeRequest(itemId: UUID, status: Status)
  case class StatusChangeResponse(value: Option[String])
}