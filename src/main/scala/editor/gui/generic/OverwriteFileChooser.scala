package editor.gui.generic

import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File
import javax.swing.JOptionPane

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