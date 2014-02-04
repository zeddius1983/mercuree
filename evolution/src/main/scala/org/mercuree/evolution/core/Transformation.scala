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

/**
 * Represents a database transformation.
 * <p>
 *
 * @author Alexander Valyugin
 */
case class Transformation(name: String, sqlUpdate: String, sqlUpdateHash: String)

object Transformation {

  private val NAME_ATTR = "@names"
  private val ENABLED_ATTR = "@enabled"
  private val UPDATE_TAG = "UPDATE"
  private val ROLLBACK_TAG = "ROLLBACK"

  def tupled: ((String, String, String)) => Transformation = {
    case Tuple3(x1, x2, x3) => apply(x1, x2, x3)
  }

  /**
   * Parses the given xml string and returns the transformation object from it.
   *
   * @param xml string.
   * @return transformation instance.
   */
  def fromXML(xml: scala.xml.Elem) : Transformation = {
    val name = (xml \ NAME_ATTR).text match {
      case s if s.trim.nonEmpty => s
      case _ => "default"
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
