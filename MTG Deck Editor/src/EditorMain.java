import gui.MainFrame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This class contains the entry point for the program. It creates the GUI using
 * the system look and feel.
 * 
 * @author Alec Roelke
 */
public class EditorMain
{
	/**
	 * Entry point for the program. All it does is set the look and feel to the
	 * system one and create the GUI.
	 * 
	 * @param args Arguments to the program
	 */
	public static void main(String[] args)
	{
		List<File> files = new ArrayList<File>();
		for (String arg: args)
		{
			File f = new File(arg);
			if (f.exists())
				files.add(f);
		}
		
		// TODO: Try to reduce memory footprint.
		// TODO: Decide on nimbus or default look and feel (may require resizing some things)
		try
		{
			/*
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
			*/
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.invokeLater(() -> new MainFrame(files).setVisible(true));
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
