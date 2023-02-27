package editor.gui

import editor.database.attributes.ManaType
import editor.database.symbol.ManaSymbolInstances.ColorSymbol
import editor.database.symbol.Symbol
import editor.gui.generic.ComponentUtils

import java.awt.Image
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JCheckBox

/**
 * A two-state button using a [[Symbol]] as an icon. Dims when unselected.
 * @author Alec Roelke
 */
object SymbolButton {
  private val IconHeight = 13

  private val cache = collection.mutable.Map.empty[String, Icon]

  /**
   * Create a new symbol button using a [[Symbol]] or one derived from a [[ManaType]].
   * 
   * @param item symbol or mana type to use for the new button
   * @param selected initial state of the button
   * 
   * @return a [[JCheckBox]] that uses the given symbol, or derived symbol instead of the standard check box
   */
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