package suggestions



import language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}
import rx.lang.scala._
import org.scalatest._
import gui._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import rx.lang.scala.schedulers.IOScheduler


@RunWith(classOf[JUnitRunner])
class WikipediaApiTest extends FunSuite {

    object mockApi extends WikipediaApi {
    def wikipediaSuggestion(term: String) = Future {
      if (term.head.isLetter) {
        for (suffix <- List(" (Computer Scientist)", " (Footballer)")) yield term + suffix
      } else {
        List(term)
      }
    }
    def wikipediaPage(term: String) = Future {
      "Title: " + term
    }
  }

  import mockApi._

  test("WikipediaApi should make the stream valid using sanitized") {
    val notvalid = Observable.just("erik", "erik meijer", "martin")
    val valid = notvalid.sanitized

    var count = 0
    var completed = false

    val sub = valid.subscribe(
      term => {
        assert(term.forall(_ != ' '))
        count += 1
      },
      t => assert(false, s"stream error $t"),
      () => completed = true
    )
    assert(completed && count == 3, "completed: " + completed + ", event count: " + count)
  }
  test("WikipediaApi should correctly use concatRecovered") {
    val requests = Observable.just(1, 2, 3)
    val remoteComputation = (n: Int) => Observable.just(0 to n : _*)
    val responses = requests concatRecovered remoteComputation
    val sum = responses.foldLeft(0) { (acc, tn) =>
      tn match {
        case Success(n) => acc + n
        case Failure(t) => throw t
      }
    }
    var total = -1
    val sub = sum.subscribe {
      s => total = s
    }
    assert(total == (1 + 1 + 2 + 1 + 2 + 3), s"Sum: $total")
  }

  test("ConcatRecovered should recover correctly") {

    val l1 = List(1, 2).toObservable.concatRecovered(num => List(num, num).toObservable).toBlocking.toList

    assert(List(1, 1, 2, 2).map { Success(_) } == l1)

    val ex = new Exception

    val l2 = List(1, 2, 3).toObservable.concatRecovered(num => if (num == 2) Observable.error(ex) else Observable.just(num)).toBlocking.toList

    assert(List(Success(1), Failure(ex), Success(3)) == l2)
  }

  test("TimedOut should drop values after the timeout") {

    val o = List(1, 2, 3, 4).toObservable.zip(Observable.interval(600 millis, IOScheduler())).map(_._1) // emit every 600 millis
    val t = o.timedOut(2).toBlocking.toList

    assert(List(1, 2, 3) == t)
  }

}
