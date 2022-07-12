package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

import scala.reflect.ClassTag

/**
 * A type of filter that groups cards by an attribute that can have zero or more of a set of distinct vales.
 * @tparam T type of the data that is being filtered
 * @author Alec Roelke
 */
trait MultiOptionsFilter[T, F <: MultiOptionsFilter[T, ?] : ClassTag] extends OptionsFilter[T, F] {
  /** Function to use to get the values of the attribute from a card. */
  def values: (Card) => Set[T]
  override protected def testFace(c: Card) = contain(values(c), selected)
}