package editor.gui.ccp.handler

import editor.collection.CardListEntry
import editor.gui.ccp.data.EntryTransferData
import editor.gui.deck.EditorFrame

import java.awt.datatransfer.Transferable
import java.time.LocalDate
import javax.swing.JComponent
import javax.swing.TransferHandler
import scala.collection.immutable.ListMap

/**
 * Handler for transferring data between [[CardTable]]s in an [[EditorFrame]].
 * 
 * @constructor create a new transfer handler for a particular table in a particular frame
 * @param editor frame containing the table to transfer to or from
 * @param id ID of the list/table to transfer to or from
 * 
 * @author Alec Roelke
 */
class EditorTableTransferHandler(editor: EditorFrame, id: Int) extends EditorFrameTransferHandler(editor, id) {
  override def createTransferable(c: JComponent) = {
    val data = editor.getSelectedCards.map((e) => e.card -> e.count).toMap
    EntryTransferData(editor, id, data)
  }

  override def getSourceActions(c: JComponent) = TransferHandler.COPY_OR_MOVE

  override def exportDone(source: JComponent, data: Transferable, action: Int) = data match {
    case d: EntryTransferData => action match {
      case TransferHandler.MOVE =>
        if (d.source == d.target)
          d.source.lists(d.from).move(d.cards)(d.source.lists(d.to))
        else {
          d.source.lists(d.from) --= d.cards.map{ case (c, i) => CardListEntry(c, i) }
          if (d.target != null)
            d.target.lists(d.to) ++= d.cards.map{ case (c, i) => CardListEntry(c, i) }
        }
      case TransferHandler.COPY =>
        if (d.target != null)
          d.target.lists(d.to) ++= d.cards.map{ case (c, i) => CardListEntry(c, i) }
      case _ =>
    }
    case _ =>
  }
}