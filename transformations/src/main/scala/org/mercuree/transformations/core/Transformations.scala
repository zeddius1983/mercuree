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

import scala.slick.jdbc.JdbcBackend._
import scala.slick.driver.JdbcProfile
import org.mercuree.transformations.core.Transformation.Supplement
import scala.util.Try

/**
 * TODO: javadoc
 * <p>
 *
 * @author Alexander Valyugin
 */
class Transformations(val db: Database, val driver: JdbcProfile, val transformations: Seq[(Transformation, Supplement)]) {

  val transformationDao = new TransformationDao(driver)

  def run(): Unit = {
    val (enabledList, disabledList) = transformations.partition(_._2.enabled)
    disabledList.foreach(transformationWithSupplement => {
      println(s"Transformation ${transformationWithSupplement._1.name} is disabled and won't be ran")
    })

    enabledList.foreach(transformationWithSupplement => {
      val (t, s) = transformationWithSupplement

      def applyTransformation()(session: Session): Unit = transformationDao << t

      if (s.runInTransaction) {
        db.withTransaction(applyTransformation())
      } else {
        db.withSession(applyTransformation())
      }

    })

  }

}


object Transformations {

}