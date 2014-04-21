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

import org.springframework.core.io.support.{ResourcePatternResolver, PathMatchingResourcePatternResolver}
import scala.annotation.tailrec
import java.io.File
import scala.util.{Failure, Try}
import org.slf4j.LoggerFactory

/**
 * Ordering strategy for paths (e.g. '1.0.0/some.sql' or 'stored/some.sql') that mixes
 * lexicographic ordering and version number based ordering, where the later has a priority
 * over the former.
 *
 * @author Alexander Valyugin
 */
object FilePathOrdering extends Ordering[String] {

  private final val Version = """(\d+)(\.\d+)*""".r

  private def comparePathElement(left: String, right: String): Int = {
    val leftVersionOption = Version findFirstIn left
    val rightVersionOption = Version findFirstIn right
    (leftVersionOption, rightVersionOption) match {
      case (Some(leftVersion), Some(rightVersion)) => {
        val leftParts = leftVersion.split("\\.").toList.map(_.toInt)
        val rightParts = rightVersion.split("\\.").toList.map(_.toInt)
        compareVersionElements(leftParts, rightParts)
      }
      case (Some(leftVersion), None) => -1
      case (None, Some(rightVersion)) => 1
      case (None, None) => left.compareTo(right)
    }
  }

  @tailrec
  private def compareVersionElements(left: List[Int], right: List[Int]): Int = {
    val leftHead :: leftTail = left
    val rightHead :: rightTail = right
    val cmp = leftHead.compareTo(rightHead)
    if (cmp == 0 && leftTail.nonEmpty && rightTail.nonEmpty)
      compareVersionElements(leftTail, rightTail)
    else
      cmp
  }

  @tailrec
  private def comparePathElements(left: List[String], right: List[String]): Int = (left, right) match {
    case (leftHead :: List(), rightHead :: List()) => comparePathElement(leftHead, rightHead)
    case (leftHead :: List(), rightHead :: rightTail) => -1
    case (leftHead :: leftTail, rightHead :: List()) => 1
    case (leftHead :: leftTail, rightHead :: rightTail) => comparePathElement(leftHead, rightHead) match {
      case 0 => comparePathElements(leftTail, rightTail)
      case x => x
    }
  }

  override def compare(left: String, right: String): Int = {
    val leftParts = left.split(File.separator).toList
    val rightParts = right.split(File.separator).toList
    comparePathElements(leftParts, rightParts)
  }

}

/**
 * Provides local transformations found in the files on the given path.
 * Transformation id then would be a unique relative file path.
 *
 * @author Alexander Valyugin
 */
trait FileLocalTransformations extends LocalTransformations {

  private final val logger = LoggerFactory.getLogger(getClass)

  private final val SqlPattern = "/**/*.sql"

  /**
   * Provide with local transformations path.
   */
  val transformationsPath: String

  // TODO: remove Spring dependency
  private val resolver: ResourcePatternResolver = new PathMatchingResourcePatternResolver

  def localTransformations: List[Transformation] = {
    val rootPath = resolver.getResource(transformationsPath).getFile.getAbsolutePath
    val resources = resolver.getResources(transformationsPath + SqlPattern).toList
    implicit val sortedBy = FilePathOrdering
    resources map { resource =>
      val url = resource.getURL
      val relativePath = resource.getFile.getAbsolutePath.substring(rootPath.length)
      val id = if (relativePath.startsWith(File.separator)) relativePath.substring(1) else relativePath
      Try(LocalTransformation.fromURL(url, id)) recoverWith {
        case e: Exception =>
          logger.error(s"Unabled to load [${id}] due to:\n ${e.getMessage}")
          Failure(e)
      }
    } filter (_.isSuccess) map (_.get) sortBy (_.id)
  }

}
