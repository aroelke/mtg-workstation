package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.VariableNumberFilter
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.util.Comparison

import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * Convenience constructors for [[VariableNumberFilterPanel]].
 * @author Alec Roelke
 */
object VariableNumberFilterPanel {
  /**
   * Create a new, empty [[VariableNumberFilterPanel]] with the given variable string.
   * 
   * @param v string to display indicating what "variable" means
   * @return an empty [[VariableNumberFilter]] with the given string in its variable check box label
   */
  def apply(v: String, selector: FilterSelectorPanel) = new VariableNumberFilterPanel(v, selector)

  /**
   * Create a new [[VariableNumberFilterPanel]] pre-populated with values from a filter. Its variable string
   * is inferred from the attribute of the filter.
   * 
   * @param filter filter to use for populating the panel
   * @return a [[VariableNumberFilterPanel]] with values taken from the given filter and variable string inferred
   * from its attribute
   */
  def apply(filter: VariableNumberFilter, selector: FilterSelectorPanel) = {
    val panel = new VariableNumberFilterPanel(if (filter.attribute == CardAttribute.LOYALTY) "X or *" else "*", selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A filter editor panel that filters by a numeric value that can sometimes be undefined or variable throughout
 * a game (for example, power or toughness might be "*" or loyalty might be "X"). Works similarly to a
 * [[NumberFilterPanel]], except contains an additional check box indicating if the desired attribute value is
 * variable, which disables the other elements.
 * 
 * @constructor create a new filter panel with the given variable string
 * @param v string to use to indicate what "variable" means
 * 
 * @author Alec Roelke
 */
class VariableNumberFilterPanel(v: String, selector: FilterSelectorPanel) extends FilterEditorPanel[VariableNumberFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  private val comparison = ComboBoxPanel(Comparison.values)
  add(comparison)

  private val spinner = JSpinner()
  spinner.setModel(SpinnerNumberModel(0.0, 0.0, null, 1.0))
  spinner.setMaximumSize(Dimension(100, Int.MaxValue))
  add(spinner)

  private val variable = JCheckBox(s"Contains $v")
  variable.addActionListener(_ => spinner.setEnabled(!variable.isSelected))
  add(variable)

  protected override var attribute = CardAttribute.POWER

  override def filter = (CardAttribute.createFilter(attribute), spinner.getValue) match {
    case (number: VariableNumberFilter, value: Double) =>
      number.operation = comparison.getSelectedItem
      number.operand = value
      number.varies = variable.isSelected
      number
  }

  override def setFields(filter: VariableNumberFilter) = {
    attribute = filter.attribute
    comparison.setSelectedItem(filter.operation)
    spinner.setValue(filter.operand)
    variable.setSelected(filter.varies)
    spinner.setEnabled(!filter.varies)
    comparison.setEnabled(!filter.varies)
  }
}
