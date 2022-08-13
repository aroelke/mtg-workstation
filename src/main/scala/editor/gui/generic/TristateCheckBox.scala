package editor.gui.generic

import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.event.ActionListener
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.UIManager

/**
 * Possible states for a [[TristateCheckBox]]: selected (checked), indeterminate or mixed (displays a box), and unselected (unchecked).
 * @author Alec Roelke
 */
enum TristateCheckBoxState { case Selected, Indeterminate, Unselected }

/**
 * A check box that, in addition to being checked or unchecked, can be in a third state that indicates a mixture of being checked or
 * unchecked, or an indeterminate state.  This state cannot be reached by clicking, only by creating one already in that state. If the
 * box is in the state and is clicked, it becomes unselected.
 * 
 * @constructor create a new tristate check box
 * @param text label next to the check box
 * @param s initial state of the check box
 * 
 * @author Alec Roelke
 */
class TristateCheckBox(text: String = "", private var s: TristateCheckBoxState = TristateCheckBoxState.Unselected) extends JCheckBox(text) {
  import TristateCheckBoxState._

  state = s

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

  /** Listeners for changes in the check box's state. */
  val listeners = collection.mutable.Set[ActionListener]()

  setIcon(IndeterminateIcon(UIManager.getColor("CheckBox.foreground")))
  setRolloverIcon(IndeterminateIcon(UIManager.getColor("CheckBox.foreground")))
  super.addActionListener((e) => {
    state = state match {
      case Selected | Indeterminate => Unselected
      case Unselected => Selected
    }
    listeners.foreach(_.actionPerformed(e))
  })

  /** @return the current state of the check box */
  def state = s

  /**
   * Set the state of the check box.  Triggers action listeners.  This is the only way to set the check box to
   * an indeterminate state after it's been constructed.
   *
   * @param s new state of the check box
   */
  def state_=(s: TristateCheckBoxState) = {
    this.s = s
    super.setSelected(state == Selected)
  }

  /** @return true if the check box is in an indeterminate/multi state, or false otherwise */
  def isPartial = state == Indeterminate

  override def isSelected = state == Selected
  override def setSelected(b: Boolean) = state = if (b) Selected else Unselected
  @deprecated override def addActionListener(l: ActionListener) = listeners += l
  @deprecated override def removeActionListener(l: ActionListener) = listeners -= l
  @deprecated override def getActionListeners = listeners.toArray
}