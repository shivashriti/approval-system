package com.shivashriti.order

import java.util.UUID

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.Materializer
import com.shivashriti.order.utils.ResponseUtil._
import com.shivashriti.order.persistance._

import scala.concurrent.Future
import scala.util.{Success, Failure}

class OrderCommandActor(orderDaoComponent: OrderDaoComponent)(implicit system: ActorSystem, materializer: Materializer) extends Actor with ActorLogging {

  import OrderCommandActor._

  override def preStart(): Unit = log.info("OrderCommandActor started")
  override def postStop(): Unit = log.info("OrderCommandActor stopped")

  implicit val cd = context.dispatcher

  override def receive: Receive = {
    case CreateOrderRequest(ipOrder) => {
      val order = Order(UUID.randomUUID(), ipOrder.customerName, ipOrder.amount, "Created")
      log.info(s"[${order.id}] :: New CreateOrderRequest for order: $ipOrder")

      val result: Future[Option[Order]] =
        // if the order amount exceeds max allowed limit
        if(order.amount > amountLimit) {
          val approvalOrder = order.copy(status = "Pending for Approval")

          // insert order
          orderDaoComponent
            .orderDao
            .insert(approvalOrder)
            .map(Some(_))
            .andThen {

              case Success(_) =>
                // request to add item for approval
                Http().singleRequest(
                  HttpRequest(
                    method = HttpMethods.POST,
                    uri = approvalServiceUri,
                    entity = HttpEntity(ContentTypes.`application/json`,
                      toEntity(
                        ApprovalItem(
                          order.id,
                          order.customerName,
                          "Pending",
                          "Order amount limit exceeded",
                          notifyUri)
                      ).toString())
                  )
                )
                  .map(_ => Some(approvalOrder))

              case Failure(_) =>
                Future.successful(None)
            }
        }
      else {
          // insert order
        orderDaoComponent
          .orderDao
          .insert(order)
          .map(v => Some(v))
      }
      result
        .map(v => CreateOrderResponse(v))
        .pipeTo(sender())
    }

    case NotifyRequest(id, note) => {
      log.info(s"[${id}] :: New NotifyRequest with note: $note")

      // get order by id
      orderDaoComponent
        .orderDao
        .byId(id)
        .map {
          // update status when order found
          case Some(item) =>
            orderDaoComponent
              .orderDao
              .update(id, item.copy(status = note.status))
          case None => Future.successful(0)
        }
        .pipeTo(sender())
    }

    case OrdersById(id) => {
      log.info(s"[${id}] :: New OrdersById Request")

      // get order by id
      orderDaoComponent
        .orderDao
        .byId(id)
        .map{
          case Some(i) => OrderQueryResponse(Seq(i))
          case None => OrderQueryResponse(Nil)
        }
        .pipeTo(sender())
    }

      // get all orders
    case QueryAllItems => {
      log.info(s"[] :: New QueryAllItems")

      orderDaoComponent
        .orderDao
        .all
        .map(OrderQueryResponse(_))
        .pipeTo(sender())
    }
  }
}

object OrderCommandActor {

  def props(orderDaoComponent: OrderDaoComponent)(implicit system: ActorSystem, materializer: Materializer) = Props(classOf[OrderCommandActor], orderDaoComponent, system, materializer)

  case class CreateOrderRequest(order: InputOrder)
  case class CreateOrderResponse(id: Option[Order])
  case class NotifyRequest(id: UUID, note: Note)
  case class OrdersById(id: UUID)
  case class OrderQueryResponse(orders: Seq[Order])
  case object QueryAllItems
}