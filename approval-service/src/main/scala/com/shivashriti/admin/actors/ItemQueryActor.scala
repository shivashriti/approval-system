package com.shivashriti.admin.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import com.shivashriti.admin.Status.Status
import com.shivashriti.admin.models._
import com.shivashriti.admin.persistance._

class ItemQueryActor(itemDaoComponent: ItemDaoComponent) extends Actor with ActorLogging {
  import ItemQueryActor._

  override def preStart(): Unit = log.info("ItemQueryActor started")
  override def postStop(): Unit = log.info("ItemQueryActor stopped")

  implicit val cd = context.dispatcher

  override def receive: Receive = {
    case QueryAllItems(None, None) => {
      log.info(s"[] :: New QueryAllItems")

      // get all items
      itemDaoComponent
        .itemDao
        .all
        .map(ItemQueryResponse(_))
        .pipeTo(sender())
    }

    case ItemsById(id) => {
      log.info(s"[] :: New ItemsById for id: ${id}")

      // get item by id
      itemDaoComponent
        .itemDao
        .byId(id)
        .map{
          case Some(i) => ItemQueryResponse(Seq(i))
          case None => ItemQueryResponse(Nil)
        }
        .pipeTo(sender())
    }

    case ItemsByStatus(status) => {
      log.info(s"[] :: New ItemsByStatus for status: ${status}")

      // get all item by status
      itemDaoComponent
        .itemDao
        .byStatus(status)
        .map(items => ItemQueryResponse(items))
        .pipeTo(sender())
    }
  }
}

object ItemQueryActor {

  def props(itemDao: ItemDaoComponent) = Props(classOf[ItemQueryActor], itemDao)

  case class QueryAllItems(id: Option[UUID] = None, status: Option[Status] = None)
  case class ItemsById(id: UUID)
  case class ItemsByStatus(status: Status)
  case class ItemQueryResponse(items: Seq[ApprovalItem])
}