package editor.gui.ccp.handler

import editor.collection.deck.Category
import java.awt.Component
import javax.swing.TransferHandler
import javax.swing.JComponent
import editor.gui.ccp.data.CategoryTransferData
import java.awt.datatransfer.Transferable

class CategoryTransferHandler(supplier: () => Category, contains: (Category) => Boolean, add: (Category) => Boolean, remove: (Category) => Unit) extends EditorTransferHandler(CategoryImportHandler(contains, add)) {
  override def getSourceActions(c: JComponent) = TransferHandler.MOVE | TransferHandler.COPY
  override def createTransferable(c: JComponent) = CategoryTransferData(supplier())
  override def exportDone(source: JComponent, data: Transferable, action: Int) = if (action == TransferHandler.MOVE) data match {
    case c: CategoryTransferData => remove(c.spec)
    case x => throw ClassCastException(s"expected ${classOf[CategoryTransferData]}, got ${x.getClass}")
  }
}
