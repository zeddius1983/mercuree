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
import scala.Some

/**
 * [[LocalTransformation]] test spec.
 *
 * @author Alexander Valyugin
 */
class LocalTransformationSpec extends FlatSpec {

  private val invalidRootTagXml = <evolution></evolution>
  private val noNameSpecifiedXml = <transformation></transformation>
  private val updateTagMissedXml = <transformation name="test"></transformation>
  private val noUpdateScriptXml = <transformation name="test"><update></update></transformation>
  private val transformationXml = <transformation name="test" enabled="false"><update>script</update></transformation>

  "A transformation construction" should "fail if the root tag is invalid" in {
    intercept[TransformationException] {
      LocalTransformation.parseXML(invalidRootTagXml)
    }
  }

  it should "fail if the name is not specified" in {
    intercept[TransformationException] {
      LocalTransformation.parseXML(noNameSpecifiedXml)
    }
  }

  it should "fail if the update tag is missing" in {
    intercept[TransformationException] {
      LocalTransformation.parseXML(updateTagMissedXml)
    }
  }

  it should "fail if the update script is not specified" in {
    intercept[TransformationException] {
      LocalTransformation.parseXML(noUpdateScriptXml)
    }
  }

  "A transformation" should "be loaded from the url correctly" in {
    val url = getClass.getResource("/transformations/create_table.sql")
    val transformation = LocalTransformation.fromURL(url, Some("test"))

    assert("test" == transformation.name)
    assert(transformation.enabled)
    assert(transformation.sqlUpdate ==
      """
        |CREATE TABLE User (
        |    id bigint(20) NOT NULL AUTO_INCREMENT,
        |    email varchar(255) NOT NULL,
        |    password varchar(255) NOT NULL,
        |    fullname varchar(255) NOT NULL,
        |    isAdmin boolean NOT NULL,
        |    PRIMARY KEY (id)
        |);
      """.stripMargin.trim)
    assert("0E5BEB7344F44C053094BEAD4411B621" == transformation.sqlUpdateHash.toUpperCase)
    assert("DROP TABLE User;" == transformation.sqlRollback)
    assert("BCB8140B058A8CA2F5DCA6BF6B26B4B9" == transformation.sqlRollbackHash.toUpperCase)
  }

  "Enabled attribute" should "be parsed properly" in {
    val transformation = LocalTransformation.parseXML(transformationXml)

    assert(!transformation.enabled)
  }

  "Default transformation name" should "be overrided by a name attribute" in {
    val transformation = LocalTransformation.parseXML(transformationXml, Some("some_script"))

    assert("test" == transformation.name)
  }

}
