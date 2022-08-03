package editor.gui.deck

import editor.collection.Categorization
import editor.gui.display.CardTable
import editor.util.MouseListenerFactory

import java.awt.Color
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.TableCellEditor
import scala.jdk.CollectionConverters._

/**
 * Table cell editor for changing the category inclusion of a card. When the cell is double-
 * clicked, a dialog opens containing an [[IncludeExcludePanel]] for doing the editing.
 * 
 * @constructor create a new category inclusion editor
 * @param frame editor frame containing the deck whose categories are to be edited
 * 
 * @author Alec Roelke
 */
class InclusionCellEditor(frame: EditorFrame) extends AbstractCellEditor with TableCellEditor {
  private var included = Seq.empty[Categorization]
  private var iePanel: Option[IncludeExcludePanel] = None

  private val editor = new JPanel {
    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      included.zipWithIndex.foreach{ case (include, i) =>
        val x = i*(getHeight + 1) + 1
        val y = 1
        g.setColor(include.color)
        g.fillRect(x, y, getHeight - 3, getHeight - 3)
        g.setColor(Color.BLACK)
        g.drawRect(x, y, getHeight - 3, getHeight - 3)
      }
    }
  }
  editor.addMouseListener(MouseListenerFactory.createMouseListener(pressed = _ => {
    if (JOptionPane.showConfirmDialog(frame, JScrollPane(iePanel.get), "Set Categories", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
      fireEditingStopped()
    else
      fireEditingCanceled()
    iePanel = None
  }))

  override def getCellEditorValue = iePanel.get

  override def getTableCellEditorComponent(table: JTable, value: AnyRef, isSelected: Boolean, row: Int, column: Int) = {
    table match {
      case cTable: CardTable =>
        iePanel = Some(IncludeExcludePanel(frame.categories.toSeq.sortBy(_.name.toLowerCase), Seq(frame.getCardAt(cTable, row).card)))
        included = value match {
          case c: java.util.Collection[?] => c.asScala.collect{ case category: Categorization => category }.toSeq
          case _ => Seq.empty
        }
        if (!cTable.isRowSelected(row))
          editor.setBackground(cTable.rowColor(row))
        else {
          editor.setBackground(cTable.getSelectionBackground)
          if (cTable.hasFocus)
            editor.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"))
        }
      case _ => iePanel = Some(IncludeExcludePanel(frame.categories.toSeq.sortBy(_.name.toLowerCase), frame.getSelectedCards.map(_.card)))
    }
    editor
  }

  override def isCellEditable(eo: EventObject) = eo match {
    case m: MouseEvent => m.getClickCount > 1
    case _ => false
  }
}
