package editor.util

import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener

/**
 * Convenience object for creating menu listeners so they don't have to be manually overridden every time.
 * @author Alec Roelke
 */
object MenuListenerFactory {
  /**
   * Create a new menu listener with optionally-specified behavior. Any parameters can be omitted to have the listener
   * do nothing for the corresponding event.
   * 
   * @param canceled action to perform when the menu is canceled
   * @param deselected action to perform when the menu is deselected
   * @param selected action to perform when the menu is selected
   * @return a new menu listener that performs the specified actions at the corresponding events
   */
  def createMenuListener(canceled: (MenuEvent) => Unit = _ => {}, deselected: (MenuEvent) => Unit = _ => {}, selected: (MenuEvent) => Unit = _ => {}) = new MenuListener {
    override def menuCanceled(e: MenuEvent) = canceled(e)
    override def menuDeselected(e: MenuEvent) = deselected(e)
    override def menuSelected(e: MenuEvent) = selected(e)
  }
}
