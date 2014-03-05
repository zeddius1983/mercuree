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
import scala.slick.jdbc.meta.MTable
import org.slf4j.LoggerFactory
import scala.slick.jdbc.{StaticQuery => Sql}
import scala.Some
import org.mercuree.transformations.core.Transformation.Supplement

/**
 * Transformations dao manager.
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationDao(val driver: JdbcProfile, val systemTableName: String = TransformationDao.DEFAULT_TABLE_NAME) {

  import driver.simple._

  final val logger = LoggerFactory.getLogger(getClass())

  /**
   * Database table schema definition. The table that keeps all database transformations applied.
   * <p>
   *
   * @author Alexander Valyugin
   */
  class TransformationTable(tag: Tag, tableName: String) extends Table[Transformation](tag, tableName) {
    def name = column[String]("name", O.PrimaryKey)

    def sqlUpdate = column[String]("sql_update", O.DBType("text"))

    def sqlUpdateHash = column[String]("sql_update_hash", O.DBType("char(128)"))

    def sqlRollback = column[String]("sql_rollback", O.DBType("text"))

    def sqlRollbackHash = column[String]("sql_rollback_hash", O.DBType("char(128)"))

    def * = (name, sqlUpdate, sqlUpdateHash, sqlRollback, sqlRollbackHash) <>(Transformation.fromTuple, Transformation.unapply)
  }

  val table = TableQuery[TransformationTable]((tag: Tag) => new TransformationTable(tag, systemTableName))

  /**
   * Applies the transformation to the given database.
   * <p>
   * @param transformation the transformation to apply.
   * @param session implicit db session param.
   * @return the applied transformation.
   */
  def <<(transformation: Transformation)(implicit session: Session): TransformationDao = {
    if (!MTable.getTables.list.exists(_.name.name == systemTableName)) {
      logger.info(s"Transformation table [$systemTableName] is missing!")
      table.ddl.create
      logger.info(s"Created transformation table [$systemTableName]")
    }

    val oldTransformationQuery = table.where(_.name === transformation.name)
    oldTransformationQuery.firstOption match {
      case Some(t) if t.sqlUpdateHash != transformation.sqlUpdateHash => {
        logger.info(s"Transformation update script '${transformation.name}' modification detected")
        Sql.updateNA(t.sqlRollback)
        logger.info(s"Transformation '${transformation.name}' rolled back")

        logger.info(s"Applying transformation '${transformation.name}'")
        Sql.updateNA(transformation.sqlUpdate)
        logger.info(s"Tansformation '${transformation.name}' applied")

        oldTransformationQuery.update(transformation)
      }
      case Some(t) if t.sqlRollbackHash != transformation.sqlRollbackHash => {
        logger.info(s"Transformation rollback script '${transformation.name}' modification detected")
        oldTransformationQuery.update(transformation)
        logger.info(s"Transformation '${transformation.name}' rollback script updated")
      }
      case None => {
        logger.info(s"Applying transformation '${transformation.name}' for the first time")
        Sql.updateNA(transformation.sqlUpdate)
        table += transformation
        logger.info(s"Tansformation '${transformation.name}' applied")
      }
      case _ => logger.info(s"Transformation '${transformation.name}' is already applied")
    }
    this
  }

}

object TransformationDao {

  final val DEFAULT_TABLE_NAME = "database_transformations"

}