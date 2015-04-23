package calculator

sealed abstract class Expr
final case class Literal(v: Double) extends Expr
final case class Ref(name: String) extends Expr
final case class Plus(a: Expr, b: Expr) extends Expr
final case class Minus(a: Expr, b: Expr) extends Expr
final case class Times(a: Expr, b: Expr) extends Expr
final case class Divide(a: Expr, b: Expr) extends Expr

object Calculator {

  def computeValues(namedExpressions: Map[String, Signal[Expr]]): Map[String, Signal[Double]] = namedExpressions.map {
    case (name, expression) =>

      val computation = Var(0.0)

      computation() = {

        val expr = expression()

        eval(expr, namedExpressions)
      }

      (name, computation)
  }

  def eval(expr: Expr, references: Map[String, Signal[Expr]]): Double = {

    def nonCyclicEvaluation(ex: Expr, evaluated: List[String]): Double = ex match {

      case Literal(v)                            => v
      case Ref(name) if evaluated.contains(name) => Double.NaN
      case Ref(name)                             => nonCyclicEvaluation(getReferenceExpr(name, references), name :: evaluated)
      case Plus(a, b)                            => nonCyclicEvaluation(a, evaluated) + nonCyclicEvaluation(b, evaluated)
      case Minus(a, b)                           => nonCyclicEvaluation(a, evaluated) - nonCyclicEvaluation(b, evaluated)
      case Times(a, b)                           => nonCyclicEvaluation(a, evaluated) * nonCyclicEvaluation(b, evaluated)
      case Divide(a, b)                          => nonCyclicEvaluation(a, evaluated) / nonCyclicEvaluation(b, evaluated)
    }

    nonCyclicEvaluation(expr, Nil)
  }

  /**
   * Get the Expr for a referenced variables.
   *  If the variable is not known, returns a literal NaN.
   */
  private def getReferenceExpr(name: String,
                               references: Map[String, Signal[Expr]]) = {
    references.get(name).fold[Expr] {
      Literal(Double.NaN)
    } { exprSignal =>
      exprSignal()
    }
  }
}
