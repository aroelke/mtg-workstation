package editor.database.card

import scala.jdk.CollectionConverters._

import CardLayout.FLIP

/**
 * A card that effectively has two faces, but both are printed on different ends of
 * the same side of the physical card. During a game, the card is played using the top
 * side and then eventually may rotate to become the bottom side. Both sides have the
 * mana value printed on the top side.
 * 
 * @constructor create a new flip card
 * @param top top side of the card
 * @param bottom bottom side of the card
 * @author Alec Roelke
 */
@throws[IllegalArgumentException]("if top or bottom isn't part of a flip card")
class FlipCard(top: Card, bottom: Card) extends MultiCard(FLIP, top, bottom) {
  if (top.layout != FLIP && bottom.layout != FLIP)
    throw IllegalArgumentException("can't join non-flip cards into flip cards")
  
  override lazy val manaCost = Seq.fill(2)(top.manaCost.get(0)).asJava
  override def manaValue = top.manaValue
}