package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.symbol.ColorSymbol
import editor.database.symbol.StaticSymbol
import editor.filter.leaf.ColorFilter
import editor.filter.leaf.FilterLeaf
import editor.gui.generic.ComboBoxPanel
import editor.gui.generic.ComponentUtils
import editor.util.Containment

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import scala.jdk.CollectionConverters._

/**
 * Convenience constructors for creating [[ColorFilterPanel]]s.
 * @author Alec Roelke
 */
object ColorFilterPanel {
  /** @return an empty [[ColorFilterPanel]] set to filter by color */
  def apply() = new ColorFilterPanel

  /**
   * Create a new [[ColorFilterPanel]], set its filter attribute, and set its contents.
   * 
   * @param filter filter to use to set the new panel's attribute and contents
   * @return a new [[ColorFilterPanel]] with the given filter's attribute and contents
   */
  def apply(filter: ColorFilter) = {
    val panel = new ColorFilterPanel
    panel.setContents(filter)
    panel
  }
}

/**
 * A panel for customzing a filter that filters cards by color characteristic (e.g. color or color identity).
 * @author Alec Roelke
 */
class ColorFilterPanel extends FilterEditorPanel[ColorFilter] {
  private val IconHeight = 13

  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Containment options
  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  // Check box for filtering for colorless
  private val colorless = JCheckBox()

  // Check boxes for selecting colors
  val colors = ManaType.colors.map(_ -> JCheckBox()).toMap
  colors.foreach{ case (color, box) =>
    add(box)
    add(JLabel(ColorSymbol.SYMBOLS.get(color).getIcon(IconHeight)))
    box.addActionListener(_ => if (box.isSelected) colorless.setSelected(false))
  }
  add(Box.createHorizontalStrut(4))
  add(ComponentUtils.createHorizontalSeparator(4, contain.getPreferredSize.height))

  // Check box for multicolored cards
  private val multi = JCheckBox()
  add(multi)
  add(JLabel(StaticSymbol.SYMBOLS.get("M").getIcon(IconHeight)))
  multi.addActionListener(_ => if (multi.isSelected) colorless.setSelected(false))

  // Actually add the colorless box here
  colorless.setSelected(true)
  add(colorless)
  add(JLabel(ColorSymbol.SYMBOLS.get(ManaType.COLORLESS).getIcon(IconHeight)))
  colorless.addActionListener(_ => if (colorless.isSelected) {
    colors.foreach{ case (_, box) => box.setSelected(false) }
    multi.setSelected(false)
  })

  add(Box.createHorizontalStrut(2))

  protected override var attribute = CardAttribute.COLORS

  override def filter = CardAttribute.createFilter(attribute) match {
    case filter: ColorFilter =>
      filter.contain = contain.getSelectedItem
      filter.colors.addAll(colors.collect{ case (c, b) if b.isSelected => c }.toSet.asJava)
      filter.multicolored = multi.isSelected
      filter
  }

  override def setFields(filter: ColorFilter) = {
    attribute = filter.`type`
    contain.setSelectedItem(filter.contain)
    filter.colors.asScala.foreach(colors(_).setSelected(true))
    multi.setSelected(filter.multicolored)
    colorless.setSelected(!filter.multicolored && filter.colors.isEmpty)
  }
}