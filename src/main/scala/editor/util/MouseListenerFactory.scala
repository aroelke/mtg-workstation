package editor.util

import java.awt.event.MouseEvent
import java.awt.event.MouseListener

/**
 * Factory for conveniently creating [[MouseListener]]s so the class doesn't have to be manually overridden every time.
 * @author Alec Roelke
 */
object MouseListenerFactory {
  /**
   * Create a new [[MouseListener]] that performs the given actions on each event. Events can be omitted to have them do nothing.
   * 
   * @param clicked action to perform when a mouse button is clicked
   * @param pressed action to perform when a mouse button is pressed
   * @param released action to perform when a mouse button is released
   * @param entered action to perform when the mouse enters a component
   * @param exited action to perform when the mouse leaves a component
   * @return a [[MouseListener]] that performs the given actions for the given events
   */
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

  /**
   * Create a mouse listener that specifically listens for a double click.
   * 
   * @param clicked action to perform when a mouse button is double-clicked
   * @return a mouse listener that performs the given action when the mouse is double-clicked
   */
  def createDoubleClickListener(clicked: (MouseEvent) => Unit) = createMouseListener(clicked = (e) => if (e.getClickCount == 2) clicked(e))

  /**
   * Convenience method for creating a [[MouseListener]] that performs actions when a mouse button is pressed
   * and when it is released, named to emphasize its intended purpose of performing the action while the
   * button is held.
   * 
   * @param pressed action to perform when the mouse button is pressed (e.g. start a timer)
   * @param released action to perform when the mouse button is released (e.g. stop a timer)
   * @return a [[MouseListener]] that listens for only press and release events
   */
  def createHoldListener(pressed: (MouseEvent) => Unit, released: (MouseEvent) => Unit) = createMouseListener(pressed = pressed, released = released)

  /**
   * Convenience method for creating a [[MouseListener]] that listens for the mouse to enter and exit a component,
   * named to emphasize its intended purpose.
   * 
   * @param entered action to perform when the mouse enters the component
   * @param exited action to perform when the mouse exits the component
   * @return a [[MouseListener]] that listens for the mouse to enter or exit a component
   */
  def createMotionListener(entered: (MouseEvent) => Unit, exited: (MouseEvent) => Unit) = createMouseListener(entered = entered, exited = exited)

  /**
   * Create a [[MouseListener]] that does the same thing for all [[MouseEvent]]s.
   * 
   * @param handler action to perform when the mouse is clicked, pressed, or released, or it enters or exits a component
   * @return a [[MouseListener]] that performs a single action for all events
   */
  def createUniversalListener(handler: (MouseEvent) => Unit) = createMouseListener(handler, handler, handler, handler, handler)

  /**
   * Create a [[MouseListener]] that forwards events to other [[MouseListeners]] in a specific order, as opposed to simply adding them
   * all to the same component via `addMouseListener`, which does not specify the order in which they fire.
   * 
   * @param listeners listeners to compose
   * @return a new [[MouseListener]] that forwards all events to the component listeners in order
   */
  def compose(listeners: Seq[MouseListener]) = createMouseListener(
    (e) => listeners.foreach(_.mouseClicked(e)),
    (e) => listeners.foreach(_.mousePressed(e)),
    (e) => listeners.foreach(_.mouseReleased(e)),
    (e) => listeners.foreach(_.mouseEntered(e)),
    (e) => listeners.foreach(_.mouseExited(e))
  )
}
