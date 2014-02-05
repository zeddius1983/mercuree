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
import org.mercuree.evolution.core.{TransformationException, Transformation}

/**
 * TODO: javadoc
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationSpec extends FlatSpec {

  private val invalidRootTagXml = <EVOLUTION></EVOLUTION>
  private val noNameSpecifiedXml = <TRANSFORMATION><UPDATE>script</UPDATE></TRANSFORMATION>

  "A transformation" should "throw an exception if root tag is invalid" in {
    intercept[TransformationException] {
      Transformation.fromXML(invalidRootTagXml)
    }
  }

  it should "throw an exception if name is not specified" in {
    intercept[TransformationException] {
      Transformation.fromXML(noNameSpecifiedXml)
    }
  }

  it should "be loaded from file on a classpath" in {
    val t = Transformation.loadFromFile("/transformations/create_table.sql")
    println(t)
  }

}
