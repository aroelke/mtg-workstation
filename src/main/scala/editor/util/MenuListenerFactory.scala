package editor.util

import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener

object MenuListenerFactory {
  def createMenuListener(canceled: (MenuEvent) => Unit = _ => {}, deselected: (MenuEvent) => Unit = _ => {}, selected: (MenuEvent) => Unit = _ => {}) = new MenuListener {
    override def menuCanceled(e: MenuEvent) = canceled(e)
    override def menuDeselected(e: MenuEvent) = deselected(e)
    override def menuSelected(e: MenuEvent) = selected(e)
  }
}
