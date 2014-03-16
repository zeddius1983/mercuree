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

import org.scalatest.FlatSpec
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend.{Database, Session}
import scala.slick.jdbc.{StaticQuery => Sql}
import java.sql.SQLException

/**
 * Transformations test spec.
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationsSpec extends FlatSpec {

  val TestTableSql =
    """create table persons (
      |id int not null auto_increment,
      |fullname varchar(255),
      |primary key (id)
      |);
    """.stripMargin

  val InsertPersonSql = "insert into persons values (1, 'John Smith');"

  val DeletePersonSql = "delete from persons where id = 1;"

  val FailingSql = "insert into persons values (2, 3, 4);"

  val CountPersonsSql = "select count(*) from persons;"

  val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")

  "Disabled transformations" should "not be applied" in {
    val t = Transformation("test", InsertPersonSql, "", "", "")
    t.attributes = Some(TransformationAttributes(false, true))
    val transformations = new Transformations(List(t))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        transformations.execute(db, H2Driver)
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

  "Disabled transformations" should "be rolled back if had been applied previously" in {
    val t1 = Transformation("test", InsertPersonSql, "", DeletePersonSql, "")
    t1.attributes = Some(TransformationAttributes(true, true))
    val t2 = Transformation("test", InsertPersonSql, "", DeletePersonSql, "")
    t2.attributes = Some(TransformationAttributes(false, true))
    val transformationsFirst = new Transformations(List(t1))
    val transformationsSecond = new Transformations(List(t2))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        transformationsFirst.execute(db, H2Driver)
        transformationsSecond.execute(db, H2Driver)
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

  "Transformations running in session" should "be applied properly" in {
    val t = Transformation("test", InsertPersonSql + FailingSql, "", "", "")
    t.attributes = Some(TransformationAttributes(true, false))
    val transformations = new Transformations(List(t))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        transformations.execute(db, H2Driver)
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 1)
    }
  }

  "Transformations running in transaction" should "be applied properly" in {
    val t = Transformation("test", InsertPersonSql + FailingSql, "", "", "")
    t.attributes = Some(TransformationAttributes(true, true))
    val transformations = new Transformations(List(t))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        transformations.execute(db, H2Driver)
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

  "Removed transformations" should "be rolled back if had been applied previously" in {
    val t = Transformation("test", InsertPersonSql, "", DeletePersonSql, "")
    t.attributes = Some(TransformationAttributes(true, true))
    val transformationsFirst = new Transformations(List(t))
    val transformationsSecond = new Transformations(List())

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        transformationsFirst.execute(db, H2Driver)
        transformationsSecond.execute(db, H2Driver)
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

}
