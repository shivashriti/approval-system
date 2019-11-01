package com.shivashriti.admin

import java.util.UUID

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import akka.testkit.TestActor
import akka.testkit.TestProbe
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server._
import Directives._
import akka.actor.ActorRef
import com.shivashriti.admin.models._
import com.shivashriti.admin.routes.QueryRoutes

import scala.concurrent.duration._

class ApprovalServiceQuerySpec extends WordSpec with Matchers with ScalatestRouteTest with QueryRoutes {

  val queryActor = TestProbe()

  override def queryHandler: ActorRef = queryActor.ref

  override val timeout = akka.util.Timeout(5.seconds)

  lazy val routes = queryRoutes

  val sampleItem = ApprovalItem(UUID.randomUUID(), "John", Status.withName("Pending"), "Order limit exceeded", "abc.com")
  val approvedStatus = ItemStatus(Status.withName("Approved"))

  "QueryRoutes" should {

    "get approval item with id" in {
      val getItemRequest = HttpRequest(method = HttpMethods.GET, uri = "/api/v1/item/")

      getItemRequest.withUri(s"${sampleItem.id}") -> routes -> check {
        status should === (StatusCodes.OK)
      }
    }

    "get status of approval item with id" in {
      val getStatusRequest = HttpRequest(method = HttpMethods.POST, uri = s"/api/v1/item/")

      getStatusRequest.withUri(s"${sampleItem.id}/status") -> routes -> check {
        status should === (StatusCodes.OK)
        entityAs[String] should === ("""["Pending"]""")
      }
    }

    "get all items for approval" in {
      val getItemRequest = HttpRequest(method = HttpMethods.GET, uri = "/api/v1/item/")

      getItemRequest -> routes -> check {
        status should === (StatusCodes.OK)
      }
    }

    "get items for approval by specific status" in {
      val getItemRequest = HttpRequest(method = HttpMethods.GET, uri = "/api/v1/item/")

      getItemRequest.withUri("?status=Pending") -> routes -> check {
        status should === (StatusCodes.OK)
      }
    }

  }

}
