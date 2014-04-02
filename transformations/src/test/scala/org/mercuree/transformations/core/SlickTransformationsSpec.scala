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
 * [[SlickTransformations]] test spec.
 *
 * @author Alexander Valyugin
 */
class SlickTransformationsSpec extends FlatSpec {

  class TransformationsPack(val localTransformations: List[LocalTransformation])
    extends SlickTransformations with LoggedTransformations {
    val profile = H2Driver
    val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
  }

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
    val local = LocalTransformation("test", InsertPersonSql, "", false)
    val pack = new TransformationsPack(List(local))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        pack.accomplish
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

  "Disabled transformations" should "be rolled back if had been applied previously" in {
    val local1 = LocalTransformation("test", InsertPersonSql, DeletePersonSql)
    val local2 = LocalTransformation("test", InsertPersonSql, DeletePersonSql, false)
    val pack1 = new TransformationsPack(List(local1))
    val pack2 = new TransformationsPack(List(local2))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        pack1.accomplish
        pack2.accomplish
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

  "Transformations running in transaction" should "be applied properly" in {
    val local = LocalTransformation("test", InsertPersonSql + FailingSql, "")
    val pack = new TransformationsPack(List(local))

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        pack.accomplish
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
    }
  }

  "Removed transformations" should "be rolled back if had been applied previously" in {
    val local = LocalTransformation("test", InsertPersonSql, DeletePersonSql)
    val pack1 = new TransformationsPack(List(local))
    val pack2 = new TransformationsPack(List())

    db.withSession {
      implicit session: Session =>
        Sql.updateNA(TestTableSql).execute
        pack1.accomplish
        pack2.accomplish
        val count = Sql.queryNA[Int](CountPersonsSql).first
        assert(count == 0)
        assert(pack2.storedTransformations.isEmpty)
    }
  }

}
