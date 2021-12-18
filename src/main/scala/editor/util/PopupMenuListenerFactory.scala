package editor.util

import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

/**
 * Convenience object for creating [[PopupMenuListener]]s.
 * @author Alec Roelke
 */
object PopupMenuListenerFactory {
  /**
   * Create a new [[PopupMenuListener]] that performs the given actions for its events. Leaving an action unspecified will cause it to be
   * ignored.
   * 
   * @param cancel action to perform when the popup is canceled
   * @param invisible action to perform just before the popup becomes invisibler
   * @param visible action to perform just before the popup becomes visible
   * @return a new [[PopupMenuListener]] that performs the given actions
   */
  def createPopupListener(cancel: (PopupMenuEvent) => Unit = _ => {}, invisible: (PopupMenuEvent) => Unit = _ => {}, visible: (PopupMenuEvent) => Unit = _ => {}) = new PopupMenuListener {
    override def popupMenuCanceled(e: PopupMenuEvent) = cancel(e)
    override def popupMenuWillBecomeInvisible(e: PopupMenuEvent) = invisible(e)
    override def popupMenuWillBecomeVisible(e: PopupMenuEvent) = visible(e)
  }
  @deprecated def createVisibleListener(visible: java.util.function.Consumer[PopupMenuEvent]) = createPopupListener(visible = visible.accept)
}
