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
import scala.util.Random

/**
 * [[VersionAndPathOrdering]] test spec.
 *
 * @author Alexander Valyugin
 */
class VersionAndPathOrderingSpec extends FlatSpec {

  "Paths" should "be in the right order" in {
    val expectedPaths = List(
      "init.sql",
      "1.0.0/table.sql",
      "1.0.0/common/clean.sql",
      "1.0.1/alter.sql",
      "2.1/new.sql",
      "2013.2.1/drop.sql",
      "2013.2.1/erase.sql",
      "common/houseKeep.sql"
    )
    val actualPaths = Random.shuffle(expectedPaths) sorted VersionAndPathOrdering

    expectedPaths zip actualPaths foreach(it => assert(it._1 == it._2))
  }


}
