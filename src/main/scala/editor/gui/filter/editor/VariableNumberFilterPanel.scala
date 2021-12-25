package editor.gui.filter.editor

import editor.filter.leaf.VariableNumberFilter
import javax.swing.BoxLayout
import editor.gui.generic.ComboBoxPanel
import editor.util.Comparison
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import java.awt.Dimension
import javax.swing.JCheckBox
import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf

object VariableNumberFilterPanel {
  def apply(v: String) = new VariableNumberFilterPanel(v)

  def apply(filter: VariableNumberFilter) = {
    val panel = new VariableNumberFilterPanel(if (filter.`type` == CardAttribute.LOYALTY) "X or *" else "*")
    panel.setContents(filter)
    panel
  }
}

class VariableNumberFilterPanel(v: String) extends FilterEditorPanel[VariableNumberFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  private val comparison = ComboBoxPanel(Comparison.values)
  add(comparison)

  private val spinner = JSpinner()
  spinner.setModel(SpinnerNumberModel(0.0, 0.0, null, 1.0))
  spinner.setMaximumSize(Dimension(100, Int.MaxValue))
  add(spinner)

  private val variable = JCheckBox(s"Contains $v")
  variable.addActionListener(_ => spinner.setEnabled(!variable.isSelected))

  private var attribute = CardAttribute.POWER

  override def filter = (CardAttribute.createFilter(attribute), spinner.getValue) match {
    case (number: VariableNumberFilter, value: Double) =>
      number.operation = comparison.getSelectedItem
      number.operand = value
      number.varies = variable.isSelected
      number
  }

  override def setContents(filter: VariableNumberFilter) = {
    attribute = filter.`type`
    comparison.setSelectedItem(filter.operation)
    spinner.setValue(filter.operand)
    variable.setSelected(filter.varies)
    spinner.setEnabled(!filter.varies)
    comparison.setEnabled(!filter.varies)
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case number: VariableNumberFilter => setContents(number)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a variable number filter")
  }
}
