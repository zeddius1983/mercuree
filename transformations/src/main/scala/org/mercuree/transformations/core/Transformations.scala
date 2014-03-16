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

import scala.slick.driver.JdbcProfile
import org.slf4j.LoggerFactory
import scala.slick.jdbc.JdbcBackend.Database
import scala.util.{Success, Failure, Try}

/**
 * TODO: javadoc
 * <p>
 *
 * @author Alexander Valyugin
 */
class Transformations(val transformations: Seq[Transformation]) {

  val logger = LoggerFactory.getLogger(getClass)

  /**
   * Executes transformations.
   */
  def execute(db: Database, driver: JdbcProfile): Unit = {
    val transformationDao = new TransformationDao(driver)
    import transformationDao.driver.simple._

    def rollback(transformation: Transformation)(implicit session: Session): Unit = {
      Try(transformationDao.rollback(transformation)).recover {
        case e => logger.error(s"Transformation '${transformation.name}' rollback failed due to ", e)
      }
    }

    def apply(transformation: Transformation)(implicit session: Session): Unit = {
      Try(transformationDao.apply(transformation)).recover {
        case e => logger.error(s"Transformation '${transformation.name}' is not applied due to ", e)
      }
    }

    db.withSession {
      implicit session =>
        transformationDao.ensureSystemTable

        // First rollback disabled transformations
        val (enabledList, disabledList) = transformations.partition(_.attributes.get.enabled)
        disabledList.foreach {
          t =>
            session.withTransaction {
              logger.info(s"Transformation '${t.name}' is disabled")
              rollback(t)
            }
        }

        enabledList.foreach {
          t =>
            if (t.attributes.get.runInTransaction) {
              session.withTransaction {
                apply(t)
              }
            } else {
              apply(t)
            }

        }

        // Rollback all deleted transformations
        val databaseTransformations = transformationDao.all.map(_.name).toSet
        val localTransformations = enabledList.map(_.name).toSet
        val removedTransformationNames = databaseTransformations -- localTransformations
        val transformationsToRollback = transformationDao.all.filter(t => removedTransformationNames.contains(t.name))
        transformationsToRollback.foreach {
          t =>
            session.withTransaction {
              logger.info(s"Transformation '${t.name}' has been removed")
              rollback(t)
            }
        }
    }
  }

}


object Transformations {

}