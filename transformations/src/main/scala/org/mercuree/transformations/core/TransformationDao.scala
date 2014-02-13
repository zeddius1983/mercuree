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
import scala.slick.jdbc.{GetResult, StaticQuery => Sql}

/**
 * Data access object for {@link Transformation}.
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

    def * = (name, sqlUpdate, sqlUpdateHash) <>(Transformation.tupled, Transformation.unapply)
  }

  val transformations = TableQuery[TransformationTable]((tag: Tag) => new TransformationTable(tag, systemTableName))

  def apply(transformation: Transformation)(implicit session: Session): Unit = {
    if (!MTable.getTables.list.exists(_.name.name == systemTableName)) {
      transformations.ddl.create
      logger.info(s"Created transformation table [$systemTableName]")
    }

    val oldTransformationQuery = transformations.filter(_.name.toString() == transformation.name)
    oldTransformationQuery.firstOption match {
      case Some(t) if t.sqlUpdateHash != transformation.sqlUpdateHash => {
        oldTransformationQuery.update(transformation)
        Sql.updateNA(transformation.sqlUpdate)
      }
      case None => {
        transformations += transformation
        Sql.updateNA(transformation.sqlUpdate)
      }
    }
  }

}

object TransformationDao {

  final val DEFAULT_TABLE_NAME = "database_transformations"

}
