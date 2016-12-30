package editor.gui.generic;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class OverwriteFileChooser extends JFileChooser
{
	public OverwriteFileChooser()
	{
		super();
	}
	
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
