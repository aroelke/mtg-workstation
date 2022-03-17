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
class ModalCard(front: Card, back: Card) extends MultiCard(MODAL_DFC, Seq(front, back)) {
  if (front.layout != MODAL_DFC || back.layout != MODAL_DFC)
    IllegalArgumentException("can't join non-modal-double-faced cards into modal double-faced cards")

  override lazy val manaValue = front.manaValue
  override lazy val imageNames = front.imageNames ++ back.imageNames
}