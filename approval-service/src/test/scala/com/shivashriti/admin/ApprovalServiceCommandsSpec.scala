package com.shivashriti.admin

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.shivashriti.admin.models._
import com.shivashriti.admin.routes.CommandRoutes
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class ApprovalServiceCommandsSpec extends WordSpec with Matchers with ScalatestRouteTest with CommandRoutes {

  val commandActor = TestProbe()

  override def commandHandler: ActorRef = commandActor.ref

  override val timeout = akka.util.Timeout(5.seconds)

  lazy val routes = commandRoutes

  val sampleItem = ApprovalItem(UUID.randomUUID(), "John", Status.withName("Pending"), "Order limit exceeded", "abc.com")
  val approvedStatus = ItemStatus(Status.withName("Approved"))

  "CommandRoutes" should {

    "post new approval item" in {
      val newItemRequest = HttpRequest(method = HttpMethods.POST, uri = "/api/v1/item", entity = sampleItem.toJson.toString())

      newItemRequest -> routes -> check {
        status should === (StatusCodes.Created)
      }
    }

    "change status of approval item" in {
      val statusChangeRequest = HttpRequest(method = HttpMethods.POST, uri = s"/api/v1/item/${sampleItem.id}/status", entity = approvedStatus.toJson.toString())

      statusChangeRequest -> routes -> check {
        status should === (StatusCodes.Created)
      }
    }

  }

}
