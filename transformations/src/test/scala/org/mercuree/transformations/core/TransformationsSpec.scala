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

import org.scalatest.FlatSpec
import org.scalamock.scalatest.MockFactory

/**
 * [[Transformations]] process logic test spec.
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationsSpec extends FlatSpec with MockFactory {

  trait MockTransformations extends Transformations {
    val mockApply = stubFunction[LocalTransformation, Unit]("apply")
    val mockRollback = stubFunction[StoredTransformation, Unit]("rollback")
    val mockUpdate = stubFunction[LocalTransformation, Unit]("update")

    def apply(transformation: LocalTransformation) = mockApply(transformation)

    def rollback(transformation: StoredTransformation) = mockRollback(transformation)

    def update(transformation: LocalTransformation) = mockUpdate(transformation)
  }

  class TransformationsPack(val localTransformations: List[LocalTransformation],
                            val storedTransformations: List[StoredTransformation])
    extends Transformations with MockTransformations with LoggedTransformations

  "A new transformation" should "be applied" in {
    val local = LocalTransformation("test", "", "")
    val pack = new TransformationsPack(List(local), List())

    pack.accomplish

    pack.mockApply.verify(local).once
    pack.mockRollback.verify(*).never
    pack.mockUpdate.verify(*).never
  }

  it should "not be applied if disabled" in {
    val local = LocalTransformation("test", "", "", false)
    val pack = new TransformationsPack(List(local), List())

    pack.accomplish

    pack.mockApply.verify(local).never
    pack.mockRollback.verify(*).never
    pack.mockUpdate.verify(*).never
  }

  "Disabled transformations" should "be rolled back if applied before" in {
    val local = LocalTransformation("test", "", "", false)
    val stored = StoredTransformation("test", "", "", "", "")
    val pack = new TransformationsPack(List(local), List(stored))

    pack.accomplish

    pack.mockApply.verify(local).never
    pack.mockRollback.verify(stored).once
    pack.mockUpdate.verify(*).never
  }

  "Locally removed transformation" should "be rolled back" in {
    val stored = StoredTransformation("test", "", "", "", "")
    val pack = new TransformationsPack(List(), List(stored))

    pack.accomplish

    pack.mockApply.verify(*).never
    pack.mockUpdate.verify(*).never
    pack.mockRollback.verify(stored).once
  }

  "Modified transformation" should "be rolled back and applied again" in {
    val local = LocalTransformation("test", "A", "")
    val stored = StoredTransformation("test", "", "", "", local.sqlRollbackHash)
    val pack = new TransformationsPack(List(local), List(stored))

    pack.accomplish

    inSequence {
      pack.mockRollback.verify(stored).once
      pack.mockApply.verify(local).once
    }
    pack.mockUpdate.verify(*).never
  }

  "On rollback script modification it " should "only update the stored transformation" in {
    val local = LocalTransformation("test", "", "A")
    val stored = StoredTransformation("test", "", local.sqlUpdateHash, "", "")
    val pack = new TransformationsPack(List(local), List(stored))

    pack.accomplish

    pack.mockApply.verify(*).never
    pack.mockRollback.verify(*).never
    pack.mockUpdate.verify(local).once
  }

}
