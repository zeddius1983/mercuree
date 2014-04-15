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
 * [[Transformations]] test.
 * <p>
 *
 * @author Alexander Valyugin
 */
class TransformationsSpec extends FlatSpec with MockFactory {

  trait MockStoredTransformations extends StoredTransformations {

    val mocked = stub[StoredTransformations]

    override def findAllExcept(names: Set[String]): Seq[StoredTransformation] = mocked.findAllExcept(names)

    override def findById(name: String): Option[StoredTransformation] = mocked.findById(name)

    override def insert(transformation: LocalTransformation): Unit = mocked.insert(transformation)

    override def delete(transformation: Transformation): Unit = mocked.delete(transformation)

    override def update(transformation: LocalTransformation): Unit = mocked.update(transformation)

    override def applyScript(script: String): Unit = mocked.applyScript(script)

    override def transform[A](f: => A): A = f

    override def transactional[A](f: => A): A = f

  }

  class TestTransformations(override val localTransformations: List[Transformation])
    extends Transformations with LocalTransformations with MockStoredTransformations

  "A new transformation" should "be applied" in {
    val local = LocalTransformation("test", "create", "")
    val pack = new TestTransformations(List(local))

    import pack.mocked._
    (findById _).when("test").returns(None)

    pack.run

    inSequence {
      (findById _).verify("test")
      (applyScript _).verify("create")
      (insert _).verify(local)
    }
  }

  "Disabled transformation" should "not be applied" in {
    val disabled = DisabledTransformation("test")
    val pack = new TestTransformations(List(disabled))

    import pack.mocked._
    (findById _).when("test").returns(None)

    pack.run

    (applyScript _).verify(*).never
    (insert _).verify(*).never
    (delete _).verify(*).never
    (update _).verify(*).never
  }

  "Disabled transformation" should "be rolled back if had been applied previously" in {
    val disabled = DisabledTransformation("test")
    val stored = StoredTransformation("test", "", "", "rollback", "")
    val pack = new TestTransformations(List(disabled))

    import pack.mocked._
    (findById _).when("test").returns(Some(stored))

    pack.run

    inSequence {
      (findById _).verify("test")
      (applyScript _).verify("rollback")
      (delete _).verify(stored)
    }
  }
  //
  //  "Locally removed transformation" should "be rolled back" in {
  //    val stored = StoredTransformation("test", "", "", "", "")
  //    val pack = new TransformationsPack(List(), List(stored))
  //
  //    pack.run
  //
  //    pack.mockApply.verify(*).never
  //    pack.mockUpdate.verify(*).never
  //    pack.mockRollback.verify(stored).once
  //  }
  //
  //  "Modified transformation" should "be rolled back and applied again" in {
  //    val local = LocalTransformation("test", "A", "")
  //    val stored = StoredTransformation("test", "", "", "", local.sqlRollbackHash)
  //    val pack = new TransformationsPack(List(local), List(stored))
  //
  //    pack.run
  //
  //    inSequence {
  //      pack.mockRollback.verify(stored).once
  //      pack.mockApply.verify(local).once
  //    }
  //    pack.mockUpdate.verify(*).never
  //  }
  //
  //  "On rollback script modification it" should "only update the stored transformation" in {
  //    val local = LocalTransformation("test", "", "A")
  //    val stored = StoredTransformation("test", "", local.sqlUpdateHash, "", "")
  //    val pack = new TransformationsPack(List(local), List(stored))
  //
  //    pack.run
  //
  //    pack.mockApply.verify(*).never
  //    pack.mockRollback.verify(*).never
  //    pack.mockUpdate.verify(local).once
  //  }
  //
  //  "Transformations" should "be applied in the given order" in {
  //    val local1 = LocalTransformation("test1", "", "")
  //    val local2 = LocalTransformation("test2", "", "")
  //    val local3 = LocalTransformation("test3", "", "")
  //    val stored1 = StoredTransformation("test2", "", "A", "", "")
  //    val stored2 = StoredTransformation("test4", "", "", "", "")
  //    val pack = new TransformationsPack(List(local3, local2, local1), List(stored1, stored2))
  //
  //    pack.run
  //
  //    inSequence {
  //      pack.mockApply.verify(local3).once
  //      pack.mockRollback.verify(stored1).once
  //      pack.mockApply.verify(local2).once
  //      pack.mockApply.verify(local1).once
  //      pack.mockRollback.verify(stored2).once
  //    }
  //  }

}
