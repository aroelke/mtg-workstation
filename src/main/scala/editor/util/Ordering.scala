package editor.util

implicit class OptionOrdering[T : Ordering](ascending: Boolean = true) extends Ordering[Option[T]] {
  override def compare(x: Option[T], y: Option[T]) = (x, y) match {
    case (Some(a), Some(b)) => summon[Ordering[T]].compare(a, b)
    case (Some(_), None) => if (ascending) -1 else 1
    case (None, Some(_)) => if (ascending) 1 else -1
    case (None, None) => 0
  }
}

implicit class SeqOrdering[T : Ordering] extends Ordering[Seq[T]] {
  override def compare(x: Seq[T], y: Seq[T]) = (x zip y).map(summon[Ordering[T]].compare).find(_ != 0).getOrElse(x.size.compare(y.size))
}