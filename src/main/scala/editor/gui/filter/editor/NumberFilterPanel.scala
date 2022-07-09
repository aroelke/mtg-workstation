package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.NumberFilter
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.util.Comparison

import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * Convenience constructors for [[NumberFilterPanel]].
 * @author Alec Roelke
 */
object NumberFilterPanel {
  /** @return a new, empty [[NumberFilterPanel]] */
  def apply(selector: FilterSelectorPanel) = new NumberFilterPanel(selector)

  /**
   * Create a new [[NumberFilterPanel]] and pre-populate it with the contents of a [[NumberFilter]].
   * 
   * @param filter filter to use to populate the new panel
   * @return a new number filter panel set to compare with the value from the filter using the comparison from it
   */
  def apply(filter: NumberFilter, selector: FilterSelectorPanel) = {
    val panel = new NumberFilterPanel(selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A panel for customizing [[NumberFilter]]s. The comparison with the card's value is chosen using a combo box and
 * the value to compare with is chosen using a spinner.
 * 
 * @author Alec Roelke
 */
class NumberFilterPanel(selector: FilterSelectorPanel) extends FilterEditorPanel[NumberFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Combo box for choosing the type of comparison to make
  private val comparison = ComboBoxPanel(Comparison.values)
  add(comparison)

  // Value to compare the card number against
  private val spinner = JSpinner()
  spinner.setModel(SpinnerNumberModel(0.0, 0.0, null, 1.0))
  spinner.setMaximumSize(Dimension(100, Int.MaxValue))
  add(spinner)

  protected override var attribute = CardAttribute.CardNumber

  override def filter = (attribute.filter, spinner.getValue) match {
    case (number: NumberFilter, value: Double) =>
      number.faces = selector.faces
      number.operation = comparison.getSelectedItem
      number.operand = value
      number
  }

  override def setFields(filter: NumberFilter) = {
    attribute = filter.attribute
    comparison.setSelectedItem(filter.operation)
    spinner.setValue(filter.operand)
  }
}