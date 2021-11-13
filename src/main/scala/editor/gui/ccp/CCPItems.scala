package editor.gui.ccp

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.JMenuItem
import javax.swing.KeyStroke
import javax.swing.TransferHandler

/**
 * Generates sets of cut, copy, and paste menu items with behaviors.
 * @author Alec Roelke
 */
object CCPItems {
  /**
   * Create a new set of cut, copy, and paste menu items and add default associated behavior with the option to
   * add keyboard accelerators (Ctrl + key).  Accelerators used are Ctrl+X for cut, Ctrl+C for copy, and Ctrl+V
   * for paste.
   * 
   * @param source component performing the cut, copy, and/or paste actions
   * @param accelerate whether or not to add keyboard accelerators
   * @return a new set of cut, copy, and paste menu items with default behaviors added
   */
  def apply(source: => Component, accelerate: Boolean): CCPItems = {
    val items = CCPItems()

    items.cut.addActionListener(_ => TransferHandler.getCutAction.actionPerformed(ActionEvent(source, ActionEvent.ACTION_PERFORMED, null)));
    items.copy.addActionListener(_ => TransferHandler.getCopyAction.actionPerformed(ActionEvent(source, ActionEvent.ACTION_PERFORMED, null)));
    items.paste.addActionListener(_ => TransferHandler.getPasteAction.actionPerformed(ActionEvent(source, ActionEvent.ACTION_PERFORMED, null)));

    if (accelerate) {
      items.cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
      items.copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
      items.paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
    }

    items
  }
}

/**
 * Structure defining menu items for cut, copy, and paste. By default, these don't have actions associated; use
 * the companion object to automatically generate that.
 * 
 * @constructor create a new set of cut, copy, and paste menu items
 * @param cut menu item for cut; defaults to a menu item with the text "Cut"
 * @param copy menu item for copy; defaults to a menu item with the text "Copy"
 * @param menu item for paste; defaults to a menu item with the text "Paste"
 * 
 * @author Alec Roelke
 */
case class CCPItems(cut: JMenuItem = JMenuItem("Cut"), copy: JMenuItem = JMenuItem("Copy"), paste: JMenuItem = JMenuItem("Paste")) {
  @deprecated def this(source: Component, accelerate: Boolean) = {
    this()

    cut.addActionListener(_ => TransferHandler.getCutAction.actionPerformed(ActionEvent(source, ActionEvent.ACTION_PERFORMED, null)));
    copy.addActionListener(_ => TransferHandler.getCopyAction.actionPerformed(ActionEvent(source, ActionEvent.ACTION_PERFORMED, null)));
    paste.addActionListener(_ => TransferHandler.getPasteAction.actionPerformed(ActionEvent(source, ActionEvent.ACTION_PERFORMED, null)));

    if (accelerate) {
      cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
      copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
      paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
    }
  }
}