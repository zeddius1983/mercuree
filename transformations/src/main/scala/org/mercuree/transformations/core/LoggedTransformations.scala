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

/**
 * Logs transformation processing steps.
 *
 * @author Alexander Valyugin
 */
trait LoggedTransformations {

  import System.{currentTimeMillis => currentTime}

  def profile[R](f: => R, t: Long = currentTime) = { f; currentTime - t }

//  abstract override def apply(transformation: LocalTransformation) {
//    logger.info(s"--Apply transformation '${transformation.name}'")
//    val elapsed = profile {
//      super.apply(transformation)
//    }
//    logger.info(s"--Transformation '${transformation.name}' successfully applied in $elapsed ms")
//  }
//
//  abstract override def rollback(transformation: StoredTransformation) {
//    logger.info(s"--Rollback transformation '${transformation.name}'")
//    val elapsed = profile {
//      super.rollback(transformation)
//    }
//    logger.info(s"--Transformation '${transformation.name}' successfully rollbacked in $elapsed ms")
//  }
//
//  abstract override def update(transformation: LocalTransformation) {
//    logger.info(s"--Update transformation '${transformation.name}'")
//    val elapsed = profile {
//      super.update(transformation)
//    }
//    logger.info(s"--Transformation '${transformation.name}' successfully updated in $elapsed ms")
//  }

}
