package com.shivashriti.admin.persistance

import java.util.UUID

import com.shivashriti.admin.Status.Status
import com.shivashriti.admin._
import com.shivashriti.admin.models._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ItemDaoComponent {
  this: DatabaseService =>

  val itemDao: ItemDao

  import driver.api._
  implicit def approvalMappper = MappedColumnType.base[Status, String](e => e.toString, s => Status.withName(s))

  class ItemTable(tag: Tag) extends Table[ApprovalItem](tag, "approvalitems") {

    val id = column[UUID]("id", O.PrimaryKey)
    val name = column[String]("name")
    val status = column[Status]("status")
    val description = column[String]("description")
    val serviceUrl = column[String]("service_url")

    def * = (id, name, status, description, serviceUrl) <> (ApprovalItem.tupled, ApprovalItem.unapply)

  }

  def initSchema(): Future[Unit] =
    itemDao
      .dropSchema()
      .map(_ => itemDao.createSchema())

  class ItemDao extends Dao[ApprovalItem] with PostgresDatabaseService {

    def table: TableQuery[ItemTable] = TableQuery[ItemTable]

    def dropSchema(): Future[Unit] = db.run(table.schema.dropIfExists)
    def createSchema(): Future[Unit] = db.run(table.schema.create)

    def byStatus(status: Status): Future[Seq[ApprovalItem]] = db.run {
      table.filter(_.status === status).result
    }

    override def insert(entity: ApprovalItem): Future[ApprovalItem] = db.run {
      table returning table += entity
    }

    override def all: Future[Seq[ApprovalItem]] = db.run {
      table.to[Seq].result
    }

    override def byId(id: UUID): Future[Option[ApprovalItem]] = db.run {
      table.filter(_.id === id).result.headOption
    }

    override def update(id: UUID, entity: ApprovalItem): Future[Int] = db.run {
      table insertOrUpdate entity
    }

    override def remove(id: UUID): Future[Boolean] = db.run {
      table.filter(_.id === id).delete.map(_ > 0)
    }


  }

}