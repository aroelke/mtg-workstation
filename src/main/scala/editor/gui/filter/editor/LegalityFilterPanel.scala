package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.database.FormatConstraints
import javax.swing.JCheckBox
import java.awt.BorderLayout
import editor.filter.leaf.options.multi.LegalityFilter
import editor.filter.leaf.options.OptionsFilter
import editor.filter.leaf.FilterLeaf

object LegalityFilterPanel {
  def apply() = new LegalityFilterPanel

  def apply(filter: LegalityFilter) = {
    val panel = new LegalityFilterPanel
    panel.setContents(filter)
    panel
  }
}

class LegalityFilterPanel extends OptionsFilterPanel(CardAttribute.LEGAL_IN, FormatConstraints.FormatNames.toArray) {
  private val restricted = JCheckBox("Restricted")
  add(restricted, BorderLayout.EAST)

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