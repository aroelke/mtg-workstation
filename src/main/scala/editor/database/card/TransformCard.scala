package editor.database.card

import editor.database.attributes.ManaCost

import scala.jdk.CollectionConverters._

import CardLayout._

/**
 * A [[Card]] with two faces that can transform back and forth between them during a game. Its mana value
 * is that of its front face, but it has two mana costs: that of its front face, and an empty cost for
 * the back face.
 * 
 * @constructor create a new transforming card
 * @param front front of the card (what it is when off the battlefield)
 * @param back back of the card (what it can transform into)
 * 
 * @author Alec Roelke
 */
class TransformCard(front: Card, back: Card) extends MultiCard(TRANSFORM, front, back) {
  if (front.layout != TRANSFORM || back.layout != TRANSFORM)
    throw IllegalArgumentException("can't join non-transforming cards into transforming cards")

  override def manaValue = front.manaValue
  override lazy val manaCost = Seq(front.manaCost.get(0), ManaCost()).asJava
}