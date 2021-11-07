package editor.gui.generic

import javax.swing.JDialog
import java.awt.Frame
import java.awt.Component
import java.awt.Dialog
import java.awt.Window
import scala.annotation.internal.Repeated
import java.awt.CardLayout
import javax.swing.JButton
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.BorderFactory
import java.awt.event.WindowEvent
import java.awt.event.WindowAdapter
import javax.swing.SwingUtilities

object WizardDialog {
  val CloseOption = 2
  val CancelOption = 1
  val FinishOption = 0

  def apply(owner: Dialog | Frame | Window, title: String, panels: Component*) = owner match {
    case d: Dialog => new JDialog(d, title, true) with WizardDialog(panels:_*)
    case f: Frame  => new JDialog(f, title, true) with WizardDialog(panels:_*)
    case w: Window => new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL) with WizardDialog(panels:_*)
  }

  def showWizardDialog(owner: Component, title: String, panels: Component*) = {
    val window = owner match {
      case w: Window => w
      case _ => SwingUtilities.getWindowAncestor(owner)
    }
    val dialog = apply(window, title, panels:_*)
    dialog.setLocationRelativeTo(owner)
    dialog.showWizard()
  }
}

trait WizardDialog private(panels: Component*) extends JDialog {
  import WizardDialog._
  private val ButtonBorder = 5

  private val layout = CardLayout()
  setLayout(layout);
  setResizable(false);

  private var result = -1

  if (panels.length < 1)
    throw IllegalArgumentException("a wizard needs at least one step");
  private case class ControlButtons(cancel: JButton, previous: Option[JButton], next: JButton)
  private val controls = new Array[ControlButtons](panels.length)

  for (i <- 0 until panels.length) {
    val step = JPanel(BorderLayout());
    step.add(panels(i), BorderLayout.CENTER);

    val buttonPanel = JPanel(BorderLayout());
    step.add(buttonPanel, BorderLayout.SOUTH);
    val buttons = JPanel(GridLayout(1, 0, ButtonBorder, ButtonBorder));
    buttons.setBorder(BorderFactory.createEmptyBorder(ButtonBorder, ButtonBorder, ButtonBorder, ButtonBorder));
    buttonPanel.add(buttons, BorderLayout.EAST)
    add(step, i.toString)

    controls(i) = ControlButtons(JButton("Cancel"), Option.when(panels.length > 1)(JButton("< Previous")), JButton(if (i == panels.length - 1) "Finish" else "Next >"))
    controls(i).cancel.addActionListener(_ => {
      result = CancelOption
      dispose()
    })
    buttons.add(controls(i).cancel)
    controls(i).previous.foreach(b => {
      b.addActionListener(_ => layout.previous(getContentPane))
      buttons.add(b)
    })
    controls(i).next.addActionListener(if (i == panels.length - 1) _ => {
      result = FinishOption
      dispose()
    } else _ => layout.next(getContentPane))
    buttons.add(controls(i).next)
  }
  if (panels.length > 1)
    controls(0).previous.foreach(_.setEnabled(false))

  addWindowListener(new WindowAdapter { override def windowClosing(e: WindowEvent) = result = CloseOption });
  pack();

  def setCancelEnabled(index: Int, enable: Boolean) = controls(index).cancel.setEnabled(enable)

  def setPreviousEnabled(index: Int, enable: Boolean) = if (index != 0) controls(index).previous.foreach(_.setEnabled(enable))

  def setNextEnabled(index: Int, enable: Boolean) = controls(index).next.setEnabled(enable)

  def showWizard() = {
    setVisible(true)
    if (result == -1)
      throw IllegalStateException("wizard not currently closed")
    result
  }
}