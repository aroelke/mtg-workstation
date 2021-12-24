package editor.gui.filter.editor

import editor.filter.leaf.NumberFilter
import javax.swing.BoxLayout
import editor.gui.generic.ComboBoxPanel
import editor.util.Comparison
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import java.awt.Dimension
import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf

object NumberFilterPanel {
  def apply() = new NumberFilterPanel

  def apply(filter: NumberFilter) = {
    val panel = new NumberFilterPanel
    panel.setContents(filter)
    panel
  }
}

class NumberFilterPanel extends FilterEditorPanel[NumberFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Combo box for choosing the type of comparison to make
  private val comparison = ComboBoxPanel(Comparison.values)
  add(comparison)

  // Value to compare the card number against
  private val spinner = JSpinner()
  spinner.setModel(SpinnerNumberModel(0.0, 0.0, null, 1.0))
  spinner.setMaximumSize(Dimension(100, Int.MaxValue))
  add(spinner)

  private var attribute = CardAttribute.CARD_NUMBER

  override def filter = (CardAttribute.createFilter(attribute), spinner.getValue) match {
    case (number: NumberFilter, value: Double) =>
      number.operation = comparison.getSelectedItem
      number.operand = value
      number
  }

  override def setContents(filter: NumberFilter) = {
    attribute = filter.`type`
    comparison.setSelectedItem(filter.operation)
    spinner.setValue(filter.operand)
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case number: NumberFilter => setContents(number)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a number filter")
  }
}