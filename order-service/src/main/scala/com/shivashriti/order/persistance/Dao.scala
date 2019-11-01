package com.shivashriti.order.persistance

import java.util.UUID

import scala.concurrent.Future

trait Dao[T] {
  self: DatabaseService =>

  def insert(entity: T): Future[T]

  def byId(id: UUID): Future[Option[T]]

  def all: Future[Seq[T]]

  def update(id: UUID, entity: T): Future[Int]

  def remove(id: UUID): Future[Boolean]
}