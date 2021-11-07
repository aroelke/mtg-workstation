package editor.gui.generic

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Frame
import java.awt.GridLayout
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.SwingUtilities
import scala.annotation.internal.Repeated

/**
 * Possible results of closing a [[WizardDialog]], indicating whether or not it was closed (via close
 * button), canceled (via the cancel button), or finished (via the finish button).
 * 
 * @author Alec Roelke
 */
enum WizardResult {
  case CloseOption, CancelOption, FinishOption
}

/**
 * Companion object to [[WizardDialog]] used to facilitate instantiating it and getting results from it.
 * @author Alec Roelke
 */
object WizardDialog {
  /**
   * Create a new [[WizardDialog]]. The dialog will be modal, meaning it will block all inputs to its parent
   * container.
   * 
   * @param owner parent container of the new dialog
   * @param title title of the dialog
   * @param panels panels to use for each step of the wizard
   * @return a new wizard dialog with the given title and steps
   */
  def apply(owner: Dialog | Frame | Window, title: String, panels: Component*) = owner match {
    case d: Dialog => new JDialog(d, title, true) with WizardDialog(panels:_*)
    case f: Frame  => new JDialog(f, title, true) with WizardDialog(panels:_*)
    case w: Window => new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL) with WizardDialog(panels:_*)
  }

  /**
   * Create a new [[WizardDialog]], show it, and the return the result of the dialog. Note that the result
   * simply indicates how it was closed, and the data entered into the panels used to generate it can be used
   * to get the result of the sequence.
   * 
   * @param owner parent container of the dialog
   * @param title title of the dialog
   * @param panels panels containing the steps of the sequence
   * @return the result of closing the dialog
   */
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

/**
 * A dialog with a sequence of panels containing content meant for generating a result in a series of steps.
 * 
 * Since its parent class, [[JDialog]], has three "main" constructors that are different depending on the
 * type of its parent container, and Scala doesn't support inheritance with multiple different super
 * constructors, an instance of WizardDialog should be created by instantiating a [[JDialog]] and mixing in
 * this trait, or by simply using the companion [[WizardDialog.apply]] method.
 * 
 * @constructor create a new wizard dialog using the given panels as steps in the sequence
 * @param panels panels used to customize each step in generating the desired result
 * 
 * @author Alec Roelke
 */
trait WizardDialog(panels: Component*) extends JDialog {
  import WizardDialog._
  import WizardResult._
  private val ButtonBorder = 5

  private val layout = CardLayout()
  setLayout(layout)
  setResizable(false)

  private var result: Option[WizardResult] = None

  if (panels.length < 1)
    throw IllegalArgumentException("a wizard needs at least one step")

  private case class ControlButtons(cancel: JButton, previous: Option[JButton], next: JButton)
  private val controls = panels.zipWithIndex.map{ case (panel, i) =>
    val step = JPanel(BorderLayout())
    step.add(panels(i), BorderLayout.CENTER)

    val buttonPanel = JPanel(BorderLayout())
    step.add(buttonPanel, BorderLayout.SOUTH)
    val buttons = JPanel(GridLayout(1, 0, ButtonBorder, ButtonBorder))
    buttons.setBorder(BorderFactory.createEmptyBorder(ButtonBorder, ButtonBorder, ButtonBorder, ButtonBorder))
    buttonPanel.add(buttons, BorderLayout.EAST)
    add(step, i.toString)

    val control = ControlButtons(JButton("Cancel"), Option.when(panels.length > 1)(JButton("< Previous")), JButton(if (i == panels.length - 1) "Finish" else "Next >"))
    control.cancel.addActionListener(_ => {
      result = Some(CancelOption)
      dispose()
    })
    buttons.add(control.cancel)
    control.previous.foreach(b => {
      b.addActionListener(_ => layout.previous(getContentPane))
      buttons.add(b)
    })
    control.next.addActionListener(if (i == panels.length - 1) _ => {
      result = Some(FinishOption)
      dispose()
    } else _ => layout.next(getContentPane))
    buttons.add(control.next)

    control
  }
  controls(0).previous.foreach(_.setEnabled(false))

  addWindowListener(new WindowAdapter { override def windowClosing(e: WindowEvent) = result = Some(CloseOption) })
  pack()

  /**
   * Set whether or not the cancel button in a stage should be enabled.
   * 
   * @param index stage with the cancel button to change
   * @param enable whether or not to enable the stage's cancel button
   */
  def setCancelEnabled(index: Int, enable: Boolean) = controls(index).cancel.setEnabled(enable)

  /**
   * Set whether or not the previous button in a stage should be enabled, if that stage isn't 0. Does nothing if the stage
   * is 0.
   * 
   * @param index stage with the previous button to change
   * @param enable whether or not to enable the stage's previous button
   */
  def setPreviousEnabled(index: Int, enable: Boolean) = if (index != 0) controls(index).previous.foreach(_.setEnabled(enable))

  /**
   * Set whether or not the next button in a stage should be enabled.
   * 
   * @param index stage with the next button to change
   * @param enable whether or not the enable the stage's next button
   */
  def setNextEnabled(index: Int, enable: Boolean) = controls(index).next.setEnabled(enable)

  def showWizard() = {
    setVisible(true)
    result.getOrElse(throw IllegalStateException("wizard not currently closed"))
  }
}