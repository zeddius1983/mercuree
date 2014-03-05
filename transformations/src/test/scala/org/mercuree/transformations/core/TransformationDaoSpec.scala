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
import scala.slick.jdbc.{StaticQuery => Sql}
import scala.util.Try

/**
 * TransformationDao test spec.
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationDaoSpec extends FlatSpec {

  val transformations = new TransformationDao(H2Driver)
  import transformations.driver.simple._

  "Transformation system table" should "be created if absent" in {
    val t = Transformation("test", "", "", "", "")
    val db = Database.forURL("jdbc:h2:mem:test", driver = "org.h2.Driver")
    db.withTransaction {
      implicit session =>
        assert(Try(transformations.table.exists.run).isFailure)
        transformations << t << t // Check table is not attempted to be created twice
        assert(transformations.table.exists.run)
    }
  }

}
