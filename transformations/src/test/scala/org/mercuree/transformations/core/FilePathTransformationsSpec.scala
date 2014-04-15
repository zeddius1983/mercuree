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
import java.io.File

/**
 * [[ResourceLocalTransformations]] test spec.
 *
 * @author Alexander Valyugin
 */
class FilePathTransformationsSpec extends FlatSpec {

  class LocalTransformationsPack(val transformationsDirectory: String) extends ResourceLocalTransformations

  "Local transformations" should "be loaded from the classpath" in {
    val pack = new LocalTransformationsPack("/transformations")
    println(pack.localTransformations)

//    pack.listResources()
//    println("create.sql".split(File.separator).toList)
//    val Decimal = """(\d+)(\.\d+)*""".r
//    for (s <- Decimal findAllIn "Versions 2013.1.2, 4.1.0.13, 4.05, 1.2.0.14, 2014, 7.sql")
//      println(s.replaceAll("\\.", "").toLong)
  }

}
