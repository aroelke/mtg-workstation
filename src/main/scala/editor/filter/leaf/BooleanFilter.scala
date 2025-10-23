package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions

/**
  * Filter the groups cards by a boolean attribute.
  *
  * @param value function retrieving the value of the boolean attribute from a card
  * @param yes true if the card should have the attribute and false otherwise
  */
final case class BooleanFilter(attribute: CardAttribute[Boolean, BooleanFilter], value: (Card) => Boolean, faces: FaceSearchOptions = FaceSearchOptions.ANY, unified: Boolean = true, yes: Boolean = false) extends FilterLeaf {
  override protected def testFace(c: Card) = c.isGameChanger
}