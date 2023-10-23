package editor.gui.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.symbol.ManaSymbolInstances.ColorSymbol
import editor.database.symbol.ManaSymbolInstances.StaticSymbol
import editor.filter.leaf.ColorFilter
import editor.filter.leaf.FilterLeaf
import editor.gui.ManaSetPanel
import editor.gui.SymbolButton
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.gui.generic.ComponentUtils
import editor.util.Containment

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import scala.collection.immutable.ListMap
import scala.collection.immutable.ListSet

/**
 * Convenience constructors for creating [[ColorFilterPanel]]s.
 * @author Alec Roelke
 */
object ColorFilterPanel {
  /** @return an empty [[ColorFilterPanel]] set to filter by color */
  def apply(selector: FilterSelectorPanel): ColorFilterPanel = new ColorFilterPanel(selector, ListSet(ManaType.colors:_*))

  /**
   * Create a new [[ColorFilterPanel]], set its filter attribute, and set its contents.
   * 
   * @param filter filter to use to set the new panel's attribute and contents
   * @param available set of mana types available to choose from
   * @return a new [[ColorFilterPanel]] with the given filter's attribute and contents
   */
  def apply(filter: ColorFilter, available: ListSet[ManaType], selector: FilterSelectorPanel): ColorFilterPanel = {
    val panel = new ColorFilterPanel(selector, available)
    panel.setContents(filter)
    panel
  }

  /**
   * Create a new [[ColorFilterPanel]], set its filter attribute, and set its contents.
   * 
   * @param filter filter to use to set the new panel's attribute and contents
   * @param available list of mana types available to choose from
   * @return a new [[ColorFilterPanel]] with the given filter's attribute and contents
   */
  def apply(filter: ColorFilter, available: Seq[ManaType], selector: FilterSelectorPanel): ColorFilterPanel = apply(filter, ListSet(available:_*), selector)
}

/**
 * A panel for customzing a filter that filters cards by color characteristic (e.g. color or color identity).
 * @author Alec Roelke
 */
class ColorFilterPanel(selector: FilterSelectorPanel, available: ListSet[ManaType]) extends FilterEditorPanel[ColorFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Containment options
  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  // Check box for filtering for colorless
  private val colorless = Option.when(!available.contains(ManaType.Colorless))(SymbolButton(ManaType.Colorless))

  // Check boxes for selecting colors
  val colorBoxes = ManaSetPanel(available = available)
  add(colorBoxes)
  colorless.foreach((c) => colorBoxes.addActionListener((e) => e.getSource match {
    case b: JCheckBox =>
      if (b.isSelected)
        c.setSelected(false)
      else if (colorBoxes.selected.isEmpty)
        c.setSelected(true)
  }))

  add(Box.createHorizontalStrut(4))
  add(ComponentUtils.createHorizontalSeparator(4, contain.getPreferredSize.height))

  // Check box for multicolored cards
  private val multi = SymbolButton(StaticSymbol("M"))
  add(multi)
  colorless.foreach((c) => multi.addActionListener(_ => if (multi.isSelected) c.setSelected(false)))

  // Actually add the colorless box here
  colorless.foreach((c) => {
    c.setSelected(true)
    add(c)
    c.addActionListener(_ => if (c.isSelected) {
      colorBoxes.selected = Set.empty
      multi.setSelected(false)
    })
  })

  add(Box.createHorizontalStrut(2))

  protected override var attribute = CardAttribute.Colors

  override def filter = attribute.filter.copy(faces = selector.faces, contain = contain.getSelectedItem, colors = colorBoxes.selected, multicolored = multi.isSelected)

  override def setFields(filter: ColorFilter) = {
    require(filter.colors.forall(available.contains))

    attribute = filter.attribute
    contain.setSelectedItem(filter.contain)
    colorBoxes.selected = filter.colors
    multi.setSelected(filter.multicolored)
    colorless.foreach(_.setSelected(!filter.multicolored && filter.colors.isEmpty))
  }
}