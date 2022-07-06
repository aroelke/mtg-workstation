package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.MultiCard
import editor.database.card.SingleCard
import editor.filter.FaceSearchOptions
import editor.filter.Filter

/**
 * A filter that groups cards by a single attribute.
 * 
 * @constructor create a new filter
 * @param t attribute to be filtered
 * @param unified whether or not the attribute applies to an entire card (true) or individually to its faces (false)
 * 
 * @author Alec Roelke
 */
abstract class FilterLeaf(t: CardAttribute[?], val unified: Boolean) extends Filter(t) {
  /** If the filter is not unified, which faces to consider when applying the filter. */
  var faces = FaceSearchOptions.ANY

  /**
   * Test a card face for its value of the attribute
   * 
   * @param c card face to test
   * @return true if the card's attribute passes the filter, and false otherwise.
   */
  protected def testFace(c: Card): Boolean

  final override def apply(c: Card) = if (unified) testFace(c) else c match {
    case s: SingleCard => testFace(c)
    case m: MultiCard => faces match {
      case FaceSearchOptions.ANY   => c.faces.exists(testFace)
      case FaceSearchOptions.ALL   => c.faces.forall(testFace)
      case FaceSearchOptions.FRONT => testFace(c.faces.head)
      case FaceSearchOptions.BACK  => testFace(c.faces.last)
    }
  }

  /** @return a copy of this filter, except for which faces to search. */
  protected def copyLeaf: FilterLeaf

  final override def copy = {
    val filter = copyLeaf
    filter.faces = faces
    filter
  }

  /** @return true if the values of this filter's fields are equal to the other's, and false otherwise. */
  protected def leafEquals(other: Any): Boolean

  override def equals(other: Any) = other match {
    case o: FilterLeaf => leafEquals(o) && o.faces == faces
    case _ => false
  }
}