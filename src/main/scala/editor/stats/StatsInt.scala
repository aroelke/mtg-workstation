package editor.stats

/**
 * Enhancement of [[Int]] with methods used for statistical calculations.
 * 
 * @param n value of the integer
 * @author Alec Roelke
 */
class StatsInt(n: Int) {
  /** @return the factorial of the integer. */
  def ! = factorial(n)

  /** @return the number of combinations of this number items out of k possible items. */
  def choose(k: Int) = nchoosek(n, k)
}