package editor

/**
 * Collection of methods to compute some statistical functions.
 * @author Alec Roelke
 */
package object stats {
  /** Extensions adding commonly-used stat functions to integers */
  object extensions {
    extension (n: BigInt) {
      /** @return the factorial of the integer. */
      def ! = factorial(n)

      /**
       * Compute the factorial of an integer divided by another integer.
       * 
       * @param m the number to divide by
       * @return the result of the operation.
       */
      def !/(m: BigInt) = factorial(n)/m

      /**
       * Compute the factorial of an integer multiplied by another integer.
       * 
       * @param m the number to multiply by
       * @return the result of the operation.
       */
      def !*(m: BigInt) = factorial(n)*m

      /** @return the number of combinations of this number items out of k possible items. */
      def choose(k: BigInt) = nchoosek(n, k)
    }
  }
  import extensions._

  /**
   * Compute the factorial of an integer, or the product of all integers from 1 to that number (or 1
   * if the number is 0).
   * 
   * @param n number to compute the factorial of
   * @return n!, or the factorial of n; or 1 if n == 0
   */
  def factorial(n: BigInt) = {
    if (n < 0)
      throw IllegalArgumentException("negative factorial")
    else if (n == 0)
      BigInt(1)
    else
      (BigInt(1) to n).product
  }

  /**
   * Compute "n choose k", or the number of combinations of n items out of a pool of k.
   * Computed by n!/((n-k)!*k!)
   * 
   * @param n number of items in sample
   * @param k number of items in pool
   * @return n choose k
   */
  def nchoosek(n: BigInt, k: BigInt) = {
    if (k == 0)
      BigInt(1)
    else if (n == 0)
      BigInt(0)
    else
      n!/((n - k)!*(k.!))
  }

  /**
   * Compute a hypergeometric distribution, which is the probability of k successes in n draws out of a pool of N items, K of which are successes.
   * 
   * @param n desired number of successes (k)
   * @param hand number of draws (n)
   * @param count number of successes in pool (K)
   * @param total size of pool (N)
   * @return the probability of drawing n desired cards in hand of the given size out of a deck of size total with count desired cards in it
   */
  def hypergeometric(n: Int, hand: Int, count: Int, total: Int) = {
    if (hand - (total - count) > n)
      0.0
    else
      (BigDecimal((count choose n)*((total - count) choose (hand - n)))/BigDecimal(total choose hand)).toDouble
  }
}