package editor.util

object Stats {
  def factorial(n: Int) = {
    if (n < 0)
      throw IllegalArgumentException("negative factorial")
    else if (n == 0)
      BigInt(1)
    else
      (1 to n).map(BigInt(_)).product
  }

  def nchoosek(n: Int, k: Int) = {
    if (k == 0)
      BigInt(1)
    else if (n == 0)
      BigInt(0)
    else
      factorial(n)/(factorial(n - k)*factorial(k))
  }

  def hypergeometric(n: Int, hand: Int, count: Int, total: Int) = {
    if (hand - (total - count) > n)
      0.0
    else
      (BigDecimal(nchoosek(count, n)*nchoosek(total - count, hand - n))/BigDecimal(nchoosek(total, hand))).toDouble
  }
}