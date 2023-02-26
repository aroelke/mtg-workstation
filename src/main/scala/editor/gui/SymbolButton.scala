package editor.gui

import editor.database.symbol.ColorSymbol
import editor.database.symbol.Symbol
import javax.swing.JCheckBox
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import java.io.IOException
import java.awt.Image
import editor.gui.generic.ComponentUtils

object SymbolButton {
  private val IconHeight = 13

  def apply(symbol: ColorSymbol, selected: Boolean = false) = try {
    val box = JCheckBox()

    val unselected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/unselected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val disabledUnselected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/disabled_unselected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val disabledSelected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/disabled_selected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val pressed = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/pressed.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))

    box.setIcon(unselected)
    box.setSelectedIcon(symbol.scaled(IconHeight))
    box.setDisabledIcon(disabledUnselected)
    box.setDisabledSelectedIcon(disabledSelected)
    box.setPressedIcon(pressed)
    box.setRolloverIcon(unselected)
    box.setRolloverSelectedIcon(symbol.scaled(IconHeight))

    box.setSelected(selected)
    box
  } catch case x: IOException => JCheckBox()
}
