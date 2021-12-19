package editor.gui.ccp.data

import java.awt.datatransfer.Transferable
import editor.database.card.Card
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import scala.jdk.CollectionConverters._

class CardTransferData(cards: Array[Card]) extends Transferable {
  @deprecated def this(cards: java.util.Collection[Card]) = this(cards.asScala.toArray)

  @throws[UnsupportedFlavorException]
  override def getTransferData(flavor: DataFlavor) = flavor match {
    case DataFlavors.cardFlavor => cards
    case DataFlavor.stringFlavor => cards.map(_.unifiedName).mkString("\n")
    case _ => throw UnsupportedFlavorException(flavor)
  }

  override val getTransferDataFlavors = Array(DataFlavors.cardFlavor, DataFlavor.stringFlavor)

  override def isDataFlavorSupported(flavor: DataFlavor) = getTransferDataFlavors.contains(flavor)
}