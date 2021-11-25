package editor.gui.generic

import javax.swing.JPanel
import java.awt.Component
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.UIManager
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import java.awt.event.ActionEvent
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

class ButtonScrollPane(view: Component) extends JPanel(BorderLayout()) {
  private val left = ArrowButton(ButtonDirection.West);
  add(left, BorderLayout.WEST);
  private val pane = JScrollPane(view);
  pane.setBorder(null);
  pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  add(pane, BorderLayout.CENTER);
  private val right = ArrowButton(ButtonDirection.East);
  add(right, BorderLayout.EAST);

  private val bar = pane.getHorizontalScrollBar
  left.addRepeatListener((e) => bar.getActionMap.get("negativeUnitIncrement").actionPerformed(ActionEvent(bar, ActionEvent.ACTION_PERFORMED, "", e.getWhen, e.getModifiers)));
  right.addRepeatListener((e) => bar.getActionMap.get("positiveUnitIncrement").actionPerformed(ActionEvent(bar, ActionEvent.ACTION_PERFORMED, "", e.getWhen, e.getModifiers)));

  view.addComponentListener(new ComponentListener {
    override def componentResized(e: ComponentEvent) = {
      val scrollable = view.getPreferredSize.width > pane.getSize.width;
      left.setEnabled(scrollable);
      right.setEnabled(scrollable);
    }

    override def componentHidden(e: ComponentEvent) = {}
    override def componentMoved(e: ComponentEvent) = {}
    override def componentShown(e: ComponentEvent) = {}
  });
}

// North and South are placeholders for vertical
private enum ButtonDirection { case North, East, South, West }

private class ArrowButton(direction: ButtonDirection, initial: Int = 750, tick: Int = 75) extends RepeatButton(initial, tick) {
  setFocusable(false)

  override def getPreferredSize = Dimension(12, super.getPreferredSize.height)

  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)

    val x = direction match {
      case ButtonDirection.East => Array(getWidth/3, 2*getWidth/3, getWidth/3)
      case ButtonDirection.West => Array(2*getWidth/3, getWidth/3, 2*getWidth/3)
      case _ => throw NotImplementedError(s"$direction")
    }
    val y = Array(2*getHeight/3, getHeight/2, getHeight/3)
    g.setColor(UIManager.getColor(if (isEnabled) "Button.foreground" else "Button.disabledForeground"))
    g.fillPolygon(x, y, 3)
  }
}