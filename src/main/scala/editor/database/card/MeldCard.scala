package editor.database.card

import editor.database.attributes.ManaCost

import javax.swing.text.BadLocationException
import javax.swing.text.StyledDocument
import scala.jdk.CollectionConverters._

import CardLayout.MELD

/**
 * One half of a meld card, which is a card that can combine with another during a game to form
 * another card represented by the back halves of the two combined cards. It knows about the other
 * card that forms the other half of the back, but doesn't display any information about it. The
 * mana value of a meld card is that of the front face only.
 */
class MeldCard(front: Card, other: Card, back: Card) extends MultiCard(MELD, front, back) {
  if (front.layout != MELD || other.layout != MELD || back.layout != MELD)
    throw IllegalArgumentException("can't join non-meld cards into meld cards")
  
  override lazy val manaCost = Seq(front.manaCost.get(0), ManaCost()).asJava

  override def manaValue = front.manaValue

  override def formatDocument(document: StyledDocument, printed: Boolean, f: Int) = {
    val reminderStyle = document.getStyle("reminder");
    super.formatDocument(document, printed, f);
    if (f == 0) {
      try {
          document.insertString(document.getLength(), s"\nMelds with ${other.unifiedName()} (${other.expansion})", reminderStyle);
      } catch case e: BadLocationException => e.printStackTrace
    }
  }
}