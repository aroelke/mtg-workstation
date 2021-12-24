package editor.gui.filter.editor

import editor.database.FormatConstraints
import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.options.OptionsFilter
import editor.filter.leaf.options.multi.LegalityFilter

import java.awt.BorderLayout
import javax.swing.JCheckBox

/**
 * Convenience constructors for [[LegalityFilterPanel]].
 * @author Alec Roelke
 */
object LegalityFilterPanel {
  /** @return a new, empty [[LegalityFilterPanel]]. */
  def apply() = new LegalityFilterPanel

  /**
   * Create a new [[LegalityFilterPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use for populating the new panel
   * @return a format legality filter with its contents taken from the filter
   */
  def apply(filter: LegalityFilter) = {
    val panel = new LegalityFilterPanel
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
class LegalityFilterPanel extends OptionsFilterPanel(CardAttribute.LEGAL_IN, FormatConstraints.FormatNames.toArray) {
  private val restricted = JCheckBox("Restricted")
  add(restricted, BorderLayout.EAST)

  /**
   * Update the format selection box and restricted check box based on the contents of the filter.
   * @param filter filter to use to update contents
   */
  def setContents(filter: LegalityFilter) = {
    super.setContents(filter)
    restricted.setSelected(filter.restricted)
  }

  override def filter = super.filter match {
    case legality: LegalityFilter =>
      legality.restricted = restricted.isSelected
      legality
  }

  override def setContents(filter: OptionsFilter[String]) = filter match {
    case legality: LegalityFilter => setContents(legality)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a legality filter")
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case legality: LegalityFilter => setContents(legality)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a legality filter")
  }
}