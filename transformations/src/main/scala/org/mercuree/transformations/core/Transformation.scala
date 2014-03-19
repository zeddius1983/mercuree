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

import scala.io.Source
import java.net.URL
import java.security.MessageDigest

/**
 * Represents a database transformation.
 *
 * @author Alexander Valyugin
 */
case class Transformation(name: String, sqlUpdate: String, sqlUpdateHash: String,
                          sqlRollback: String, sqlRollbackHash: String) {
  class RootAttributes(var enabled: Boolean)

  var rootAttributes = new RootAttributes(true)
}

object Transformation {

  private val NameAttr = "@name"
  private val EnabledAttr = "@enabled"
  private val RootTag = "transformation"
  private val UpdateTag = "update"
  private val RollbackTag = "rollback"

  def fromTuple: ((String, String, String, String, String)) => Transformation = {
    case Tuple5(x1, x2, x3, x4, x5) => apply(x1, x2, x3, x4, x5)
  }

  /**
   * Loads the transformation from the given url.
   *
   * @param url file path.
   * @param defaultName default transformation name.
   * @return transformation object.
   */
  def fromURL(url: URL, defaultName: Option[String] = None): Transformation = {
    val source = Source.fromURL(url).mkString
    parseSQL(source, defaultName)
  }

  /**
   * Parses the sql source to obtain a transformation object. Valid sql text to parse may look like this:
   * {{{
   * --<transformation name="create_table_script" enabled="true">
   *   --<update>
   *     -- script body here
   *   --</update>
   *   --<rollback>
   *     -- rollback script body here
   *   --</rollback>
   * --</transformation>
   * }}}
   *
   * @param sql sql text.
   * @param defaultName default transformation name.
   * @return transformation object.
   */
  def parseSQL(sql: String, defaultName: Option[String] = None): Transformation = {
    val xml = scala.xml.XML.loadString(sql.replace("--<", "<"))
    parseXML(xml, defaultName)
  }

  /**
   * Parses the valid xml to obtain a transformation object.
   *
   * @param xml xml document.
   * @param defaultName default transformation name.
   * @return transformation object.
   */
  def parseXML(xml: scala.xml.Elem, defaultName: Option[String] = None): Transformation = {
    if (xml.label != RootTag) {
      throw TransformationException(s"Transformation root element must be <$RootTag> tag")
    }
    val name = (xml \ NameAttr).text.trim match {
      case s if s.nonEmpty => s
      case _ => defaultName.getOrElse(throw TransformationException(s"Transformation name must be specified with $NameAttr attribute"))
    }
    val enabled = (xml \ EnabledAttr).text.trim match {
      case "false" => false
      case _ => true
    }
    // Update script is mandatory
    val sqlUpdate = (xml \\ UpdateTag).text.trim match {
      case s if s.nonEmpty => s
      case _ => throw TransformationException(s"Update script must be specified inside <$UpdateTag> tag")
    }
    // Rollback script is not mandatory
    val sqlRollback = (xml \\ RollbackTag).text.trim

    def md5(text: String) = MessageDigest.getInstance("MD5").digest(text.getBytes).map("%02x".format(_)).mkString

    val transformation = Transformation(name, sqlUpdate, md5(sqlUpdate), sqlRollback, md5(sqlRollback))
    transformation.rootAttributes.enabled = enabled
    transformation
  }

}

case class TransformationException(message: String) extends RuntimeException(message)
