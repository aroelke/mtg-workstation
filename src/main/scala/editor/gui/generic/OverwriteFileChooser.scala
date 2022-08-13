package editor.gui.generic

import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * File chooser that asks permission to replace existing files. A "Yes" choice replaces the file,
 * "No" returns to the chooser for renaming, and "Cancel" or quit simply exits without doing any
 * file operations.
 * 
 * @constructor create a new file chooser that asks permission to overwrite files
 * @param cwd starting directory of the file chooser, or user's default directory (as specified by
 * [[JFileChooser]]) if not set
 * 
 * @author Alec Roelke
 */
class OverwriteFileChooser(cwd: String = JFileChooser().getCurrentDirectory.getAbsolutePath) extends JFileChooser(cwd) {
  override def getSelectedFile = {
    Option(super.getSelectedFile).map{ file => getFileFilter match {
      case filter: FileNameExtensionFilter if !filter.getExtensions.exists((ext) => file.getAbsolutePath.endsWith(s".$ext")) => File(s"${file.getAbsolutePath}.${filter.getExtensions.apply(0)}")
      case _ => file
    }}.getOrElse(null)
  }

  override def approveSelection() = {
    if (getSelectedFile.exists && getDialogType == JFileChooser.SAVE_DIALOG) {
      JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION) match {
        case JOptionPane.YES_OPTION => super.approveSelection
        case JOptionPane.NO_OPTION =>
        case JOptionPane.CANCEL_OPTION => cancelSelection()
        case JOptionPane.CLOSED_OPTION =>
      }
    } else {
      super.approveSelection()
    }
  }
}