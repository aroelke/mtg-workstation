package editor.gui

import editor.database.symbol.Symbol
import editor.database.symbol.ManaSymbolInstances.ColorSymbol
import editor.gui.generic.ComponentUtils
import editor.database.attributes.ManaType

import java.awt.Image
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JCheckBox
import javax.swing.Icon

object SymbolButton {
  private val IconHeight = 13

  private val cache = collection.mutable.Map.empty[String, Icon]

  def apply(item: Symbol | ManaType, selected: Boolean = false): JCheckBox = try { item match {
    case symbol: Symbol =>
      val box = JCheckBox()

      def createIcon(name: String) = cache.getOrElseUpdate(
        s"${symbol.name}/$name",
        ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/$name.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
      )

      box.setIcon(createIcon("unselected"))
      box.setSelectedIcon(symbol.scaled(IconHeight))
      box.setDisabledIcon(createIcon("disabled_unselected"))
      box.setDisabledSelectedIcon(createIcon("disabled_selected"))
      box.setPressedIcon(createIcon("pressed"))
      box.setRolloverIcon(createIcon("rollover_unselected"))
      box.setRolloverSelectedIcon(createIcon("rollover_selected"))

      box.setSelected(selected)
      box
    case color: ManaType => apply(ColorSymbol(color), selected)
  }} catch case x: IOException => JCheckBox()
}