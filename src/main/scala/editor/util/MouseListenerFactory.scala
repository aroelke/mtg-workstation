package editor.util

import java.awt.event.MouseListener
import java.awt.event.MouseEvent

object MouseListenerFactory {
  def createMouseListener(
    clicked: (MouseEvent) => Unit = _ => {},
    pressed: (MouseEvent) => Unit = _ => {},
    released: (MouseEvent) => Unit = _ => {},
    entered: (MouseEvent) => Unit = _ => {},
    exited: (MouseEvent) => Unit = _ => {}
  ) = new MouseListener {
    override def mouseClicked(e: MouseEvent) = clicked(e)
    override def mousePressed(e: MouseEvent) = pressed(e)
    override def mouseReleased(e: MouseEvent) = released(e)
    override def mouseEntered(e: MouseEvent) = entered(e)
    override def mouseExited(e: MouseEvent) = exited(e)
  }
  @deprecated def createClickListener(clicked: java.util.function.Consumer[MouseEvent]) = createMouseListener(clicked = clicked.accept)
  @deprecated def createPressListener(pressed: java.util.function.Consumer[MouseEvent]) = createMouseListener(pressed = pressed.accept)
  @deprecated def createReleaseListener(released: java.util.function.Consumer[MouseEvent]) = createMouseListener(released = released.accept)

  def createDoubleClickListener(clicked: (MouseEvent) => Unit) = createMouseListener(clicked = (e) => if (e.getClickCount == 2) clicked(e))

  def createHoldListener(pressed: (MouseEvent) => Unit, released: (MouseEvent) => Unit) = createMouseListener(pressed = pressed, released = released)

  def createMotionListener(entered: (MouseEvent) => Unit, exited: (MouseEvent) => Unit) = createMouseListener(entered = entered, exited = exited)

  def createUniversalListener(handler: (MouseEvent) => Unit) = createMouseListener(handler, handler, handler, handler, handler)

  def compose(listeners: Seq[MouseListener]) = createMouseListener(
    (e) => listeners.foreach(_.mouseClicked(e)),
    (e) => listeners.foreach(_.mousePressed(e)),
    (e) => listeners.foreach(_.mouseReleased(e)),
    (e) => listeners.foreach(_.mouseEntered(e)),
    (e) => listeners.foreach(_.mouseExited(e))
  )
}
