package editor

/**
 * Package containing extensions for Scala's math library.
 * @author Alec Roelke
 */
package object math {
  /**
   * Provides an ordering for [[Option]] instances containing values with [[Ordering]]s.  Always sorts missing values last based on
   * the provided sort ordering.
   * 
   * @param ascending true if sorting in ascending order, or false otherwise
   */
  given OptionOrdering[T : Ordering](using ascending: Boolean = true): Ordering[Option[T]] with {
    override def compare(x: Option[T], y: Option[T]) = (x, y) match {
      case (Some(a), Some(b)) => summon[Ordering[T]].compare(a, b)
      case (Some(_), None) => if (ascending) -1 else 1
      case (None, Some(_)) => if (ascending) 1 else -1
      case (None, None) => 0
    }
  }

  /** Provides an ordering for [[Seq]] instances containing values with [[Ordering]]s. */
  given SeqOrdering[T : Ordering]: Ordering[Seq[T]] with {
    override def compare(x: Seq[T], y: Seq[T]) = (x zip y).map(summon[Ordering[T]].compare).find(_ != 0).getOrElse(x.size.compare(y.size))
  }
}