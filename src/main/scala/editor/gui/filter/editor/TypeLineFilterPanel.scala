package editor.gui.filter.editor

import editor.filter.leaf.TypeLineFilter
import javax.swing.BoxLayout
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment
import javax.swing.JTextField
import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf

object TypeLineFilterPanel {
  def apply() = new TypeLineFilterPanel

  def apply(filter: TypeLineFilter) = {
    val panel = new TypeLineFilterPanel
    panel.setContents(filter)
    panel
  }
}

class TypeLineFilterPanel extends FilterEditorPanel[TypeLineFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  private val line = JTextField()
  add(line)

  override def filter = CardAttribute.createFilter(CardAttribute.TYPE_LINE) match {
    case typeline: TypeLineFilter =>
      typeline.contain = contain.getSelectedItem
      typeline.line = line.getText
      typeline
  }

  override def setContents(filter: TypeLineFilter) = {
    contain.setSelectedItem(filter.contain)
    line.setText(filter.line)
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case line: TypeLineFilter => setContents(line)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a type line filter")
  }
}