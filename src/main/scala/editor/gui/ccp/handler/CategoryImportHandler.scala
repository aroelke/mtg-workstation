package editor.gui.ccp.handler

import editor.collection.Categorization
import editor.gui.ccp.data.CategoryTransferData
import editor.gui.ccp.data.DataFlavors

import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import javax.swing.TransferHandler

/**
 * Import handler for [[Categorization]]s.  Used only for cut/copy/paste; does not support drag-and-drop.
 * 
 * @constructor create a new category import handler that processes categories with arbitrary predicates
 * @param contains predicate indicating if the category is already contained by the target element; duplicate categories
 * are not allowed
 * @param add function for adding the category to the element and returning true on success and false otherwise
 * 
 * @author Alec Roelke
 */
class CategoryImportHandler(contains: (Categorization) => Boolean, add: (Categorization) => Boolean) extends TransferHandler with ImportHandler {
  @deprecated def this(contains: java.util.function.Predicate[Categorization], add: java.util.function.Predicate[Categorization]) = this(contains.test, add.test)

  override def supportedFlavor = DataFlavors.categoryFlavor

  override def canImport(supp: TransferHandler.TransferSupport) = if (supp.isDrop) false else if (supp.isDataFlavorSupported(supportedFlavor)) {
    try {
      val data = supp.getTransferable.getTransferData(supportedFlavor) match {
        case c: CategoryTransferData => c
        case x => throw ClassCastException(s"expected ${classOf[CategoryTransferData]}, got ${x.getClass}")
      }
      !contains(data.spec)
    } catch {
      case _: UnsupportedFlavorException => false
      case _: IOException => false
    }
  } else false

  override def importData(supp: TransferHandler.TransferSupport) = if (!canImport(supp)) false else {
    try {
      val data = supp.getTransferable.getTransferData(supportedFlavor) match {
        case c: CategoryTransferData => c
        case x => throw ClassCastException(s"expected ${classOf[CategoryTransferData]}, got ${x.getClass}")
      }
      add(data.spec)
    } catch {
      case _: UnsupportedFlavorException => false
      case _: IOException => false
    }
  }
}