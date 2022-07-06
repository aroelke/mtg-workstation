package editor.gui.filter.editor

import editor.database.FormatConstraints
import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.options.OptionsFilter
import editor.filter.leaf.options.multi.LegalityFilter
import editor.gui.filter.FilterSelectorPanel

import java.awt.BorderLayout
import javax.swing.JCheckBox

/**
 * Convenience constructors for [[LegalityFilterPanel]].
 * @author Alec Roelke
 */
object LegalityFilterPanel {
  /** @return a new, empty [[LegalityFilterPanel]]. */
  def apply(selector: FilterSelectorPanel) = new LegalityFilterPanel(selector)

  /**
   * Create a new [[LegalityFilterPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use for populating the new panel
   * @return a format legality filter with its contents taken from the filter
   */
  def apply(filter: LegalityFilter, selector: FilterSelectorPanel) = {
    val panel = new LegalityFilterPanel(selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A filter editor panel that can customize a [[LegalityFilter]], presenting a list of formats to filter cards
 * by legality in and a check box to indicate if the card should or should not be restricted in it.
 * 
 * @author Alec Roelke
 */
class LegalityFilterPanel(selector: FilterSelectorPanel) extends OptionsFilterPanel(CardAttribute.LegalIn, FormatConstraints.FormatNames.toArray, selector) {
  private val restricted = JCheckBox("Restricted")
  add(restricted, BorderLayout.EAST)

  override def filter = super.filter match {
    case legality: LegalityFilter =>
      legality.restricted = restricted.isSelected
      legality
  }

  override def setFields(filter: OptionsFilter[String]) = filter match {
    case legality: LegalityFilter =>
      super.setFields(legality)
      restricted.setSelected(legality.restricted)
    case _ => throw IllegalArgumentException(s"${filter.attribute} is not a legality filter")
  }
}