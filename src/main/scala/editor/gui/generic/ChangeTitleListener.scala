package editor.gui.generic

import java.awt.event.MouseAdapter
import javax.swing.JComponent
import javax.swing.border.TitledBorder
import javax.swing.JTextField
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import javax.swing.JPopupMenu
import javax.swing.BorderFactory
import java.awt.event.MouseEvent
import java.awt.Rectangle
import java.awt.Dimension

/**
 * Companion object containing additional constructors for [[ChangeTitleListener]].
 * @author Alec Roelke
 */
object ChangeTitleListener {
  /**
   * Create a new [[ChangeTitleListener]] with a constant horizontal and vertical gap for a
   * component with a [[TitledBorder]].
   * 
   * @param component component containing the border to listen to
   * @param change what to do when the title changes
   * @param hgap horizontal gap between the left edge of the component and the start of the title
   * @param vgap vertical gap between the top edge of the component and the top of the title
   * @return a new [[ChangeTitleListener]] for the [[TitledBorder]] of the component
   */
  @throws[IllegalArgumentException]("if the component doesn't have a titled border")
  def apply(component: JComponent, change: (String) => Unit, hgap: Int = 0, vgap: Int = 0) = new ChangeTitleListener(component, component.getBorder match {
    case t: TitledBorder => t
    case _ => throw IllegalArgumentException("component must have a titled border")
  }, change, _ => hgap, _ => vgap)
}

/**
 * Listener for when the title of a [[TitledBorder]] changes. Double-clicking a border initiates
 * the change, and confirming the new entry completes the change and activates the change function.
 * 
 * @constructor create a new listener for changes in the title of a border of a component
 * @param component component containing the border
 * @param border border to listen for changes in title
 * @param change what to do when the title changes
 * @param hgap horizontal gap between the edge of the component and the start of the title, based on the
 * contents of the title, used for determining when the title is double-clicked
 * @param vgap vertical gap between the edge of the component and the top of the title, based on the contents
 * of the title, used for determining when the title is double-clicked
 * 
 * @author Alec Roelke
 */
class ChangeTitleListener(component: JComponent, border: TitledBorder, change: (String) => Unit, hgap: (String) => Int, vgap: (String) => Int) extends MouseAdapter {
  @deprecated def this(component: JComponent, border: TitledBorder, change: java.util.function.Consumer[String], hgap: (String) => Int, vgap: (String) => Int) = this(component, border, change.accept(_), hgap, vgap)

  private val field = JTextField();
  private val popup = JPopupMenu();
  popup.setBorder(BorderFactory.createEmptyBorder);
  popup.add(field);

  // Accept entry when enter is pressed
  field.addActionListener(_ => {
    val value = field.getText;
    change(value);
    popup.setVisible(false);
    popup.getInvoker.revalidate();
    popup.getInvoker.repaint();
  });
  // Throw away entry when ESC is pressed
  field.registerKeyboardAction(_ => popup.setVisible(false), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
  // Implicitly throw away entry when something else is clicked

  override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) {
    val metrics = component.getFontMetrics(border.getTitleFont);
    val width = metrics.stringWidth(if (border.getTitle.isEmpty) "Change title" else border.getTitle) + 20;
    if (Rectangle(hgap.apply(border.getTitle), vgap(border.getTitle), width, metrics.getHeight).contains(e.getPoint))
    {
      field.setText(border.getTitle);
      popup.setPreferredSize(Dimension(width, field.getPreferredSize.height));
      popup.show(component, hgap(border.getTitle), vgap(border.getTitle));
      field.selectAll();
      field.requestFocusInWindow();
    }
  }
}