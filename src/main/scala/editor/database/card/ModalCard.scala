package editor.database.card

import CardLayout._

/**
 * A [[Card]] with two faces, either of which can be played, and doesn't typically switch until it
 * leaves the battlefield or stack. Its mana value is that of its front face.
 * 
 * @constructor create a new modal double-faced card
 * @param front front face of the card
 * @param back back face of the card
 * @author Alec Roelke
 */
@throws[IllegalArgumentException]("if both faces aren't modal double-faced cards")
class ModalCard(front: Card, back: Card) extends MultiCard(MODAL_DFC, IndexedSeq(front, back)) {
  if (front.layout != MODAL_DFC || back.layout != MODAL_DFC)
    IllegalArgumentException("can't join non-modal-double-faced cards into modal double-faced cards")

  override def manaValue = front.manaValue
  override def minManaValue = faces.map(_.manaValue).min
  override def maxManaValue = faces.map(_.manaValue).max
  override def avgManaValue = faces.map(_.manaValue).sum/faces.size
  override lazy val imageNames = front.imageNames ++ back.imageNames
}