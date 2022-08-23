package editor.gui.deck

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.`export`.CardListFormat
import editor.collection.mutable.Deck
import editor.gui.MainFrame
import editor.serialization
import editor.util.ProgressInputStream

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
import org.json4s.CustomSerializer
import org.json4s.Extraction
import org.json4s.JObject
import org.json4s.JField
import org.json4s.JArray
import org.json4s.JString

/**
 * Companion object containing global information about serializing decks and for creating new [[DeckSerializer]]s.
 * @author Alec Roelke
 */
object DeckSerializer {
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
    val worker = LoadWorker(file, parent, (stream) => Using.resource(BufferedReader(InputStreamReader(stream)))(serialization.Serializer.fromJson(_, classOf[DeckSerializer])))
    worker.executeAndDisplay()
    try {
      worker.get.copy(file = Some(file))
    } catch {
      case _: CancellationException => DeckSerializer()
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
      case _: CancellationException => DeckSerializer()
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
case class DeckSerializer(deck: Deck = Deck(), sideboards: Map[String, Deck] = Map.empty, notes: String = "", changelog: String = "", file: Option[File] = None)
  extends CustomSerializer[DeckSerializer](implicit formats => (
    { case JObject(JField("main", main) :: JField("sideboards", JArray(sideboards)) :: JField("notes", JString(notes)) :: JField("changelog", JString(changelog)) :: Nil) =>
      DeckSerializer(Extraction.extract[Deck](main), sideboards.collect{ case entry: JObject => entry.obj.collect{ case ("name", JString(name)) => name }.head -> Extraction.extract[Deck](entry) }.toMap, notes, changelog) },
    { case DeckSerializer(deck, sideboards, notes, changelog, _) => JObject(List(
      JField("main", Extraction.decompose(deck)),
      JField("sideboards", JArray(sideboards.map{ case (name, sb) => JObject(Extraction.decompose(sb) match {
        case JObject(obj) => JField("name", JString(name)) +: obj
        case _ => throw MatchError("a deck should be a JObject")
      }) }.toList)),
      JField("notes", JString(notes)),
      JField("changelog", JString(changelog))
    )) }
  )) with JsonSerializer[DeckSerializer] with JsonDeserializer[DeckSerializer]
{
  /** Save the serialized deck to a JSON file, if the file is defined. */
  @throws[IOException]("if the file could not be saved")
  @throws[NoSuchFileException]("if there is no file to save to")
  def save() = file.map((f) => Using.resource(FileWriter(f)){ writer =>
    writer.write(serialization.Serializer.toJson(this))
  }).getOrElse(throw NoSuchFileException("no file to save to"))

  override def serialize(src: DeckSerializer, typeOfSrc: Type, context: JsonSerializationContext) = {
    val json = JsonObject()
    json.add("main", context.serialize(src.deck))
    val side = JsonArray()
    src.sideboards.keys.foreach((n) => {
      val board = context.serialize(src.sideboards(n)).getAsJsonObject
      board.addProperty("name", n)
      side.add(board)
    })
    json.add("sideboards", side)
    json.addProperty("notes", src.notes)
    json.addProperty("changelog", src.changelog)
    json
  }

  @throws[JsonParseException]("if the JSON could not be parsed")
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val obj = json.getAsJsonObject
    val deck = context.deserialize[Deck](obj.get("main"), classOf[Deck])
    val notes = if (obj.has("notes")) obj.get("notes").getAsString else ""
    val sideboard = collection.mutable.Map[String, Deck]()
    obj.get("sideboards").getAsJsonArray.asScala.foreach((entry) => {
      sideboard(entry.getAsJsonObject.get("name").getAsString) = context.deserialize[Deck](entry, classOf[Deck])
    })
    val changelog = obj.get("changelog").getAsString
    DeckSerializer(deck, sideboard.toMap, notes, changelog)
  }
}
/**
 * Helper class for loading a deck in the background. Shows a dialog indicating load progress.
 * 
 * @constructor create a new background load worker
 * @param file file to load from
 * @param parent component doing the loading, for positioning the dialog
 * @param background function converting the data from the file input stream into a [[DeckSerializer]]
 */
private class LoadWorker(file: File, parent: Component, background: (InputStream) => DeckSerializer) extends SwingWorker[DeckSerializer, Integer] {
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