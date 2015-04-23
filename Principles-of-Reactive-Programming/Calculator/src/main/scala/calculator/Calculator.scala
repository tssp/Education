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

        evalNonCyclic(expr, namedExpressions, Nil)
      }

      (name, computation)
  }

  def eval(expr: Expr, references: Map[String, Signal[Expr]]): Double = expr match {

    case Literal(v)   => v
    case Ref(name)    => eval(getReferenceExpr(name, references), references)
    case Plus(a, b)   => eval(a, references) + eval(b, references)
    case Minus(a, b)  => eval(a, references) - eval(b, references)
    case Times(a, b)  => eval(a, references) * eval(b, references)
    case Divide(a, b) => eval(a, references) / eval(b, references)
  }

  def evalNonCyclic(expr: Expr, references: Map[String, Signal[Expr]], evaluatedNames: List[String]): Double = expr match {

    case Literal(v)                                 => v
    case Ref(name) if evaluatedNames.contains(name) => Double.NaN
    case Ref(name)                                  => evalNonCyclic(getReferenceExpr(name, references), references, name :: evaluatedNames)
    case Plus(a, b)                                 => evalNonCyclic(a, references, evaluatedNames) + evalNonCyclic(b, references, evaluatedNames)
    case Minus(a, b)                                => evalNonCyclic(a, references, evaluatedNames) - evalNonCyclic(b, references, evaluatedNames)
    case Times(a, b)                                => evalNonCyclic(a, references, evaluatedNames) * evalNonCyclic(b, references, evaluatedNames)
    case Divide(a, b)                               => evalNonCyclic(a, references, evaluatedNames) / evalNonCyclic(b, references, evaluatedNames)
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
