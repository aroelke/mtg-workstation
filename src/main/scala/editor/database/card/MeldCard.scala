package editor.database.card

import editor.database.attributes.ManaCost

import javax.swing.text.BadLocationException
import javax.swing.text.StyledDocument

import CardLayout.MELD

/**
 * One half of a meld card, which is a card that can combine with another during a game to form
 * another card represented by the back halves of the two combined cards. It knows about the other
 * card that forms the other half of the back, but doesn't display any information about it. The
 * mana value of a meld card is that of the front face only.
 * 
 * @constructor create a new meld card
 * @param front front face of the card
 * @param other front face of the other card that forms the back
 * @param back fully-formed back face of the two halves
 * @author Alec Roelke
 */
@throws[IllegalArgumentException]("if any of the faces aren't part of a meld card")
class MeldCard(front: Card, other: Card, back: Card) extends MultiCard(MELD, IndexedSeq(front, back)) {
  if (front.layout != MELD || other.layout != MELD || back.layout != MELD)
    throw IllegalArgumentException("can't join non-meld cards into meld cards")
  
  override def manaCost = front.manaCost

  override def manaValue = front.manaValue
  override def minManaValue = faces.map(_.manaValue).min
  override def maxManaValue = faces.map(_.manaValue).max
  override def avgManaValue = faces.map(_.manaValue).sum/faces.size
  override lazy val imageNames = front.imageNames ++ back.imageNames

  override def formatDocument(document: StyledDocument, printed: Boolean, f: Int) = {
    val reminderStyle = document.getStyle("reminder");
    super.formatDocument(document, printed, f);
    if (f == 0) {
      try {
          document.insertString(document.getLength(), s"\nMelds with ${other.name} (${other.expansion})", reminderStyle);
      } catch case e: BadLocationException => e.printStackTrace
    }
  }
}