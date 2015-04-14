package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {


  lazy val genHeap: Gen[H] = for {
    v <- arbitrary[Int]
    h <- oneOf(const(empty), genHeap)
  } yield insert(v, h)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  val nonEmptyHeaps = genHeap suchThat (!isEmpty(_))

  property("single min") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  property("delete min") = forAll { a: Int =>
    val h = deleteMin(insert(a, empty))

    isEmpty(h)
  }

  property("insert non-empty") = forAll(nonEmptyHeaps, arbitrary[Int]) { (h: H, v:Int) =>

    val min= Math.min(findMin(h), v)

    findMin(insert(v, h)) == min
  }


  property("dual min") = forAll { (a: Int, b:Int) =>

    val h = insert(b, insert(a, empty))

    val min= Math.min(a, b)

    findMin(h) == min
  }

  property("melding non-empty min") = forAll(nonEmptyHeaps, nonEmptyHeaps) { (h1: H, h2:H) =>

    val h = meld(h1, h2)

    val min= Math.min(findMin(h1), findMin(h2))

    findMin(h) == min
  }



  property("comparing non-empty min") = forAll(nonEmptyHeaps, nonEmptyHeaps) { (h1: H, h2:H) =>

    def compareHeaps(hh1: H, hh2: H): Boolean =
      if(isEmpty(hh1) && isEmpty(hh2)) true
      else findMin(hh1) == findMin(hh2) && compareHeaps(deleteMin(hh1), deleteMin(hh2))

    compareHeaps(meld(h1, h2), meld(deleteMin(h1), insert(findMin(h1), h2)))
  }

  property("non-empty sorted") = forAll(nonEmptyHeaps) { h: H =>

    def heap2list(h: H):List[Int] =
      if(isEmpty(h)) Nil
      else findMin(h)::heap2list(deleteMin(h))

    val xs = heap2list(h)

    xs.sorted == xs
  }

}
