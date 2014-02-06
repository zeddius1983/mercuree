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

package org.mercuree.evolution.core

import scala.slick.driver.H2Driver.simple._
import TransformationTable._

/**
 * Database table schema definition. The table that keeps all database transformations applied.
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationTable(tag: Tag, tableName: String = DEFAULT_TABLE_NAME) extends Table[Transformation](tag, tableName) {
  def name = column[String]("name", O.PrimaryKey)
  def sqlUpdate = column[String]("sql_update", O.DBType("text"))
  def sqlUpdateHash = column[String]("sql_update_hash", O.DBType("char(128)"))

  def * = (name, sqlUpdate, sqlUpdateHash) <> (Transformation.tupled, Transformation.unapply)
}

object TransformationTable {
  final val DEFAULT_TABLE_NAME = "database_transformations"
}
