package editor.gui.generic

import javax.swing.JButton
import java.awt.event.ActionListener
import javax.swing.Timer
import editor.util.MouseListenerFactory
import java.awt.event.ActionEvent

class RepeatButton(initial: Int = 0, tick: Option[Int] = None) extends JButton {
  def addRepeatListener(listener: ActionListener, initial: Int = initial, tick: Int = tick.getOrElse(initial)): Unit = {
    val timer = Timer(tick, listener)
    timer.setRepeats(true)
    timer.setInitialDelay(initial)
    addMouseListener(MouseListenerFactory.createHoldListener((e) => {
      listener.actionPerformed(ActionEvent(e.getSource, e.getID, e.paramString, e.getWhen, e.getModifiersEx))
      timer.start()
    }, _ => timer.stop()))
  }

  def addRepeatListener(listener: ActionListener, delay: Int): Unit = addRepeatListener(listener, delay, delay)
}