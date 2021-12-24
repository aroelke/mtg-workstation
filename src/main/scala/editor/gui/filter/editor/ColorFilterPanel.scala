package editor.gui.filter.editor

import editor.filter.leaf.ColorFilter
import javax.swing.BoxLayout
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment
import javax.swing.JCheckBox
import editor.database.attributes.ManaType
import javax.swing.JLabel
import editor.database.symbol.ColorSymbol
import editor.gui.generic.ComponentUtils
import javax.swing.Box
import editor.database.symbol.StaticSymbol
import editor.database.attributes.CardAttribute
import scala.jdk.CollectionConverters._
import editor.filter.leaf.FilterLeaf

object ColorFilterPanel {
  def apply() = new ColorFilterPanel

  def apply(filter: ColorFilter) = {
    val panel = new ColorFilterPanel
    panel.setContents(filter)
    panel
  }
}

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

  private var attribute = CardAttribute.COLORS

  override def filter = CardAttribute.createFilter(attribute) match {
    case filter: ColorFilter =>
      filter.contain = contain.getSelectedItem
      filter.colors.addAll(colors.collect{ case (c, b) if b.isSelected => c }.toSet.asJava)
      filter.multicolored = multi.isSelected
      filter
  }

  override def setContents(filter: ColorFilter) = {
    attribute = filter.`type`
    contain.setSelectedItem(filter.contain)
    filter.colors.asScala.foreach(colors(_).setSelected(true))
    multi.setSelected(filter.multicolored)
    colorless.setSelected(!filter.multicolored && filter.colors.isEmpty)
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case f: ColorFilter => setContents(f)
    case _ => throw IllegalArgumentException(s"illegal color filter type ${filter.`type`}")
  }
}