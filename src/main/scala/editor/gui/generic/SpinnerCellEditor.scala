package editor.gui.generic

import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.JTable
import java.util.EventObject
import java.awt.event.MouseEvent
import java.text.ParseException

/**
 * Table cell editor used for editing discrete values, like numbers, using a spinner.
 * @constructor create a new spinner cell editor
 * @author Alec Roelke
 */ 
class SpinnerCellEditor extends DefaultCellEditor(JTextField()) {
  private val spinner = JSpinner(SpinnerNumberModel())
  spinner.getEditor match {
    case editor: JSpinner.NumberEditor => editor.getTextField.addActionListener(_ => stopCellEditing())
    case _ =>
  }

  override def isCellEditable(eo: EventObject) = eo match {
    case m: MouseEvent => m.getClickCount > 1
    case _ => false
  }

  override def getTableCellEditorComponent(table: JTable, value: AnyRef, isSelected: Boolean, row: Int, column: Int) = {
    spinner.setValue(value)
    spinner
  }

  override def stopCellEditing() = {
    try {
      spinner.commitEdit()
    } catch case e: ParseException => {}
    super.stopCellEditing()
  }

  override def getCellEditorValue = spinner.getValue
}
