package editor.util

object Comparison {
  def valueOf(op: Char) = values.find(_.operation == op).getOrElse(throw IllegalArgumentException(s"unknown comparison $op"))
}

enum Comparison(private val operation: Char, comparison: (Int) => Boolean) {
  def apply[T](a: T, b: T)(implicit order: Ordering[T]) = comparison(order.compare(a, b))

  override def toString = operation.toString

  case EQ extends Comparison('=', _ == 0)
  case GE extends Comparison(UnicodeSymbols.GREATER_OR_EQUAL, _ >= 0)
  case GT extends Comparison('>', _ > 0)
  case LE extends Comparison(UnicodeSymbols.LESS_OR_EQUAL, _ <= 0)
  case LT extends Comparison('<', _ < 0)
  case NE extends Comparison(UnicodeSymbols.NOT_EQUAL, _ != 0)
}