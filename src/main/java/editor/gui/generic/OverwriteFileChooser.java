package editor.gui.generic;

import java.io.File;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

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

    /**
     * {@inheritDoc}
     * <p>
     * If the file name doesn't have an extension specified by the file filter, add it.
     */
    @Override
    public File getSelectedFile()
    {
        if (getFileFilter() instanceof FileNameExtensionFilter)
        {
            FileNameExtensionFilter filter = (FileNameExtensionFilter)getFileFilter();
            String fname = super.getSelectedFile().getAbsolutePath();
            if (Arrays.stream(filter.getExtensions()).noneMatch((ext) -> fname.endsWith("." + ext)))
                return new File(fname + "." + filter.getExtensions()[0]);
        }
        return super.getSelectedFile();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the file already exists, make sure the user wants to replace it first. If not,
     * allow them to make another choice or close the dialog depending on what was
     * selected.
     */
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
