package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.symbol.ManaSymbolInstances.ColorSymbol
import editor.database.symbol.ManaSymbolInstances.StaticSymbol
import editor.filter.leaf.ColorFilter
import editor.filter.leaf.FilterLeaf
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.gui.generic.ComponentUtils
import editor.util.Containment

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters._

/**
 * Convenience constructors for creating [[ColorFilterPanel]]s.
 * @author Alec Roelke
 */
object ColorFilterPanel {
  /** @return an empty [[ColorFilterPanel]] set to filter by color */
  def apply(selector: FilterSelectorPanel) = new ColorFilterPanel(selector)

  /**
   * Create a new [[ColorFilterPanel]], set its filter attribute, and set its contents.
   * 
   * @param filter filter to use to set the new panel's attribute and contents
   * @return a new [[ColorFilterPanel]] with the given filter's attribute and contents
   */
  def apply(filter: ColorFilter, selector: FilterSelectorPanel) = {
    val panel = new ColorFilterPanel(selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A panel for customzing a filter that filters cards by color characteristic (e.g. color or color identity).
 * @author Alec Roelke
 */
class ColorFilterPanel(selector: FilterSelectorPanel) extends FilterEditorPanel[ColorFilter] {
  private val IconHeight = 13

  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Containment options
  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  // Check box for filtering for colorless
  private val colorless = JCheckBox()

  // Check boxes for selecting colors
  val colorBoxes = ListMap(ManaType.colors.map(_ -> JCheckBox()):_*)
  colorBoxes.foreach{ case (color, box) =>
    add(box)
    add(JLabel(ColorSymbol(color).getIcon(IconHeight)))
    box.addActionListener(_ => if (box.isSelected) colorless.setSelected(false))
  }
  add(Box.createHorizontalStrut(4))
  add(ComponentUtils.createHorizontalSeparator(4, contain.getPreferredSize.height))

  // Check box for multicolored cards
  private val multi = JCheckBox()
  add(multi)
  add(JLabel(StaticSymbol("M").getIcon(IconHeight)))
  multi.addActionListener(_ => if (multi.isSelected) colorless.setSelected(false))

  // Actually add the colorless box here
  colorless.setSelected(true)
  add(colorless)
  add(JLabel(ColorSymbol(ManaType.Colorless).getIcon(IconHeight)))
  colorless.addActionListener(_ => if (colorless.isSelected) {
    colorBoxes.foreach{ case (_, box) => box.setSelected(false) }
    multi.setSelected(false)
  })

  add(Box.createHorizontalStrut(2))

  protected override var attribute = CardAttribute.Colors

  override def filter = attribute.filter.copy(faces = selector.faces, contain = contain.getSelectedItem, colors = colorBoxes.collect{ case (c, b) if b.isSelected => c }.toSet, multicolored = multi.isSelected)

  override def setFields(filter: ColorFilter) = {
    attribute = filter.attribute
    contain.setSelectedItem(filter.contain)
    filter.colors.foreach(colorBoxes(_).setSelected(true))
    multi.setSelected(filter.multicolored)
    colorless.setSelected(!filter.multicolored && filter.colors.isEmpty)
  }
}