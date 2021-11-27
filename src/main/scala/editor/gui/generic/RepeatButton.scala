package editor.gui.generic

import editor.util.MouseListenerFactory

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.Timer

/**
 * Button that, when held down, repeats its action with a delay until the mouse is released.
 * Repeated actions are not performed when the button is initially pressed; use [[addActionListener]]
 * inherited from [[JButton]] for that.
 * 
 * @constructor create a new button optionally wi th default delays
 * @param initial initial delay between pressing the button and when action repetition begins
 * @param tick delay between action repetitions; defaults to initial if not set
 * 
 * @author Alec Roelke
 */
class RepeatButton(initial: Int = 0, tick: Option[Int] = None) extends JButton {
  /**
   * Add a new repeat listener with custom initial delay and delays between repetitions.
   * 
   * @param listener action to perform while held down
   * @param initial delay before beginning repetition; use the default value if unset
   * @param tick delay between repetitions; use the default value if unset
   */
  def addRepeatListener(listener: ActionListener, initial: Int = initial, tick: Int = tick.getOrElse(initial)): Unit = {
    val timer = Timer(tick, listener)
    timer.setRepeats(true)
    timer.setInitialDelay(initial)
    addMouseListener(MouseListenerFactory.createHoldListener((e) => {
      listener.actionPerformed(ActionEvent(e.getSource, e.getID, e.paramString, e.getWhen, e.getModifiersEx))
      timer.start()
    }, _ => timer.stop()))
  }

  /**
   * Add a new repeat listener with the same initial delay and tick.
   * 
   * @param listener action to perform while held down
   * @param delay delay between actions and before the repetitions start
   */
  def addRepeatListener(listener: ActionListener, delay: Int): Unit = addRepeatListener(listener, delay, delay)
}