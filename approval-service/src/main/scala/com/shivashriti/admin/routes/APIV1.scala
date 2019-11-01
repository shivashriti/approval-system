package com.shivashriti.admin.routes

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._

trait ApiV1 {
  val apiPrefix: Directive[Unit] = pathPrefix("api" / "v1")
}
