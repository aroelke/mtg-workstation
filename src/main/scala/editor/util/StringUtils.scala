package editor.util

/**
 * Utilities for formatting strings.
 * @author Alec Roelke
 */
object StringUtils {
  /**
   * Format a number into a string with no decimal places if it's an integer or with a cutomizable precision otherwise.
   * 
   * @param n number to format
   * @param precision precision to use to format it if it isn't an integer
   * @return a string containing the formatted number
   */
  def formatDouble(n: Double, precision: Int) = {
    if (n == n.toInt)
      n.toInt.toString
    else
      s"%.${precision}f".format(n)
  }
}
