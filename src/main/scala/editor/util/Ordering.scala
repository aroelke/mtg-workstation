package editor.util

class OptionOrdering[T : Ordering] extends math.Ordering.OptionOrdering[T] {
  override def optionOrdering = implicitly[Ordering[T]]
}

class SeqOrdering[T : Ordering] extends Ordering[Seq[T]] {
  override def compare(x: Seq[T], y: Seq[T]) = (x zip y).map(implicitly[Ordering[T]].compare).find(_ != 0).getOrElse(x.size.compare(y.size))
}