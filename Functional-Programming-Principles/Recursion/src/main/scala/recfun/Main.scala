package recfun
import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {

    require(c >= 0, "Column must be a positive value")
    require(r >= 0, "Row must be a positive value")
    require(c <= r, "Column must be less or equal to the row number")

    def pascalIter(currentRow: Int, line: Array[Int]): Int = {

      if(currentRow == r) line(c)
      else {

        val colsPerLine = line.size + 1
        val newLine = new Array[Int](colsPerLine)

        newLine(0)=1
        newLine(colsPerLine-1)=1

        for(i <- 0 until line.size - 1) {

          newLine(1+i) = line(i) + line(i+1)
        }

        pascalIter(currentRow+1, newLine)
      }
    }

    pascalIter(0, Array(1))
  }

  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean = ???

  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int = ???
}
