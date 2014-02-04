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
import scala.io.Source
import org.mercuree.evolution.core.Transformation

/**
 * TODO: javadoc
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationSpec extends FlatSpec {

  private val noNameSpecifiedXml = <TRANSFORMATION><UPDATE>script</UPDATE></TRANSFORMATION>

  "A transformation" should "be assigned a default name" in {
    val t = Transformation.fromXML(noNameSpecifiedXml)
    assert(t.name.nonEmpty)
  }

}
