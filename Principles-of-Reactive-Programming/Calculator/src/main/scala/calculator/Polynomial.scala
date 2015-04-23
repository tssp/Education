package calculator

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double], c: Signal[Double]): Signal[Double] = {

    val delta = Var(0.0)

    delta() = {

      // Δ = b² - 4ac

      val aa = a()
      val bb = b()
      val cc = c()

      (bb * bb) - (4 * aa * cc)
    }

    delta
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double], c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {

    val roots = Var(Set.empty[Double])

    roots() = {

      // (-b ± √Δ) / (2a)

      val dd = delta()
      val aa = a()
      val bb = b()
      val cc = c()

      if (dd < 0)
        Set(0)
      else {

        val r1 = ((bb * -1) + Math.sqrt(dd)) / (2 * aa)
        val r2 = ((bb * -1) - Math.sqrt(dd)) / (2 * aa)

        Set(r1, r2)

      }

    }

    roots
  }
}
