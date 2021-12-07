package editor.gui.inventory

import javax.swing.SwingWorker
import java.net.URL
import java.io.File
import scala.util.Using
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import scala.jdk.CollectionConverters._
import java.util.zip.ZipInputStream
import java.io.FileInputStream
import java.awt.Frame
import java.io.IOException
import javax.swing.JDialog
import java.awt.Dialog
import java.awt.Dimension
import javax.swing.WindowConstants
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.JButton
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import javax.swing.JComponent
import java.util.concurrent.Executors
import edu.emory.mathcs.backport.java.util.concurrent.ExecutionException
import javax.swing.JOptionPane
import edu.emory.mathcs.backport.java.util.concurrent.CancellationException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object InventoryDownloader {
  def downloadInventory(owner: Frame, site: URL, file: File) = {
    val zip = File(s"${file.getPath}.zip")
    val tmp = File(s"${zip.getPath}.tmp")

    val dialog = JDialog(owner, "Update", Dialog.ModalityType.APPLICATION_MODAL)
    dialog.setPreferredSize(Dimension(350, 115))
    dialog.setResizable(false)
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    val unzipper = UnzipWorker(zip, file)

    // Content panel
    val contentPanel = JPanel(BorderLayout(0, 2))
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    dialog.setContentPane(contentPanel)

    // Stage progress label
    val progressLabel = JLabel("Downloading inventory...")
    contentPanel.add(progressLabel, BorderLayout.NORTH)

    // Overall progress bar
    val progressBar = JProgressBar()
    progressBar.setIndeterminate(true)
    contentPanel.add(progressBar, BorderLayout.CENTER)

    val downloader = DownloadWorker(site, tmp)
    if (downloader.size >= 0)
      progressBar.setMaximum(downloader.size)
    downloader.updater = (downloaded) => {
      def formatDownload(n: Int) = {
        if (n < 0)
          ""
        else if (n <= 1024)
          f"$n%d"
        else if (n <= 1048576)
          f"${n/1024.0}%.1fk"
        else
          f"${n/1048576.0}%.2fM"
      }

      val progress = collection.mutable.StringBuilder()
      progress ++= s"Downloading inventory... ${formatDownload(downloaded)}"
      if (downloader.size < 0)
        progressBar.setVisible(false)
      else {
        progressBar.setIndeterminate(false)
        progressBar.setValue(downloaded)
        progress ++= s"B/${formatDownload(downloader.size)}"
      }
      progress ++= "B downloaded."
      progressLabel.setText(progress.toString)
    }

    // Cancel button
    val cancelPanel = JPanel()
    val cancelButton = JButton("Cancel")
    def cancelAction() = {
      downloader.cancel(true)
      unzipper.cancel(true)
    }
    cancelButton.addActionListener(_ => cancelAction())
    cancelPanel.add(cancelButton)
    contentPanel.add(cancelPanel, BorderLayout.SOUTH)
    dialog.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) = cancelAction()
    })
    dialog.getRootPane.registerKeyboardAction(_ => cancelAction(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)

    dialog.pack()
    dialog.setLocationRelativeTo(owner)

    val future = Executors.newSingleThreadExecutor().submit(() => {
      downloader.execute()
      var success = try {
        downloader.get()
        true
      } catch {
        case e @ (_: InterruptedException | _: ExecutionException) =>
          JOptionPane.showMessageDialog(owner, s"Error downloading ${zip.getName}: ${e.getCause.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
          tmp.delete()
          dialog.setVisible(false)
          false
        case e: CancellationException =>
          tmp.delete
          dialog.setVisible(false)
          false
      }
      if (success) {
        success = try {
          Files.move(tmp.toPath, zip.toPath, StandardCopyOption.REPLACE_EXISTING)
          true
        } catch case e: IOException => {
          JOptionPane.showMessageDialog(owner, s"Could not replace temporary file: ${e.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
          dialog.setVisible(false)
          false
        }
      }
      if (success) {
        progressLabel.setText("Unzipping archive...")
        unzipper.execute()
        success = try {
          unzipper.get()
          true
        } catch {
          case e @ (_: InterruptedException | _: ExecutionException) =>
            JOptionPane.showMessageDialog(owner, s"Error decompressing ${zip.getName}: ${e.getCause.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
            dialog.setVisible(false)
            false
          case e: CancellationException =>
            dialog.setVisible(false)
            file.delete()
            false
        } finally {
          zip.delete()
        }
      }
      if (success)
        dialog.setVisible(false)
      success
    })
    dialog.setVisible(true)
    try {
      future.get()
    } catch {
      case _ @ (_: InterruptedException | _: ExecutionException) => false
    } finally {
      dialog.dispose()
    }
  }
}

private class DownloadWorker(url: URL, file: File) extends SwingWorker[Unit, Integer] {
  private val connection = url.openConnection()

  val size = connection.getContentLength
  var updater: (Int) => Unit = (i) => {}

  protected override def doInBackground() = Using.resources(BufferedInputStream(connection.getInputStream), BufferedOutputStream(FileOutputStream(file))){ (in, out) =>
    val data = new Array[Byte](1024)
    var sz = 0
    var x = 0
    while (!isCancelled && { x = in.read(data); x } > 0) {
      sz += x
      out.write(data, 0, x)
      publish(sz)
    }
  }

  protected override def process(chunks: java.util.List[Integer]) = updater(chunks.asScala.last)
}

private class UnzipWorker(zipfile: File, outfile: File) extends SwingWorker[Unit, Unit] {
  protected override def doInBackground() = Using.resources(ZipInputStream(FileInputStream(zipfile)), BufferedOutputStream(FileOutputStream(outfile))){ (zis, out) =>
    zis.getNextEntry()
    val data = new Array[Byte](1024)
    var x = 0
    while (!isCancelled && { x = zis.read(data); x } > 0)
      out.write(data, 0, x)
  }
}