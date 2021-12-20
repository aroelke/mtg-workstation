package editor.gui.ccp.data

import editor.database.card.Card

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import scala.jdk.CollectionConverters._

/**
 * Data that can be transferred from an [[Inventory]] via drag-and-drop or cut/copy/paste. Supports transferring to
 * other elements that can accept [[Card]]s or to strings.
 * 
 * @constructor create a new set of card transfer data from an array of cards
 * @param cards cards to be transferred
 *
 * @author Alec Roelke
 */
class CardTransferData(cards: Array[Card]) extends Transferable {
  @throws[UnsupportedFlavorException]
  override def getTransferData(flavor: DataFlavor) = flavor match {
    case DataFlavors.cardFlavor => cards
    case DataFlavor.stringFlavor => cards.map(_.unifiedName).mkString("\n")
    case _ => throw UnsupportedFlavorException(flavor)
  }

  override def getTransferDataFlavors = Array(DataFlavors.cardFlavor, DataFlavor.stringFlavor)

  override def isDataFlavorSupported(flavor: DataFlavor) = getTransferDataFlavors.contains(flavor)
}