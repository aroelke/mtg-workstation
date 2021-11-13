package editor.gui.editor

import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer
import editor.collection.deck.Deck
import java.io.File
import java.util.NoSuchElementException
import java.io.FileWriter
import scala.util.Using
import editor.gui.MainFrame
import java.lang.reflect.Type
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonParseException
import scala.jdk.CollectionConverters._
import java.io.IOException
import java.text.SimpleDateFormat
import java.awt.Window
import java.io.InputStream
import javax.swing.SwingWorker
import javax.swing.JDialog
import java.awt.Dialog
import javax.swing.JProgressBar
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.BorderFactory
import javax.swing.JButton
import java.io.FileInputStream
import editor.util.ProgressInputStream
import java.awt.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CancellationException
import editor.collection.`export`.CardListFormat
import java.io.ObjectInputStream
import java.io.ObjectInput

object DeckSerializer {
  def ChangelogDateFormat = SimpleDateFormat("MMMM d, yyyy HH:mm:ss")

  def SaveVersion = 3L

  @throws[DeckLoadException]("if the deck couldn't be loaded (not including user cancellation)")
  def load(file: File, parent: Component) = {
    val worker = LoadWorker(file, parent, (stream) => Using.resource(BufferedReader(InputStreamReader(stream)))(MainFrame.Serializer.fromJson(_, classOf[DeckSerializer])))
    worker.executeAndDisplay()
    try {
      val d = worker.get
      d._file = Some(file)
      d
    } catch {
      case _: CancellationException => DeckSerializer()
      case e => throw DeckLoadException(file, e)
    }
  }

  @throws[DeckLoadException]("if the list couldn't be imported (not including user cancellation)")
  def importList(format: CardListFormat, file: File, parent: Component) = {
    val worker = LoadWorker(file, parent, format.parse(_))
    worker.executeAndDisplay()
    try {
      val d = worker.get
      d.imported = true
      d
    } catch {
      case _: CancellationException => DeckSerializer()
      case e => throw DeckLoadException(file, e)
    }
  }

  @throws[DeckLoadException]("if the legacy file couldn't be imported (not including user cancellation)")
  def importLegacy(file: File, parent: Component) = {
    val version = try {
      Using.resource(ObjectInputStream(FileInputStream(file)))(_.readLong)
    } catch case e: IOException => throw DeckLoadException(file, e)
    val worker = LoadWorker(file, parent, (stream) => {
      val v = if (version > DeckSerializer.SaveVersion) 0 else version
      Using.resource(ObjectInputStream(stream)){ ois =>
        if (v > 0)
          ois.readLong
        val deck = editor.serialization.legacy.DeckDeserializer.readExternal(ois)
        val sideboard = if (v <= 2) Map("Sideboard" -> editor.serialization.legacy.DeckDeserializer.readExternal(ois)) else {
          val boards = ois.readInt
          (0 until boards).map(_ => {
            val name = ois.readUTF
            name -> editor.serialization.legacy.DeckDeserializer.readExternal(ois)
          }).toMap
        }
        val changelog = if (v < 2) ois.readObject.asInstanceOf[String] else ois.readUTF
        DeckSerializer(deck, sideboard, changelog)
      }
    })
    worker.executeAndDisplay()
    try {
      val d = worker.get
      d.imported = true
      d
    } catch {
      case e: CancellationException => DeckSerializer()
      case e: Exception => throw DeckLoadException(file, e)
    }
  }

  @deprecated def apply(d: Deck = Deck(), s: Map[String, Deck] = Map.empty, n: String = "", c: String = "") = new DeckSerializer(d, s, n, c)
}

class DeckSerializer(private var d: Deck = Deck(), private var s: Map[String, Deck] = Map.empty, private var n: String = "", private var c: String = "")
  extends JsonSerializer[DeckSerializer] with JsonDeserializer[DeckSerializer]
{
  @deprecated def this(d: Deck, s: java.util.Map[String, Deck], n: String, c: String) = this(d, s.asScala.toMap, n, c)

  private var imported = false

  private var _file: Option[File] = None
  def file = _file.getOrElse(throw NoSuchElementException("no file saved or loaded"))

  def deck = d

  def sideboards = s

  def notes = n

  def changelog = c

  def canSaveFile = _file.isDefined && !imported

  @throws[IOException]("if the file could not be saved")
  def save(f: File) = Using.resource(FileWriter(f)){ writer =>
    writer.write(MainFrame.Serializer.toJson(this))
    _file = Some(f)
  }

  @deprecated def importLegacy(file: File, parent: Component) = {
    if (!deck.isEmpty)
      throw DeckLoadException(file, "deck already loaded")
    else {
      val version = try {
        Using.resource(ObjectInputStream(FileInputStream(file)))(_.readLong)
      } catch case e: IOException => throw DeckLoadException(file, e)
      val worker = LoadWorker(file, parent, (stream) => {
        val v = if (version > DeckSerializer.SaveVersion) 0 else version
        Using.resource(ObjectInputStream(stream)){ ois =>
          if (v > 0)
            ois.readLong
          d = editor.serialization.legacy.DeckDeserializer.readExternal(ois)
          if (v <= 2)
            s += ("Sideboard" -> editor.serialization.legacy.DeckDeserializer.readExternal(ois))
          else {
            val boards = ois.readInt
            for (i <- 0 until boards) {
              val name = ois.readUTF
              s += (name -> editor.serialization.legacy.DeckDeserializer.readExternal(ois))
            }
          }
          c = if (v < 2) ois.readObject.asInstanceOf[String] else ois.readUTF
          this
        }
      })
      worker.executeAndDisplay()
      try {
        worker.get
      } catch {
        case e: CancellationException =>
        case e: Exception => throw DeckLoadException(file, e)
      }
      imported = true
    }
  }

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