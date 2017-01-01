package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * This class represents a dialog that presents a series of steps to the user with
 * buttons in between each step that closes after the last one or if it is
 * canceled.  Wizards are modal, so other parts of the application will not respond
 * while they are open.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class WizardDialog extends JDialog
{
	/**
	 * Amount of space around control buttons.
	 */
	private static final int BUTTON_BORDER = 5;
	
	/**
	 * Value returned when the wizard completes successfully.
	 */
	public static final int FINISH_OPTION = 0;
	/**
	 * Value returned when the wizard is canceled.
	 */
	public static final int CANCEL_OPTION = 1;
	/**
	 * Value returned when the wizard is closed.
	 */
	public static final int CLOSE_OPTION = 2;
	
	/**
	 * Show a wizard dialog with a series of components.
	 * 
	 * @param owner parent frame of the dialog
	 * @param title title of the dialog
	 * @param panels panels representing steps of the wizard
	 * @return a value indicating how the dialog was closed
	 */
	public static int showWizardDialog(Frame owner, String title, Component... panels)
	{
		return new WizardDialog(owner, title, panels).showWizard();
	}
	
	/**
	 * Show a wizard dialog with a series of components.
	 * 
	 * @param owner parent frame of the dialog
	 * @param title title of the dialog
	 * @param panels panels representing steps of the wizard
	 * @return a value indicating how the dialog was closed
	 */
	public static int showWizardDialog(Frame owner, String title, List<Component> panels)
	{
		return new WizardDialog(owner, title, panels.toArray(new Component[panels.size()])).showWizard();
	}
	
	/**
	 * Buttons used for canceling the wizard.  Each panel gets one so they can be individually
	 * enabled or disabled.
	 */
	private JButton[] cancelButtons;
	/**
	 * Buttons used for advancing the wizard.  Each panel gets one so they can be individually
	 * enabled or disabled, and so the last one can close the wizard.
	 */
	private JButton[] nextButtons;
	/**
	 * Buttons used for going back to the previous step in the wizard.  Each panel gets one so
	 * they can be individually enabled or disabled.
	 */
	private JButton[] previousButtons;
	/**
	 * Value to return indicating how the dialog was closed.
	 */
	private int result;
	
	/**
	 * Create a new WizardDialog.
	 * 
	 * @param owner parent frame of the dialog
	 * @param title title of the dialog
	 * @param panels panels representing steps of the wizard
	 */
	public WizardDialog(Frame owner, String title, Component... panels)
	{
		super(owner, title, true);
		CardLayout layout = new CardLayout();
		setLayout(layout);
		setResizable(false);
		
		result = -1;
		
		if (panels.length < 1)
			throw new IllegalArgumentException("a wizard needs at least one step");
		cancelButtons = new JButton[panels.length];
		nextButtons = new JButton[panels.length];
		previousButtons = new JButton[panels.length];
		
		for (int i = 0; i < panels.length; i++)
		{
			JPanel step = new JPanel(new BorderLayout());
			step.add(panels[i], BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel(new BorderLayout());
			step.add(buttonPanel, BorderLayout.SOUTH);
			JPanel buttons = new JPanel(new GridLayout(1, 0, BUTTON_BORDER, BUTTON_BORDER));
			buttons.setBorder(BorderFactory.createEmptyBorder(BUTTON_BORDER, BUTTON_BORDER, BUTTON_BORDER, BUTTON_BORDER));
			cancelButtons[i] = new JButton("Cancel");
			cancelButtons[i].addActionListener((e) -> {
				result = CANCEL_OPTION;
				dispose();
			});
			buttons.add(cancelButtons[i]);
			if (panels.length > 1)
			{
				previousButtons[i] = new JButton("< Previous");
				previousButtons[i].addActionListener((e) -> layout.previous(getContentPane()));
				buttons.add(previousButtons[i]);
			}
			if (i == panels.length - 1)
			{
				nextButtons[i] = new JButton("Finish");
				nextButtons[i].addActionListener((e) -> {
					result = FINISH_OPTION;
					dispose();
				});
			}
			else
			{
				nextButtons[i] = new JButton("Next >");
				nextButtons[i].addActionListener((e) -> layout.next(getContentPane()));
			}
			buttons.add(nextButtons[i]);
			buttonPanel.add(buttons, BorderLayout.EAST);
			
			add(step, String.valueOf(i));
		}
		if (panels.length > 1)
			previousButtons[0].setEnabled(false);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				result = CLOSE_OPTION;
			}
		});
		
		pack();
	}
	
	/**
	 * Enable or disable the cancel button at the given step.
	 * 
	 * @param index step to control the cancel button at
	 * @param enable whether it should be enabled or not
	 */
	public void setCancelEnabled(int index, boolean enable)
	{
		cancelButtons[index].setEnabled(enable);
	}
	
	/**
	 * Enable or disable the next button at the given step.
	 * 
	 * @param index step to control the next button at
	 * @param enable whether it should be enabled or not
	 */
	public void setNextEnabled(int index, boolean enable)
	{
		cancelButtons[index].setEnabled(enable);
	}
	
	/**
	 * Enable or disable the previous button at the given step.
	 * 
	 * @param index step to control the previous button at
	 * @param enable whether it should be enabled or not
	 */
	public void setPreviousEnabled(int index, boolean enable)
	{
		if (index != 0)
			cancelButtons[index].setEnabled(enable);
	}
	
	/**
	 * Show the wizard dialog.
	 * 
	 * @return a value indicating how the dialog was closed
	 */
	public int showWizard()
	{
		setVisible(true);
		if (result == -1)
			throw new IllegalStateException("wizard not correctly closed");
		return result;
	}
}
