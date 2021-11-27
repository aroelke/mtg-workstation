package editor.gui.generic

import editor.util.MouseListenerFactory

import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.OverlayLayout
import javax.swing.SwingUtilities
import javax.swing.plaf.basic.BasicButtonUI

object EditablePanel {
  val Close = "close"
  val Edit = "edit"
  val Cancel = "cancel"
}

/**
 * Panel containing a label and a close button.  Double-clicking the label allows the user to edit
 * its contents, which fires an event when finished (whether it was confirmed or canceled), and clicking
 * the close button fires another event.
 * 
 * @constructor create a new panel with a title belonging to another component
 * @param t initial title for the label in the panel
 * @param parent owner of the label to be notified of events
 * 
 * @author Alec Roelke
 */
class EditablePanel(t: String, parent: Option[Component] = None) extends JPanel {
  import EditablePanel._

  /** Listeners for edit, cancel, and close events. */
  val listeners = collection.mutable.Set[ActionListener]()

  private class CloseButton extends JButton {
    private val CloseSize = 17

    setPreferredSize(Dimension(CloseSize, CloseSize))
    setUI(BasicButtonUI())
    setContentAreaFilled(false)
    addMouseListener(MouseListenerFactory.createMotionListener(_ => setBorderPainted(true), _ => setBorderPainted(false)))
    setRolloverEnabled(true)
    addMouseListener(MouseListenerFactory.createHoldListener(_ => setBorder(BorderFactory.createLoweredBevelBorder), _ => setBorder(BorderFactory.createRaisedBevelBorder)))
    addActionListener(_ => listeners.foreach(_.actionPerformed(ActionEvent(EditablePanel.this, ActionEvent.ACTION_PERFORMED, Close))))

    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      g match {
        case g2: Graphics2D => 
          g2.setColor(if (getModel.isRollover) Color.RED else Color.BLACK)
          g2.setStroke(BasicStroke(1))
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          if (getModel.isPressed)
            g2.translate(1, 1)
          g2.drawLine(getWidth/3, getHeight/3, getWidth*2/3, getHeight*2/3)
          g2.drawLine(getWidth/3, getHeight*2/3, getWidth*2/3, getHeight/3)
          g2.dispose()
        case _ => throw IllegalArgumentException("expected Graphics2D")
      }
    }
  }

  setLayout(OverlayLayout(this))
  setOpaque(false)
  private var old: Option[String] = None

  private val front = JPanel(BorderLayout())
  front.setOpaque(false)
  private val label = JLabel(t)
  front.add(label, BorderLayout.WEST)
  front.add(Box.createHorizontalStrut(2), BorderLayout.CENTER)
  front.add(CloseButton(), BorderLayout.EAST)
  add(front)
  label.setFocusable(false)

  private val field = JTextField(t)
  field.setBorder(BorderFactory.createEmptyBorder)
  field.setVisible(false)
  field.addActionListener(_ => finish(true))
  field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Cancel)
  field.getActionMap.put(Cancel, new AbstractAction { override def actionPerformed(e: ActionEvent) = finish(false) })
  add(field)

  label.addMouseListener(MouseListenerFactory.composeListeners(
    MouseListenerFactory.createUniversalListener((e) => {
      parent.foreach(p => p.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent, e, p)))
    }),
    MouseListenerFactory.createDoubleClickListener(_ => {
      front.setVisible(false)
      field.setText(label.getText)
      field.setVisible(true)
      field.requestFocusInWindow()
      field.selectAll()
    })
  ))

  field.addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent) = {}
    override def focusLost(e: FocusEvent) = finish(false)
  })

  private def finish(commit: Boolean) = {
    front.setVisible(true)
    field.setVisible(false)
    field.transferFocusUpCycle()
    if (commit) {
      old = Some(label.getText)
      label.setText(field.getText)
      listeners.foreach(_.actionPerformed(ActionEvent(this, ActionEvent.ACTION_PERFORMED, Edit)))
    } else {
      field.setText(label.getText)
      listeners.foreach(_.actionPerformed(ActionEvent(this, ActionEvent.ACTION_PERFORMED, Cancel)))
    }
  }

  /** @return the current text in the label */
  def title = label.getText

  /** @param t new text for the label */
  def title_=(t: String) = {
    old = Some(label.getText)
    label.setText(t)
  }

  /** @return the previous text in the label from before the last change, or None if it hasn't been changed */
  def previousTitle = old
}