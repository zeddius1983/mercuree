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

/**
 * TODO: javadoc
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationDaoSpec extends FlatSpec {

  "Transformation" should "be loaded from file on a classpath" in {
    val dao = new TransformationDao(H2Driver)
    import dao.driver.simple._

    val db = Database.forURL("jdbc:h2:mem:hello", driver = "org.h2.Driver")
    db.withSession {
      implicit session =>
        Transformation.loadFromFile("/transformations/create_table.sql").map(dao.apply(_))
        Transformation.loadFromFile("/transformations/alter_table.sql").map(dao.apply(_))
        dao.transformations foreach (println(_))
    }

  }

}
