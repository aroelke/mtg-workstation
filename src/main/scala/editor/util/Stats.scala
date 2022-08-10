package editor.util

/**
 * Collection of methods to compute some statistical functions.
 * @author Alec Roelke
 */
object Stats {
  /**
   * Compute the factorial of an integer, or the product of all integers from 1 to that number (or 1
   * if the number is 0).
   * 
   * @param n number to compute the factorial of
   * @return n!, or the factorial of n; or 1 if n == 0
   */
  def factorial(n: Int) = {
    if (n < 0)
      throw IllegalArgumentException("negative factorial")
    else if (n == 0)
      BigInt(1)
    else
      (1 to n).map(BigInt(_)).product
  }

  /**
   * Compute "n choose k", or the number of combinations of n items out of a pool of k.
   * Computed by n!/((n-k)!*k!)
   * 
   * @param n number of items in sample
   * @param k number of items in pool
   * @return n choose k
   */
  def nchoosek(n: Int, k: Int) = {
    if (k == 0)
      BigInt(1)
    else if (n == 0)
      BigInt(0)
    else
      n.! / ((n - k).! * k.!)
  }

  /** Add "n choose k" and n! (as n.!) syntax to Int. */
  implicit class IntToNCK(n: Int) {
    def ! = Stats.factorial(n)
    def choose(k: Int) = Stats.nchoosek(n, k)
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
      (BigDecimal((count choose n)*((total - count) choose (hand - n)))/BigDecimal((total choose hand))).toDouble
  }
}