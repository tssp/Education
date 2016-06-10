package recfun

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
  def pascal(c: Int, r: Int): Int =
    if (c == 0 || r == c) 1
    else if (c < 0 || r < 0) 0
    else pascal(c - 1, r - 1) + pascal(c, r - 1)

  /**
    * Exercise 2
    */
  def balance(chars: List[Char]): Boolean = {

    def balanceIter(remaining: List[Char], acc: Int): Boolean = {

      if (acc < 0) false
      else if (remaining.isEmpty && acc != 0) false
      else if (remaining.isEmpty && acc == 0) true
      else if (remaining.head == '(') balanceIter(remai
        ning.tail, acc + 1)
      else if (remaining.head == ')') balanceIter(remaining.tail, acc - 1)
      else balanceIter(remaining.tail, acc)
    }

    balanceIter(chars, 0)
  }


  /**
    * Exercise 3
    */
  def countChange(money: Int, coins: List[Int]): Int = {

    def countChangeIter(remainingMoney: Int, remainingCoins: List[Int]): Int = {

      if (remainingMoney == 0) 1
      else if (remainingMoney < 0 || remainingCoins.isEmpty) 0
      else countChangeIter(remainingMoney, remainingCoins.tail) + countChangeIter(remainingMoney - remainingCoins.head, remainingCoins)
    }

    if (money > 0) countChangeIter(money, coins) else 0
  }
}
