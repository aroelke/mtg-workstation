package editor.gui.ccp.handler

import editor.collection.Categorization
import editor.gui.ccp.data.CategoryTransferData

import java.awt.Component
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.TransferHandler

/**
 * Handler for transferring [[Categorization]]s beween elements.
 * 
 * @constructor create a new category transfer handler with functions indicating how to transfer components
 * @param supplier function supplying the [[Categorization]] to transfer
 * @param contains predicate indicating if a category is already contained by the target element
 * @param add function specifying how to add the [[Categorization]] to the target element
 * @param remove function specifying how to remove the [[Categorization]] from the source element if it is to be moved rather than copied
 * 
 * @author Alec Roelke
 */
class CategoryTransferHandler(supplier: () => Categorization, contains: (Categorization) => Boolean, add: (Categorization) => Boolean, remove: (Categorization) => Unit)
    extends EditorTransferHandler(Seq(CategoryImportHandler(contains, add))) {
  override def getSourceActions(c: JComponent) = TransferHandler.COPY_OR_MOVE
  override def createTransferable(c: JComponent) = CategoryTransferData(supplier())
  override def exportDone(source: JComponent, data: Transferable, action: Int) = if (action == TransferHandler.MOVE) data match {
    case c: CategoryTransferData => remove(c.spec)
    case x => throw ClassCastException(s"expected ${classOf[CategoryTransferData]}, got ${x.getClass}")
  }
}
