package editor.database.attributes

import editor.util.UnicodeSymbols

import scala.collection.AbstractIterable
import scala.collection.immutable.ListSet

/**
 * The set of all types (card types, supertypes, and subtypes) on a face of a card. Use [[toString]]
 * to convert to the canonical type line as it appears on a card's Oracle text.
 * 
 * @constructor create a new type line
 * @param types card types of the card face; must have at least one item
 * @param subtypes subtypes of the card face, if any
 * @param supertypes supertypes of the card face, if any
 * 
 * @author Alec Roelke
 */
final case class TypeLine(types: ListSet[String], subtypes: ListSet[String] = ListSet.empty, supertypes: ListSet[String] = ListSet.empty) extends AbstractIterable[String] with Ordered[TypeLine] {
  if (types.isEmpty)
    throw IllegalArgumentException("a card must have at least one type")

  private lazy val ordered = supertypes ++ types ++ subtypes

  /**
   * @param s type to test for; is case sensitive
   * @return true if the type line contains the given type, including case, and false otherwise
   */
  def contains(s: String) = types.contains(s) || subtypes.contains(s) || supertypes.contains(s)
  
  /**
   * @param s type to test for, ignoring case
   * @return true if the type line contains the given type, ignoring case, and false otherwise
   */
  def containsIgnoreCase(s: String) = ordered.exists(_.equalsIgnoreCase(s))

  /**
   * Create a new type line containing all the types of this one followed by the types of another.
   * 
   * @param that type line to add
   * @return a new type line with all the types of this one and that one
   */
  def concat(that: TypeLine) = TypeLine(types ++ that.types, subtypes ++ that.subtypes, supertypes ++ that.supertypes)

  /** Alias for [[concat]]. */
  def ++(that: TypeLine) = concat(that)

  override def iterator = ordered.iterator

  override def compare(that: TypeLine) = {
    if (types != that.types)
      types.mkString.compare(that.types.mkString)
    else if (supertypes != that.supertypes)
      supertypes.mkString.compare(that.supertypes.mkString)
    else
      subtypes.mkString.compare(that.subtypes.mkString)
  }

  override lazy val toString = s"""${if (supertypes.isEmpty) "" else supertypes.mkString("", " ", " ")}${types.mkString(" ")}${if (subtypes.isEmpty) "" else subtypes.mkString(s" ${UnicodeSymbols.EmDash} ", " ", "")}"""
}