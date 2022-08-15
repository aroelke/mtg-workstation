package editor.util

import editor.unicode._

object Comparison {
  def valueOf(op: Char) = values.find(_.operation == op).getOrElse(throw IllegalArgumentException(s"unknown comparison $op"))
}

/**
 * A logical comparison between two values. Relies on the behavior of [[Ordering.compare]] that the return value
 * is < 0 if a < b, is 0 if a = b, and > 0 if a > b.
 * 
 * @constructor create a new type of comparison
 * @param operation character representing the operation (such as = or <)
 * @param comparison function indicating how to interpret the result of a comparison
 * 
 * @author Alec Roelke
 */
enum Comparison(private val operation: Char, comparison: (Int) => Boolean) {
  /**
   * Interpret the result of comparing two values.
   * 
   * @param a first value to compare
   * @param b second value to compare
   * @return true if the comparison has the desired result, and false otherwise
   */
  def apply[T](a: T, b: T)(using order: Ordering[T]) = comparison(order.compare(a, b))

  override def toString = operation.toString

  /** Evaluates to true if both values are equal. */
  case EQ extends Comparison('=', _ == 0)
  /** Evaluates to true if the values are equal or the first is greater. */
  case GE extends Comparison(GreaterOrEqual, _ >= 0)
  /** Evaluates to true if the first value is greater. */
  case GT extends Comparison('>', _ > 0)
  /** Evaluates to true if the first value is less or both are equal. */
  case LE extends Comparison(LessOrEqual, _ <= 0)
  /** Evaluates to true if the first value is less. */
  case LT extends Comparison('<', _ < 0)
  /** Evaluates to true if the two values are not equal. */
  case NE extends Comparison(NotEqual, _ != 0)
}