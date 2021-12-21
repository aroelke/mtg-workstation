package editor.gui.ccp.data

import java.awt.datatransfer.DataFlavor
import editor.database.card.Card
import editor.collection.deck.Category

object DataFlavors {
  lazy val cardFlavor = DataFlavor(s"${DataFlavor.javaJVMLocalObjectMimeType};class=\"${classOf[Array[Card]].getName}\"", "Card Array")

  lazy val entryFlavor = DataFlavor(s"${DataFlavor.javaJVMLocalObjectMimeType};class=\"${classOf[EntryTransferData].getName}\"", "Deck Entries")

  lazy val categoryFlavor = DataFlavor(s"${DataFlavor.javaJVMLocalObjectMimeType};class=\"${classOf[Category].getName}\"", "Categorization")
}
