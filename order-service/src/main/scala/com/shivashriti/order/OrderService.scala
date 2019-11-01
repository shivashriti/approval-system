package com.shivashriti.order

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.util.Timeout
import com.shivashriti.order.persistance._
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent._
import scala.util._


class OrderService private(config: Config)(implicit system: ActorSystem, materializer: Materializer)
  extends ApiV1 with Routes {

  implicit def executor: ExecutionContextExecutor = system.dispatcher
  val logger: LoggingAdapter = Logging(system, getClass)

  override implicit val timeout: Timeout = akka.util.Timeout(5.seconds)

  val orderDaoComponent = new OrderDaoComponent with PostgresDatabaseService {
    override val orderDao: OrderDao = new OrderDao
  }

  orderDaoComponent.initSchema()

  override val commandHandler: ActorRef = system.actorOf(OrderCommandActor.props(orderDaoComponent))

  val bindingFuture: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

  bindingFuture.onComplete {
    case Success(bound) =>
      logger.info(s"Order Server Started: ${bound.localAddress.getHostString}")
    case Failure(e) =>
      logger.error(s"Order Server could not start: ${e.getMessage}")
      system.terminate()
  }
}

object OrderService  {

  def run(config: Config)(implicit system: ActorSystem, materializer: Materializer): OrderService =
    new OrderService(config)
}