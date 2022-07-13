package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.MultiCard
import editor.database.card.SingleCard
import editor.filter.FaceSearchOptions
import editor.filter.Filter

import scala.reflect.ClassTag

/**
 * A filter that groups cards by a single attribute.
 * @tparam F type of filter this is
 * @author Alec Roelke
 */
trait FilterLeaf extends Filter {
  /** If the filter is not unified, which faces to consider when applying the filter. */
  def faces: FaceSearchOptions

  /** Whether or not the attribute applies to an entire card (true) or individually to its faces (false). */
  def unified: Boolean

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
}