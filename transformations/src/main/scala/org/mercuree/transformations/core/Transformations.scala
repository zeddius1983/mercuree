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
import org.slf4j.LoggerFactory
import scala.language.implicitConversions

/**
 * Represents a database change.
 */
trait Transformation {
  val name: String
  val sqlUpdate: String
  val sqlUpdateHash: String
  val sqlRollback: String
  val sqlRollbackHash: String
}

/**
 * A transformation that has been already applied to a database.
 */
case class StoredTransformation(name: String, sqlUpdate: String, sqlUpdateHash: String,
                                sqlRollback: String, sqlRollbackHash: String) extends Transformation

object StoredTransformation {
  def fromTuple: ((String, String, String, String, String)) => StoredTransformation = {
    case Tuple5(x1, x2, x3, x4, x5) => apply(x1, x2, x3, x4, x5)
  }
}

/**
 * A user requested transformation to apply.
 */
case class LocalTransformation(name: String, sqlUpdate: String, sqlRollback: String,
                               enabled: Boolean = true) extends Transformation {
  val disabled = !enabled
  val sqlUpdateHash = md5(sqlUpdate)
  val sqlRollbackHash = md5(sqlRollback)

  private def md5(text: String) = MessageDigest.getInstance("MD5").digest(text.getBytes).map("%02x".format(_)).mkString
}

object LocalTransformation {

  private val NameAttr = "@name"
  private val EnabledAttr = "@enabled"
  private val RootTag = "transformation"
  private val UpdateTag = "update"
  private val RollbackTag = "rollback"

  /**
   * Loads the transformation from the given url.
   *
   * @param url file path.
   * @param defaultName default transformation name.
   * @return transformation object.
   */
  def fromURL(url: URL, defaultName: Option[String] = None): LocalTransformation = {
    val source = Source.fromURL(url).mkString
    parseSQL(source, defaultName)
  }

  implicit def localToStored(local: LocalTransformation) = StoredTransformation(
    local.name, local.sqlUpdate, local.sqlUpdateHash, local.sqlRollback, local.sqlRollbackHash)

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
  def parseSQL(sql: String, defaultName: Option[String] = None): LocalTransformation = {
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
  def parseXML(xml: scala.xml.Elem, defaultName: Option[String] = None): LocalTransformation = {
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

    LocalTransformation(name, sqlUpdate, sqlRollback, enabled)
  }
}

case class TransformationException(message: String) extends RuntimeException(message)

/**
 * TODO: scaladoc
 *
 * @author Alexander Valyugin
 */
trait Transformations {

  final val logger = LoggerFactory.getLogger(getClass)

  /**
   * Returns the list of already applied database transformations.
   *
   * @return a list of [[StoredTransformation]].
   */
  def storedTransformations: List[StoredTransformation]

  /**
   * Returns the list of user requested transformations to be applied.
   *
   * @return a list of [[LocalTransformation]].
   */
  def localTransformations: List[LocalTransformation]

  /**
   * Applies the given transformation to the database.
   * Once applied transformation should be found in stored transformations.
   *
   * @param transformation local transformation to apply.
   */
  def apply(transformation: LocalTransformation)

  /**
   * Rollbacks already applied transformation from the database.
   * After rollback the transformation won't be found in stored transformations.
   *
   * @param transformation stored transformation to rollback.
   */
  def rollback(transformation: StoredTransformation)

  /**
   * Updates the transformation details without applying it to the database.
   * Having this done the stored transformations is updated with the data from
   * the provided local transformation.
   *
   * @param transformation to update with.
   */
  def update(transformation: LocalTransformation)

  def process(tuple: (Option[LocalTransformation], Option[StoredTransformation])) {
    val name = tuple._1.orElse(tuple._2).get.name
    logger.info(s"Processing transformation '$name'")
    try {
      tuple match {
        case (Some(local), Some(stored)) => {
          if (local.disabled) {
            logger.info(s"-Transformation '${name}' is disabled")
            rollback(stored)
          } else if (local.sqlUpdateHash != stored.sqlUpdateHash) {
            logger.info(s"-Transformation '${name}' update script is modified")
            rollback(stored)
            apply(local)
          } else if (local.sqlRollbackHash != stored.sqlRollbackHash) {
            logger.info(s"-Transformation '${name}' rollback script is modified")
            update(local)
          }
        }
        case (Some(local), None) => {
          if (local.enabled) {
            logger.info(s"-Transformation '${name}' can be applied")
            apply(local)
          }
        }
        case (None, Some(stored)) => {
          logger.info(s"-Transformation '${name}' is removed by a user")
          rollback(stored)
        }
        case (None, None) => logger.error("Never reached")
      }
      logger.info(s"Transformation '$name' completed successfully")
    } catch {
      case e: Exception => logger.error(s"Transformation '$name' processing has failed due to:\n $e")
    }
  }

  def accomplish {
    val storedMap = storedTransformations.map(t => (t.name, t)).toMap
    val localMap = localTransformations.map(t => (t.name, t)).toMap
    val all = (storedMap.keySet ++ localMap.keySet).map(name => (localMap.get(name), storedMap.get(name)))
    all.foreach(process)
  }

}