package editor.gui.generic

import javax.swing.JCheckBox
import javax.swing.UIManager
import java.awt.event.ActionListener
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

enum TristateCheckBoxState { case Selected, Indeterminate, Unselected }

class TristateCheckBox(text: String = "", private var s: TristateCheckBoxState = TristateCheckBoxState.Unselected) extends JCheckBox(text) {
  import TristateCheckBoxState._

  private class IndeterminateIcon(color: Color) extends Icon {
    private val BaseIcon = UIManager.getIcon("CheckBox.icon")
    private val IconFill = 2.0/3.0

    override def getIconHeight = BaseIcon.getIconHeight
    override def getIconWidth = BaseIcon.getIconWidth

    override def paintIcon(c: Component, g: Graphics, x: Int, y: Int) = {
      BaseIcon.paintIcon(c, g, x, y)
      if (state == Indeterminate) {
        g.setColor(color)
        g.fillRect(math.floor(x + getIconWidth*(1 - IconFill)/2).toInt, math.floor(y + getIconHeight*(1 - IconFill)/2).toInt,
                   math.ceil(getIconWidth*IconFill).toInt, math.ceil(getIconHeight*IconFill).toInt)
      }
    }
  }

  val listeners = collection.mutable.Set[ActionListener]()

  setIcon(IndeterminateIcon(UIManager.getColor("CheckBox.foreground")))
  setRolloverIcon(IndeterminateIcon(UIManager.getColor("CheckBox.foreground")))

  def state = s
  def state_=(s: TristateCheckBoxState) = {
    this.s = s
    super.setSelected(state == Selected)
  }

  def isPartial = state == Indeterminate

  override def isSelected = state == Selected
  @deprecated override def addActionListener(l: ActionListener) = listeners += l
  @deprecated override def removeActionListener(l: ActionListener) = listeners -= l
  @deprecated override def getActionListeners = listeners.toArray
}