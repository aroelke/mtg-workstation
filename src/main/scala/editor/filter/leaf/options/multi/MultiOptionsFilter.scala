package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * A type of filter that groups cards by an attribute that can have zero or more of a set of distinct vales.
 * 
 * @constructor create a new multi-item options filter
 * @param t attribute to be filtered by
 * @param multifunction function to use to get the value of the attribute from a card
 * @tparam T type of the data that is being filtered
 */
abstract class MultiOptionsFilter[T](t: CardAttribute, protected val multifunction: (Card) => Set[T]) extends OptionsFilter[T](t, null) {
  override protected def testFace(c: Card) = contain.test(multifunction(c).asJava, selected)

  override def hashCode = Objects.hash(`type`, function, contain, selected)
}