package funsets

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * This class is a test suite for the methods in object FunSets. To run
 * the test suite, you can either:
 *  - run the "test" command in the SBT console
 *  - right-click the file in eclipse and chose "Run As" - "JUnit Test"
 */
@RunWith(classOf[JUnitRunner])
class FunSetSuite extends FunSuite {


  /**
   * Link to the scaladoc - very clear and detailed tutorial of FunSuite
   *
   * http://doc.scalatest.org/1.9.1/index.html#org.scalatest.FunSuite
   *
   * Operators
   *  - test
   *  - ignore
   *  - pending
   */

  /**
   * Tests are written using the "test" operator and the "assert" method.
   */
  test("string take") {
    val message = "hello, world"
    assert(message.take(5) == "hello")
  }

  /**
   * For ScalaTest tests, there exists a special equality operator "===" that
   * can be used inside "assert". If the assertion fails, the two values will
   * be printed in the error message. Otherwise, when using "==", the test
   * error message will only say "assertion failed", without showing the values.
   *
   * Try it out! Change the values so that the assertion fails, and look at the
   * error message.
   */
  test("adding ints") {
    assert(1 + 2 === 3)
  }

  
  import FunSets._

  /**
   * When writing tests, one would often like to re-use certain values for multiple
   * tests. For instance, we would like to create an Int-set and have multiple test
   * about it.
   * 
   * Instead of copy-pasting the code for creating the set into every test, we can
   * store it in the test class using a val:
   * 
   *   val s1 = singletonSet(1)
   * 
   * However, what happens if the method "singletonSet" has a bug and crashes? Then
   * the test methods are not even executed, because creating an instance of the
   * test class fails!
   * 
   * Therefore, we put the shared values into a separate trait (traits are like
   * abstract classes), and create an instance inside each test method.
   * 
   */

  trait TestSets {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)

    val s0_25 = (e:Int) => e>=0 && e<=25
    val s20_35 = (e:Int) => e>=20 && e<=35
    val s50_100 = (e:Int) => e>=50 && e<=100
  }
    
  /**
   * We create a new instance of the "TestSets" trait, this gives us access
   * to the values "s1" to "s3". 
   */
  new TestSets {
    /**
     * The string argument of "assert" is a message that is printed in case
     * the test fails. This helps identifying which assertion failed.
     */
    assert(contains(s1, 1), "Singleton")
  }

  test("union is implemented") {

    new TestSets {
      val s = union(s1, s2)
      assert(contains(s, 1), "Union 1")
      assert(contains(s, 2), "Union 2")
      assert(!contains(s, 3), "!Union 3")
    }
  }

  test("intersect is implemented") {

    new TestSets {

      val s = intersect(s0_25, s20_35)

      assert(contains(s, 20), "Intersect 20")
      assert(!contains(s, 30), "Intersect 30")
    }
  }

  test("diff is implemented") {

    new TestSets {

      val s = diff(s0_25, s20_35)

      assert(contains(s, 19))
      assert(contains(s, 0))
      assert(!contains(s, 25))

    }
  }

  test("filter is implemented") {

    new TestSets {

      val s = filter(s0_25, (e:Int) => e>=10 && e<=12)

      assert(contains(s, 10))
      assert(contains(s, 12))
      assert(!contains(s, 8))
      assert(!contains(s, 13))
    }
  }


  test("forall is implemented") {

    new TestSets {

      assert(forall(s20_35, (x:Int) => x>0))

    }
  }

  test("exists is implemented") {

    new TestSets {

      assert(exists(s20_35, (x:Int) => x==21))
      assert(!exists(s20_35, (x:Int) => x==12))
    }
  }
/**
   * Returns a set transformed by applying `f` to each element of `s`.
   */
  test("map is implemented") {

    new TestSets {

      val s = map(s1, (x:Int) => x+5)

      assert(contains(s, 6))
    }
  }
}
