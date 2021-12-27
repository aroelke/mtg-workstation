package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf

import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JLabel

/**
 * A filter "editor" panel that creates a filter that always returns true when testing a card or false.
 * 
 * @constructor create a new binary filter panel
 * @param allow whether or not the filter should allow cards through or not
 * 
 * @author Alec Roelke
 */
class BinaryFilterPanel(allow: Boolean) extends FilterEditorPanel[FilterLeaf[?]] {
  val All = "This clause will match every card."
  val None = "This clause will not match any card."

  setLayout(GridLayout(1, 1))
  setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0))
  add(JLabel(if (allow) All else None))

  protected override val attribute = if (allow) CardAttribute.ANY else CardAttribute.NONE
  override lazy val filter = CardAttribute.createFilter(if (allow) CardAttribute.ANY else CardAttribute.NONE)
  override def setFields(filter: FilterLeaf[?]) = {}
}
