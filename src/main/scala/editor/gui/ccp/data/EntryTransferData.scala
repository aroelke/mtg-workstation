package editor.gui.ccp.data

import editor.gui.editor.EditorFrame
import editor.database.card.Card
import java.awt.datatransfer.DataFlavor
import scala.jdk.CollectionConverters._
import java.awt.datatransfer.UnsupportedFlavorException

class EntryTransferData(val source: EditorFrame, val from: Int, val cards: Map[Card, Int]) extends CardTransferData(cards.keys.toSeq.sortBy(_.unifiedName).toArray) {
  @deprecated def this(source: EditorFrame, from: Int, cards: java.util.Map[Card, Integer]) = this(source, from, cards.asScala.map{ case (c, i) => c -> i.toInt }.toMap)

  var target: EditorFrame = null
  var to = -1

  @deprecated def entries = cards.map{ case (c, i) => c -> Integer(i) }.asJava

  @throws[UnsupportedFlavorException]
  override def getTransferData(flavor: DataFlavor) = if (flavor == DataFlavors.entryFlavor) this else super.getTransferData(flavor)
  override def getTransferDataFlavors = DataFlavors.entryFlavor +: super.getTransferDataFlavors
  override def isDataFlavorSupported(flavor: DataFlavor) = getTransferDataFlavors.contains(flavor)
}