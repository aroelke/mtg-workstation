package editor.gui.deck

import editor.collection.format.CardListFormat
import editor.collection.mutable.Deck
import editor.gui.MainFrame
import editor.serialization
import editor.serialization.given
import editor.util.ProgressInputStream
import org.json4s._
import org.json4s.native._

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.FlowLayout
import java.awt.Window
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.ObjectInput
import java.io.ObjectInputStream
import java.lang.reflect.Type
import java.nio.file.NoSuchFileException
import java.text.SimpleDateFormat
import java.util.NoSuchElementException
import java.util.concurrent.CancellationException
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingWorker
import scala.jdk.CollectionConverters._
import scala.util.Using

/**
 * Companion object containing global information about serializing decks and for creating new [[DeckSerializer]]s.
 * @author Alec Roelke
 */
object DesignSerializer extends CustomSerializer[DesignSerializer](implicit formats => (
  { case JObject(JField("main", main) :: JField("sideboards", JArray(sideboards)) :: JField("notes", JString(notes)) :: JField("changelog", JString(changelog)) :: Nil) =>
    DesignSerializer(main.extract[Deck], sideboards.map((e) => (e \ "name").extract[String] -> e.extract[Deck]).toMap, notes, changelog) },
  { case DesignSerializer(deck, sideboards, notes, changelog, _) => JObject(List(
    JField("main", Extraction.decompose(deck)),
    JField("sideboards", JArray(sideboards.map{ case (name, sb) => JObject(Extraction.decompose(sb) match {
      case JObject(obj) => JField("name", JString(name)) +: obj
      case _ => throw MatchError("a deck should be a JObject")
    }) }.toList)),
    JField("notes", JString(notes)),
    JField("changelog", JString(changelog))
  )) }
)) {
  /** @return the format used to serialize dates (e.g. for when a card was added to the deck) */
  def ChangelogDateFormat = SimpleDateFormat("MMMM d, yyyy HH:mm:ss")

  /**
   * Load a deck from a JSON file.  Display a dialog indicating progress in parsing the file.
   * 
   * @param file file to load from
   * @param parent component doing the loading, for positioning the progress dialog
   * @return a [[DeckSerializer]] containing the data parsed from the file
   */
  @throws[DeckLoadException]("if the deck couldn't be loaded (not including user cancellation)")
  def load(file: File, parent: Component) = {
    val worker = LoadWorker(file, parent, (stream) => Using.resource(BufferedReader(InputStreamReader(stream)))((s) => {
      Extraction.extract[DesignSerializer](JsonMethods.parse(s))
    }))
    worker.executeAndDisplay()
    try {
      worker.get.copy(file = Some(file))
    } catch {
      case _: CancellationException => DesignSerializer()
      case e => throw DeckLoadException(file, cause = Some(e))
    }
  }

  /**
   * Import deck data from a supported non-JSON format. Display a dialog indicating progressin parsing the file.
   * The file that is opened cannot be saved back to using a normal "Save" action; doing so saves a new JSON file.
   * Overwriting the old file can only be done using the export feature.
   * 
   * @param format format to use to parse the file
   * @param file file to load from
   * @param parent component doing the loading, used for positioning the progress dialog
   * @return a [[DeckSerializer]] containing the data parsed from the file
   */
  @throws[DeckLoadException]("if the list couldn't be imported (not including user cancellation)")
  def importList(format: CardListFormat, file: File, parent: Component) = {
    val worker = LoadWorker(file, parent, format.parse(_))
    worker.executeAndDisplay()
    try {
      worker.get
    } catch {
      case _: CancellationException => DesignSerializer()
      case e => throw DeckLoadException(file, cause = Some(e))
    }
  }
}

/**
 * Structure useful for serializing and deserializing deck data to and from JSON format.  The serialized deck contains the main contents
 * of the deck, any extra lists included alongside it, any user-defined notes, and the changelog.
 * 
 * @constructor create a new deck serializer
 * @param deck main deck contents
 * @param sideboards extra lists with the deck
 * @param notes user-defined notes
 * @param changelog changes made since the deck was created
 * @param file file to save the serialized deck to
 * 
 * @author Alec Roelke
 */
case class DesignSerializer(deck: Deck = Deck(), sideboards: Map[String, Deck] = Map.empty, notes: String = "", changelog: String = "", file: Option[File] = None) {
  /** Save the serialized deck to a JSON file, if the file is defined. */
  @throws[IOException]("if the file could not be saved")
  @throws[NoSuchFileException]("if there is no file to save to")
  def save() = file.map((f) => Using.resource(FileWriter(f)){ writer =>
    writer.write(JsonMethods.pretty(JsonMethods.render(Extraction.decompose(this))))
  }).getOrElse(throw NoSuchFileException("no file to save to"))
}

/**
 * Helper class for loading a deck in the background. Shows a dialog indicating load progress.
 * 
 * @constructor create a new background load worker
 * @param file file to load from
 * @param parent component doing the loading, for positioning the dialog
 * @param background function converting the data from the file input stream into a [[DeckSerializer]]
 */
private class LoadWorker(file: File, parent: Component, background: (InputStream) => DesignSerializer) extends SwingWorker[DesignSerializer, Integer] {
  private val dialog = JDialog(null, Dialog.ModalityType.APPLICATION_MODAL)
  private val progressBar = JProgressBar(0, file.length.toInt)
  private val progressPanel = JPanel(BorderLayout(0, 5))
  dialog.setContentPane(progressPanel)
  progressPanel.add(JLabel(s"Opening ${file.getName}..."), BorderLayout.NORTH)
  progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
  progressPanel.add(progressBar, BorderLayout.CENTER)
  private val cancelPanel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0))
  private val cancelButton = JButton("Cancel")
  cancelButton.addActionListener((e) => cancel(false))
  cancelPanel.add(cancelButton)
  progressPanel.add(cancelPanel, BorderLayout.SOUTH)
  dialog.pack()
  dialog.setLocationRelativeTo(parent)

  def executeAndDisplay() = {
    super.execute()
    dialog.setVisible(true)
  }

  override def doInBackground = Using.resource(ProgressInputStream(FileInputStream(file), (a, b) => publish(b.toInt)))(background(_))
  override def done = dialog.dispose()
  override def process(chunks: java.util.List[Integer]) = progressBar.setValue(chunks.asScala.last)
}