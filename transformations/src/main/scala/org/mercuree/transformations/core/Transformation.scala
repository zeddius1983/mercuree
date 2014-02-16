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

import scala.util.Try
import scala.io.{BufferedSource, Source}
import org.springframework.core.io.Resource
import java.net.URL

/**
 * Represents a database transformation.
 * <p>
 *
 * @author Alexander Valyugin
 */
case class Transformation(name: String, sqlUpdate: String, sqlUpdateHash: String,
                           sqlRollback: String, sqlRollbackHash: String)

object Transformation {

  private val NAME_ATTR = "@name"
  private val ENABLED_ATTR = "@enabled"
  private val ROOT_TAG = "TRANSFORMATION"
  private val UPDATE_TAG = "UPDATE"
  private val ROLLBACK_TAG = "ROLLBACK"

  def tupled: ((String, String, String, String, String)) => Transformation = {
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

  def parseXML(xml: scala.xml.Elem, defaultName: Option[String] = None) : Transformation = {
    if (xml.label != ROOT_TAG) {
      throw TransformationException(s"Transformation root element must be <$ROOT_TAG> tag")
    }
    val name = (xml \ NAME_ATTR).text match {
      case s if s.trim.nonEmpty => s
      case _ => {
        if (defaultName.nonEmpty)
          defaultName.get
        else
          throw TransformationException(s"Transformation name must be specified with $NAME_ATTR attribute")
      }
    }
    val enabled = (xml \ ENABLED_ATTR).text match {
      case "false" => false
      case _ => true
    }
    // Update script is mandatory
    val sqlUpdate = (xml \\ UPDATE_TAG).text match {
      case s if s.trim.nonEmpty => s
      case _ => throw TransformationException(s"Update script must be specified inside <$UPDATE_TAG> tag")
    }
    // Rollback script is not mandatory
    val sqlRollback = (xml \\ ROLLBACK_TAG).text

    Transformation(name, sqlUpdate, sqlUpdate.hashCode.toString,
      sqlRollback, sqlRollback.hashCode.toString)
  }

}

case class TransformationException(message: String) extends RuntimeException(message)
