package editor

import editor.gui.MainFrame
import editor.gui.settings.SettingsDialog

import java.io.File
import java.io.IOException
import java.nio.file.Files
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

/**
 * Main class for starting the program.
 * @author Alec Roelke
 */
object EditorApp {
  /**
   * Create a new [[editor.gui.MainFrame]] and open it with the given set of files.
   * @param files decks to open
   */
  @main def main(files: String*) = {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    } catch {
      case e @ (_: ClassNotFoundException | _: InstantiationException | _: IllegalAccessException | _: UnsupportedLookAndFeelException | _: Exception) => e.printStackTrace
    }

    try {
      Files.createDirectories(SettingsDialog.EditorHome)
    } catch {
      case e: IOException => JOptionPane.showMessageDialog(null, s"Could not create directory ${SettingsDialog.EditorHome}: ${e.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
    }

    SwingUtilities.invokeLater(() => MainFrame(files.map(File(_)).filter(_.exists)).setVisible(true))
  }
}
