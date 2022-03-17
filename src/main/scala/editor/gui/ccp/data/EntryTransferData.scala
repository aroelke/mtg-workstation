package editor.gui.ccp.data

import editor.database.card.Card
import editor.gui.editor.EditorFrame

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import scala.jdk.CollectionConverters._

/**
 * Cards being transferred from a card list to another element.
 * 
 * @constructor create a new set of transfer data for a specific list within a frame
 * @param source source frame containing the list to transfer from
 * @param from ID of the list to transfer from
 * @param cards cards and counts to transfer
 * 
 * @author Alec Roelke
 */
class EntryTransferData(val source: EditorFrame, val from: Int, val cards: Map[Card, Int]) extends CardTransferData(cards.keys.toSeq.sortBy(_.name).toArray) {
  @deprecated def this(source: EditorFrame, from: Int, cards: java.util.Map[Card, Integer]) = this(source, from, cards.asScala.map{ case (c, i) => c -> i.toInt }.toMap)

  /** Target frame containing the list to transfer to. */
  var target: EditorFrame = null
  /** ID of the list to transfer to. */
  var to = -1

  @deprecated def entries = cards.map{ case (c, i) => c -> Integer(i) }.asJava

  @throws[UnsupportedFlavorException]
  override def getTransferData(flavor: DataFlavor) = if (flavor == DataFlavors.entryFlavor) this else super.getTransferData(flavor)
  override def getTransferDataFlavors = DataFlavors.entryFlavor +: super.getTransferDataFlavors
  override def isDataFlavorSupported(flavor: DataFlavor) = getTransferDataFlavors.contains(flavor)
}