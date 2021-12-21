package editor.gui.ccp.data

import editor.collection.deck.Category
import editor.database.card.Card

import java.awt.datatransfer.DataFlavor

/**
 * Collection of data flavors used for copying data.
 * @author Alec Roelke
 */
object DataFlavors {
  /** Data flavor used for copying cards, typically from the inventory panel. */
  lazy val cardFlavor = DataFlavor(s"${DataFlavor.javaJVMLocalObjectMimeType};class=\"${classOf[Array[Card]].getName}\"", "Card Array")

  /** Data flavor used for copying cards and metadata, typically from a deck table. */
  lazy val entryFlavor = DataFlavor(s"${DataFlavor.javaJVMLocalObjectMimeType};class=\"${classOf[EntryTransferData].getName}\"", "Deck Entries")

  /** Data flavor used for copying categorizations and (TBD) cards in catgories. */
  lazy val categoryFlavor = DataFlavor(s"${DataFlavor.javaJVMLocalObjectMimeType};class=\"${classOf[Category].getName}\"", "Categorization")
}