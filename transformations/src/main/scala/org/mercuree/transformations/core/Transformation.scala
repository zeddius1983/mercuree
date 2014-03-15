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

/**
 * Represents a database transformation.
 * <p>
 *
 * @author Alexander Valyugin
 */
case class Transformation(name: String, sqlUpdate: String, sqlUpdateHash: String,
                          sqlRollback: String, sqlRollbackHash: String) {
  var attributes: Option[TransformationAttributes] = None
}

/**
 * Supplementary data that is used to control the transformation execution but is
 * not persisted within the control table.
 * <p>
 *
 * @author Alexander Valyugin
 */
case class TransformationAttributes(enabled: Boolean, runInTransaction: Boolean)

object Transformation {

  private val NameAttr = "@name"
  private val EnabledAttr = "@enabled"
  private val RunInTransactionAttr = "@runInTransaction"
  private val RootTag = "TRANSFORMATION"
  private val UpdateTag = "UPDATE"
  private val RollbackTag = "ROLLBACK"

  def fromTuple: ((String, String, String, String, String)) => Transformation = {
    case Tuple5(x1, x2, x3, x4, x5) => apply(x1, x2, x3, x4, x5)
  }

  /**
   * Loads the transformation from the given url.
   * <p>
   *
   * @param url file path.
   * @return transformation instance.
   */
  def fromURL(url: URL): Transformation = {
    val source = Source.fromURL(url)
    val xml = scala.xml.XML.loadString(source.mkString.replace("--<", "<"))
    parseXML(xml, Some(url.getFile))
  }

  def parseSQL(sql: String, defaultName: Option[String] = None): Transformation = {
    val xml = scala.xml.XML.loadString(sql.replace("--<", "<"))
    parseXML(xml, defaultName)
  }

  def parseXML(xml: scala.xml.Elem, defaultName: Option[String] = None): Transformation = {
    if (xml.label != RootTag) {
      throw TransformationException(s"Transformation root element must be <$RootTag> tag")
    }
    val name = (xml \ NameAttr).text match {
      case s if s.trim.nonEmpty => s
      case _ => defaultName.getOrElse(throw TransformationException(s"Transformation name must be specified with $NameAttr attribute"))
    }
    val enabled = (xml \ EnabledAttr).text.trim match {
      case "false" => false
      case _ => true
    }
    val runInTransaction = (xml \ RunInTransactionAttr).text.trim match {
      case "true" => true
      case _ => false
    }
    // Update script is mandatory
    val sqlUpdate = (xml \\ UpdateTag).text match {
      case s if s.trim.nonEmpty => s
      case _ => throw TransformationException(s"Update script must be specified inside <$UpdateTag> tag")
    }
    // Rollback script is not mandatory
    val sqlRollback = (xml \\ RollbackTag).text

    val transformation = Transformation(name, sqlUpdate, sqlUpdate.hashCode.toString, sqlRollback, sqlRollback.hashCode.toString)
    transformation.attributes = Some(TransformationAttributes(enabled, runInTransaction))
    transformation
  }

}

case class TransformationException(message: String) extends RuntimeException(message)
