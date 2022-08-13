package editor.util

/**
 * Global data about [[Containment]].
 * @author Alec Roelke
 */
object Containment {
  /**
   * Parse a [[Containment]] from a string using its string representation rather than its name.
   * 
   * @param s string to parse
   * @return the [[Containment]] whose representation matches the given string, ignoring case, or None if there isn't one
   */
  def parse(s: String) = Containment.values.find(_.toString.equalsIgnoreCase(s))
}

/**
 * A function for testing how the elements of a collection are contained in another.
 * 
 * @param toString string representing the containment test
 * @author Alec Roelke
 */
enum Containment(override val toString: String) extends ((Iterable[?], Iterable[?]) => Boolean) {
  override def apply(a: Iterable[?] , b: Iterable[?]) = this match {
    case AllOf => val aSet = a.toSet; b.isEmpty || b.forall(aSet.contains)
    case AnyOf => val aSet = a.toSet; b.isEmpty || b.exists(aSet.contains)
    case Exactly =>
      def counts(it: Iterable[?]) = it.groupBy(identity).map{ case (x, xs) => x -> xs.size }
      counts(a) == counts(b)
    case NoneOf => !AnyOf(a, b)
    case SomeOf => !AllOf(a, b)
    case NotExactly => !Exactly(a, b)
  }

  /** The first collection contains all elements of the second. */
  case AllOf      extends Containment("contains all of")
  /** The first collection contains any element in the second. */
  case AnyOf      extends Containment("contains any of")
  /** The first collection and the second have the same elements in the same amounts. */
  case Exactly    extends Containment("contains exactly")
  /** The first collection contains no elements in the second. */
  case NoneOf     extends Containment("contains none of")
  /** The first collection contains some, but not all, of the elements in the second. */
  case SomeOf     extends Containment("contains not all of")
  /** The first collection may contian elements in the second but not exactly the same in the same amounts. */
  case NotExactly extends Containment("contains not exactly")
}