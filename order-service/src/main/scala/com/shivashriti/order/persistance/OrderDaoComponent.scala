package com.shivashriti.order.persistance

import java.util.UUID

import com.shivashriti.order._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait OrderDaoComponent {
  this: DatabaseService =>

  val orderDao: OrderDao

  import driver.api._

  class OrderTable(tag: Tag) extends Table[Order](tag, "orders") {

    val id = column[UUID]("id", O.PrimaryKey)
    val customerName = column[String]("customer_name")
    val amount = column[Double]("amount")
    val status = column[String]("status")

    def * = (id, customerName, amount, status) <> (Order.tupled, Order.unapply)
  }

  def initSchema(): Future[Unit] =
    orderDao
      .dropSchema()
      .map(_ => orderDao.createSchema())

  class OrderDao extends Dao[Order] with PostgresDatabaseService {

    def table: TableQuery[OrderTable] = TableQuery[OrderTable]

    def dropSchema(): Future[Unit] = db.run(table.schema.dropIfExists)
    def createSchema(): Future[Unit] = db.run(table.schema.create)

    override def insert(entity: Order): Future[Order] = db.run {
      table returning table += entity
    }

    override def all: Future[Seq[Order]] = db.run {
      table.to[Seq].result
    }

    override def byId(id: UUID): Future[Option[Order]] = db.run {
      table.filter(_.id === id).result.headOption
    }

    override def update(id: UUID, entity: Order): Future[Int] = db.run {
      table insertOrUpdate entity
    }

    override def remove(id: UUID): Future[Boolean] = db.run {
      table.filter(_.id === id).delete.map(_ > 0)
    }


  }

}