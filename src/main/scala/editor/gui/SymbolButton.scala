package editor.gui

import editor.database.symbol.Symbol
import editor.gui.generic.ComponentUtils

import java.awt.Image
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JCheckBox

object SymbolButton {
  private val IconHeight = 13

  def apply(symbol: Symbol, selected: Boolean = false) = try {
    val box = JCheckBox()

    val unselected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/unselected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val disabledUnselected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/disabled_unselected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val disabledSelected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/disabled_selected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val pressed = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/pressed.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val rolloverUnselected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/rollover_unselected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))
    val rolloverSelected = ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/ui/SymbolButton/${symbol.name}/rollover_selected.png")).getScaledInstance(-1, IconHeight, Image.SCALE_SMOOTH))

    box.setIcon(unselected)
    box.setSelectedIcon(symbol.scaled(IconHeight))
    box.setDisabledIcon(disabledUnselected)
    box.setDisabledSelectedIcon(disabledSelected)
    box.setPressedIcon(pressed)
    box.setRolloverIcon(rolloverUnselected)
    box.setRolloverSelectedIcon(rolloverSelected)

    box.setSelected(selected)
    box
  } catch case x: IOException => JCheckBox()
}
