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
import java.net.JarURLConnection
import sun.net.www.protocol.file.FileURLConnection

/**
 * TODO: scaladoc
 *
 * @author Alexander Valyugin
 */
object VersionAndPathOrdering extends Ordering[String] {

  val Version = """(\d+)(\.\d+)*""".r

  private def comparePathElement(left: String, right: String): Int = {
    val leftVersionOption = Version findFirstIn left
    val rightVersionOption = Version findFirstIn right
    (leftVersionOption, rightVersionOption) match {
      case (Some(leftVersion), Some(rightVersion)) => {
        val leftParts = leftVersion.split("\\.").toList.map(_.toInt)
        val rightParts = rightVersion.split("\\.").toList.map(_.toInt)
        comparePathElements(leftParts, rightParts)((x, y) => x.compareTo(y))
      }
      case (Some(leftVersion), None) => -1
      case (None, Some(rightVersion)) => 1
      case (None, None) => left.compareTo(right)
    }
  }

  @tailrec
  private def comparePathElements[A](left: List[A], right: List[A])(compare: (A, A) => Int): Int = (left, right) match {
    case (leftHead :: List(), rightHead :: List()) => compare(leftHead, rightHead)
    case (leftHead :: List(), rightHead :: rightTail) => -1
    case (leftHead :: leftTail, rightHead :: List()) => 1
    case (leftHead :: leftTail, rightHead :: rightTail) => compare(leftHead, rightHead) match {
      case 0 => comparePathElements(leftTail, rightTail)(compare)
      case x => x
    }
  }

  override def compare(left: String, right: String): Int = {
    val leftParts = left.split(File.separator).toList
    val rightParts = right.split(File.separator).toList
    comparePathElements(leftParts, rightParts)(comparePathElement)
  }

}

/**
 * TODO: scaladoc
 *
 * @author Alexander Valyugin
 */
trait ResourceLocalTransformations extends LocalTransformations {

  val SqlPattern = "/**/*.sql"

  /**
   * Provide with local transformations directory.
   * TODO: transformationsPath
   */
  val transformationsDirectory: String

  // TODO: remove Spring dependency
  val resolver: ResourcePatternResolver = new PathMatchingResourcePatternResolver

//  def listResources() {
//    val resources = getClass.getClassLoader.getResources(transformationsDirectory)
//    while (resources.hasMoreElements) {
//      val url = resources.nextElement
//      val urlConnection = url.openConnection
//      urlConnection match {
//        case jarUrl: JarURLConnection => {
//          println("yes")
//          val jar = jarUrl.getJarFile
//        }
//        case fileUrl: FileURLConnection => {
//        }
//      }
//      println(url)
//    }
//}


  def localTransformations: List[Transformation] = {
    val rootPath = resolver.getResource(transformationsDirectory).getFile.getAbsolutePath
    val resources = resolver.getResources(transformationsDirectory + SqlPattern).toList
    implicit val sorting = VersionAndPathOrdering
    resources map { resource =>
      val url = resource.getURL
      var id = resource.getFile.getAbsolutePath.substring(rootPath.length)
      if (id.startsWith(File.separator)) {
        id = id.substring(1)
      }
      (id, url)
    } sortBy(_._1) map {
      case (id, url) => LocalTransformation.fromURL(url, id)
    }
  }

}
