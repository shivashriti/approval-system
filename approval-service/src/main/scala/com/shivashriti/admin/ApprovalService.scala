package com.shivashriti.admin

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.util.Timeout
import com.shivashriti.admin.actors.{ItemCommandActor, ItemQueryActor}
import com.shivashriti.admin.persistance._
import com.shivashriti.admin.routes._
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class ApprovalService private(config: Config)(implicit system: ActorSystem, materializer: Materializer)
  extends CommandRoutes with QueryRoutes{

  implicit def executor: ExecutionContextExecutor = system.dispatcher
  val logger: LoggingAdapter = Logging(system, getClass)

  override implicit val timeout: Timeout = akka.util.Timeout(10.seconds)

  val itemDaoComponent = new ItemDaoComponent with PostgresDatabaseService {
    override val itemDao: ItemDao = new ItemDao
  }

  itemDaoComponent.initSchema()

  override val commandHandler: ActorRef = system.actorOf(ItemCommandActor.props(itemDaoComponent))
  override val queryHandler: ActorRef = system.actorOf(ItemQueryActor.props(itemDaoComponent))

  val routes = commandRoutes ~ queryRoutes

  val bindingFuture: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

  bindingFuture.onComplete {
    case Success(bound) =>
      logger.info(s"Approval Server Started: ${bound.localAddress.getHostString}")
    case Failure(e) =>
      logger.error(s"Approval Server could not start: ${e.getMessage}")
      system.terminate()
  }
}

object ApprovalService  {

  def run(config: Config)(implicit system: ActorSystem, materializer: Materializer): ApprovalService =
    new ApprovalService(config)
}