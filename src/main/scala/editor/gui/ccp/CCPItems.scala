package editor.gui.ccp

import javax.swing.JMenuItem
import java.awt.Component
import javax.swing.TransferHandler
import java.awt.event.ActionEvent
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.event.InputEvent

object CCPItems {
  def apply(source: => Component, accelerate: Boolean): CCPItems = {
    val items = CCPItems(JMenuItem("Cut"), JMenuItem("Copy"), JMenuItem("Paste"))

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

case class CCPItems(cut: JMenuItem, copy: JMenuItem, paste: JMenuItem) {
  @deprecated def this(source: Component, accelerate: Boolean) = {
    this(JMenuItem("Cut"), JMenuItem("Copy"), JMenuItem("Paste"))

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