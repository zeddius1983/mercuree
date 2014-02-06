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

package org.mercuree.evolution.core

import scala.util.Try
import scala.io.Source
import scala.xml.{Null, UnprefixedAttribute}

/**
 * Represents a database transformation.
 * <p>
 *
 * @author Alexander Valyugin
 */
case class Transformation(name: String, sqlUpdate: String, sqlUpdateHash: String)

object Transformation {

  private val NAME_ATTR = "@name"
  private val ENABLED_ATTR = "@enabled"
  private val ROOT_TAG = "TRANSFORMATION"
  private val UPDATE_TAG = "UPDATE"
  private val ROLLBACK_TAG = "ROLLBACK"

  def tupled: ((String, String, String)) => Transformation = {
    case Tuple3(x1, x2, x3) => apply(x1, x2, x3)
  }

  /**
   * Loads the transformation from the sql script file provided in the given format.
   *
   * @param path the file path may be classpath or absolute path.
   * @return transformation instance.
   */
  def loadFromFile(path: String): Try[Transformation] = {
    for {
      url <- Try(getClass().getResource(path))
      source <- Try(Source.fromURL(url)) orElse Try(Source.fromFile(path))
      xml <- Try(scala.xml.XML.loadString(source.mkString.replace("--<", "<")))
    } yield fromXML(xml, Some(path))
  }

  /**
   * Parses the given xml string and returns the transformation object from it.
   *
   * @param xml string.
   * @return transformation instance.
   */
  def loadFromXML(xml: scala.xml.Elem, defaultName: Option[String] = None) : Try[Transformation] = {
    Try(fromXML(xml, defaultName))
  }

  private def fromXML(xml: scala.xml.Elem, defaultName: Option[String] = None) : Transformation = {
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

    Transformation(name, sqlUpdate, sqlUpdate.hashCode.toString)
  }

}

case class TransformationException(message: String) extends RuntimeException(message)
