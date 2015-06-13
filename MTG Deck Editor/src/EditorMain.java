import gui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This class contains the entry point for the program.  It creates the GUI using the system
 * look and feel.
 * 
 * @author Alec Roelke
 */
public class EditorMain
{
	/**
	 * Entry point for the program.  All it does is set the look and feel to the system
	 * one and create the GUI.
	 * 
	 * @param args Arguments to the program
	 */
	public static void main(String[] args)
	{
		// TODO: Allow for opening one or more decks using the command line.
		// TODO: Show a loading bar for loading the inventory
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
