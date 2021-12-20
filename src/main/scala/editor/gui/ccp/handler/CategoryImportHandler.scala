package editor.gui.ccp.handler

import javax.swing.TransferHandler
import editor.collection.deck.Category
import editor.gui.ccp.data.DataFlavors
import editor.gui.ccp.data.CategoryTransferData
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class CategoryImportHandler(contains: (Category) => Boolean, add: (Category) => Boolean) extends TransferHandler with ImportHandler {
  @deprecated def this(contains: java.util.function.Predicate[Category], add: java.util.function.Predicate[Category]) = this(contains.test, add.test)

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