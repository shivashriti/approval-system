package com.shivashriti.order.persistance

import slick.jdbc.JdbcProfile

trait DatabaseService {

  val dbConfig: String

  val driver: JdbcProfile

  import driver.api._

  lazy val db = Database.forConfig(dbConfig)
}

trait PostgresDatabaseService extends DatabaseService {

  val driver: JdbcProfile = slick.jdbc.PostgresProfile
  val dbConfig: String = "pgconf"
}