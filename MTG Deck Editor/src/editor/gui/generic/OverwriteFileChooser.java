package editor.gui.generic;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This is a {@link JFileChooser} that confirms first if a file should be overwritten.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class OverwriteFileChooser extends JFileChooser
{
	/**
	 * Create a new OverwriteFileChooser.
	 */
	public OverwriteFileChooser()
	{
		super();
	}
	
	/**
	 * Create a new OverwriteFileChooser that starts in the given path
	 * 
	 * @param currentDirectoryPath path to start in
	 */
	public OverwriteFileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}
	
	@Override
	public void approveSelection()
	{
		if (getSelectedFile().exists() && getDialogType() == SAVE_DIALOG)
		{
			switch (JOptionPane.showConfirmDialog(this, "File already exists.  Overwrite?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION))
			{
			case JOptionPane.YES_OPTION:
				super.approveSelection();
				return;
			case JOptionPane.NO_OPTION:
				return;
			case JOptionPane.CANCEL_OPTION:
				cancelSelection();
				return;
			case JOptionPane.CLOSED_OPTION:
				return;
			}
		}
		super.approveSelection();
	}
}
