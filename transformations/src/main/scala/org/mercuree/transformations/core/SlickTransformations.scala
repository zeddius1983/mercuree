/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mercuree.transformations.core

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend._
import scala.slick.jdbc.{StaticQuery => Sql}
import Database.dynamicSession
import scala.language.implicitConversions

/**
 * Slick based implementation.
 *
 * @author Alexander Valyugin
 */
trait SlickTransformations extends Transformations {

  val transformationsTableName = "database_transformations"

  val profile: JdbcProfile

  val db: Database

  import profile.simple._

  /**
   * Database table schema definition. The table that keeps all database transformations applied.
   */
  private class TransformationTable(tag: Tag, tableName: String) extends Table[StoredTransformation](tag, tableName) {
    def name = column[String]("name", O.PrimaryKey)

    def sqlUpdate = column[String]("sql_update", O.DBType("text"))

    def sqlUpdateHash = column[String]("sql_update_hash", O.DBType("char(128)"))

    def sqlRollback = column[String]("sql_rollback", O.DBType("text"))

    def sqlRollbackHash = column[String]("sql_rollback_hash", O.DBType("char(128)"))

    def * = (name, sqlUpdate, sqlUpdateHash, sqlRollback, sqlRollbackHash) <>(StoredTransformation.fromTuple, StoredTransformation.unapply)
  }

  private lazy val transformationsTable = TableQuery[TransformationTable]((tag: Tag) => new TransformationTable(tag, transformationsTableName))

  def storedTransformations: List[StoredTransformation] = transformationsTable.list

  private def createSystemTable {
    import scala.slick.jdbc.meta.MTable
    if (!MTable.getTables.list.exists(_.name.name == transformationsTableName)) {
      logger.debug(s"Transformation table [$transformationsTableName] is missing!")
      transformationsTable.ddl.create
      logger.debug(s"Created transformation table [$transformationsTableName]")
    }
  }

  def apply(transformation: LocalTransformation) {
    Sql.updateNA(transformation.sqlUpdate).execute
    transformationsTable += transformation
  }

  def rollback(transformation: StoredTransformation) {
    val storedTransformation = transformationsTable.where(_.name === transformation.name)
    Sql.updateNA(storedTransformation.first.sqlRollback).execute
    storedTransformation.delete
  }

  def update(transformation: LocalTransformation) {
    val storedTransformation = transformationsTable.where(_.name === transformation.name)
    storedTransformation.update(transformation)
  }

  override def process(tuple: (Option[LocalTransformation], Option[StoredTransformation]))
    = db.withDynTransaction(super.process(tuple))

  override def accomplish = db.withDynSession {
    createSystemTable
    super.accomplish
  }

}
