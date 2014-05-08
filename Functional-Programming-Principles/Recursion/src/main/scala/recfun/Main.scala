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

    /*
             1        1
            1 1       1 1
           1 2 1      1 2 1 
          1 3 3 1     1 3 3 1
         1 4 6 4 1    1 4 6 4 1
    */
    
    if(c == 0 || r == c) 1
    else if(c < 0 || r < 0) 0
    else pascal(c - 1, r - 1) +  pascal(c, r - 1)
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
