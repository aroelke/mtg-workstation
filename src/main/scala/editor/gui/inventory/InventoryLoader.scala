package editor.gui.inventory

import com.mdimension.jchronic.Chronic
import editor.collection.CardListEntry
import editor.collection.immutable.Inventory
import editor.database.FormatConstraints
import editor.database.attributes._
import editor.database.card._
import editor.database.version.DatabaseVersion
import editor.gui.MainFrame
import editor.gui.generic.ScrollablePanel
import editor.gui.settings.SettingsDialog
import editor.serialization
import org.json4s._
import org.json4s.native.JsonMethods

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.WindowConstants
import scala.collection.immutable.ListSet
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.util.Using
import scala.util.control.Breaks._

/**
 * Worker that loads inventory data from JSON along with metadata like expansions, blocks, and existing supertypes, card types, and subtypes.
 * @author Alec Roelke
 */
object InventoryLoader {
  /**
   * Load the card inventory and metadata. While loading, a dialog will appear with a progress bar indicating overall progress
   * of loading cards and a text field showing the expansions that are being processed.
   * 
   * @param owner frame performing the load, for positioning of dialogs
   * @param file file to load from
   * @return a [[LoadedData]] containing the inventory and metadata
   */
  def loadInventory(owner: Frame, file: File) = {
    val Border = 10

    val dialog = JDialog(owner, "Loading Inventory", Dialog.ModalityType.APPLICATION_MODAL)
    dialog.setResizable(false)
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

    // Content panel
    val contentPanel = Box(BoxLayout.Y_AXIS)
    contentPanel.setBorder(BorderFactory.createEmptyBorder(Border, Border, Border, Border))
    dialog.setContentPane(contentPanel)

    // Stage progress label
    val progressLabel = JLabel("Loading inventory...")
    progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT)
    contentPanel.add(progressLabel)
    contentPanel.add(Box.createVerticalStrut(2))

    // Overall progress bar
    val progressBar = JProgressBar()
    progressBar.setIndeterminate(true)
    progressBar.setAlignmentX(Component.LEFT_ALIGNMENT)
    contentPanel.add(progressBar)
    contentPanel.add(Box.createVerticalStrut(2))

    // History text area
    val progressArea = JTextArea("", 6, 40)
    progressArea.setEditable(false)
    val progressPane = JScrollPane(progressArea)
    progressPane.setAlignmentX(Component.LEFT_ALIGNMENT)
    contentPanel.add(progressPane)
    contentPanel.add(Box.createVerticalStrut(2))

    val loader = InventoryLoader(file, (c) => {
      progressLabel.setText(c)
      progressArea.append(s"$c\n")
    }, () => {
      dialog.setVisible(false)
      dialog.dispose()
    })
    loader.addPropertyChangeListener((e) => if (e.getPropertyName == "progress") {
      val p = e.getNewValue match {
        case n: Integer => n.toInt
        case _ => 0
      }
      progressBar.setIndeterminate(p < 0)
      progressBar.setValue(p)
    })

    // Cancel button
    val cancelPanel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0))
    val cancelButton = JButton("Cancel")
    cancelButton.addActionListener(_ => loader.cancel(false))
    cancelPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    cancelPanel.add(cancelButton)
    contentPanel.add(cancelPanel)

    dialog.addWindowListener(new WindowAdapter { override def windowClosing(e: WindowEvent) = loader.cancel(false) })
    dialog.getRootPane.registerKeyboardAction(_ => {
      loader.cancel(false)
      dialog.setVisible(false)
      dialog.dispose()
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)

    dialog.pack()
    loader.execute()
    dialog.setLocationRelativeTo(owner)
    dialog.setVisible(true)
    val result = try {
      loader.get()
    } catch {
      case e @ (_: InterruptedException | _: ExecutionException) =>
        JOptionPane.showMessageDialog(owner, s"Error loading inventory: ${e.getCause.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
        e.printStackTrace()
        LoadedData()
      case _: CancellationException => LoadedData()
    }
    if (SettingsDialog.settings.inventory.warn && !result.warnings.isEmpty) {
      SwingUtilities.invokeLater(() => {
        val suppress = SettingsDialog.showWarnings(owner, "Errors ocurred while loading the following card(s):", result.warnings, true)
        SettingsDialog.settings = SettingsDialog.settings.copy(inventory = SettingsDialog.settings.inventory.copy(warn = !suppress))
      })
    }

    result
  }
}

/**
 * Data loaded from the inventory JSON file.
 * 
 * @constructor create a new loaded inventory data structure
 * @param inventory the loaded cards
 * @param expansions list of all expansions the cards belong to
 * @param supertypes list of all supertypes among all cards
 * @param types list of all card types among all cards
 * @param subtypes list of all subtypes among all cards
 * @param warnings list of warnings that occurred while loading
 */
case class LoadedData(
  inventory: IndexedSeq[Card] = IndexedSeq.empty,
  expansions: Seq[Expansion] = Seq.empty,
  supertypes: Seq[String] = Seq.empty,
  types: Seq[String] = Seq.empty,
  subtypes: Seq[String] = Seq.empty,
  warnings: Seq[String] = Seq.empty
)

private case class LeadershipSkills(commands: Map[String, Boolean])
private case class Ruling(date: String, text: String)
private case class Identifiers(scryfallId: String, multiverseId: String = "-1")
private case class RawCard(
  uuid: String,
  identifiers: Identifiers,
  name: String,
  layout: String,
  faceName: Option[String] = None,
  rulings: Option[Seq[Ruling]] = None,
  legalities: Map[String, String] = Map.empty,
  leadershipSkills: Option[LeadershipSkills] = None,
  manaCost: String = "",
  colors: Seq[String] = Seq.empty,
  colorIdentity: Seq[String] = Seq.empty,
  supertypes: Seq[String] = Seq.empty,
  types: Seq[String] = Seq.empty,
  subtypes: Seq[String] = Seq.empty,
  originalType: String = "",
  text: String = "",
  flavorText: String = "",
  originalText: String = "",
  artist: String = "",
  number: String = "",
  power: Option[String] = None,
  toughness: Option[String] = None,
  loyalty: Option[String] = None,
  defense: Option[String] = None,
  rarity: String = "",
  otherFaceIds: Option[Seq[String]] = None
)
case class RawExpansion(name: String, block: String = Expansion.NoBlock, code: String, releaseDate: String, cards: Seq[RawCard]) {
  def toExpansion = Expansion(name, block, code, cards.size, try {
    Option(Chronic.parse(releaseDate))
        .map(_.getBeginCalendar.getTime.toInstant.atZone(ZoneId.systemDefault).toLocalDate)
        .getOrElse(LocalDate.now)
  } catch case _: IllegalStateException => LocalDate.now)
}

private class InventoryLoader(file: File, consumer: (String) => Unit, finished: () => Unit) extends SwingWorker[LoadedData, String] {
  private val errors = collection.mutable.ArrayBuffer[String]()

  private def createMultiFacedCard(layout: CardLayout, faces: Seq[Card]) = {
    import CardLayout._

    var error = false
    val face = faces(0)
    val joined = {
      if (faces.size < layout.faces) {
        errors += s"$face (${face.expansion}): Can't find other face(s) for ${layout.toString.toLowerCase} card"
        error = true
      } else if (faces.size > layout.faces && layout.faces > 0) {
        errors += s"$face (${face.expansion}): Too many faces for ${layout.toString.toLowerCase} card"
        error = true
      } else if (faces.forall(_.layout != layout)) {
        errors += s"${face} (${face.expansion}): can't join non-${layout.toString.toLowerCase} faces into a ${layout.toString.toLowerCase} card"
        error = true
      }
      if (!error) layout.combine(faces) else Set.empty
    }
    if (error) faces.map{ case f: SingleCard => f.copy(layout = NORMAL) }.toSet else joined
  }

  protected override def doInBackground() = {
    val format = SimpleDateFormat("yyyy-MM-dd")

    publish(s"Opening ${file.getName}...")

    val faces = collection.mutable.Map[Card, Seq[String]]()
    val expansions = collection.mutable.Set[Expansion]()
    val multiUUIDs = collection.mutable.Map[String, Card]()
    val facesNames = collection.mutable.Map[Card, Seq[String]]()
    val otherFaceIds = collection.mutable.Map[Card, Seq[String]]()

    // Read the inventory file
    val data = Using.resource(BufferedReader(InputStreamReader(FileInputStream(file), "UTF8"))){ reader =>
      publish(s"Parsing ${file.getName}...")

      given formats: Formats = serialization.formats + CustomSerializer[LeadershipSkills]((format) => (
        { case JObject(fields) => LeadershipSkills(fields.collect{ case JField(name, JBool(bool)) => name -> bool }.toMap) },
        { case _ => JNothing } // this is unused
      ))
      val entries = (JsonMethods.parse(reader) \ "data").extract[Map[String, RawExpansion]].map{ case (_, set) => set }
      val numCards = entries.map(_.cards.size).sum

      // We don't use String.intern() here because the String pool that is maintained must include extra data that adds several MB
      // to the overall memory consumption of the inventory
      val costs = collection.mutable.Map[String, ManaCost]()
      val colorSets = collection.mutable.Map[String, Set[ManaType]]()
      val allSupertypes = collection.mutable.Map[String, String]()
      val supertypeSets = collection.mutable.Map[String, ListSet[String]]()
      val allTypes = collection.mutable.Map[String, String]()
      val typeSets = collection.mutable.Map[String, ListSet[String]]()
      val allSubtypes = collection.mutable.Map[String, String]()
      val subtypeSets = collection.mutable.Map[String, ListSet[String]]()
      val printedTypes = collection.mutable.Map[String, String]()
      val texts = collection.mutable.Map[String, String]()
      val flavors = collection.mutable.Map[String, String]()
      val artists = collection.mutable.Map[String, String]()
      val formatNames = collection.mutable.Map.from(FormatConstraints.FormatNames.map((n) => n -> n).toMap)
      val numbers = collection.mutable.Map[String, String]()
      val stats = collection.mutable.Map[String, CombatStat]()
      val counters = collection.mutable.Map[String, CounterStat]()
      val rulingDates = collection.mutable.Map[String, Date]()
      val rulingContents = collection.mutable.Map[String, String]()

      publish(s"Reading cards from ${file.getName}...")
      var progress = 0
      setProgress(progress)
      val cards = tryBreakable { collection.mutable.Set.from(entries.flatMap((rawSet) =>
        if (isCancelled) {
          expansions.clear()
          break
        }

        val set = rawSet.toExpansion
        expansions += set

        publish(s"Loading cards from $set...")
        rawSet.cards.flatMap((raw) => {
          // Card's name
          val name = raw.faceName.getOrElse(raw.name)

          // If the card is a token, skip it
          try {
            val layout = try {
              CardLayout.valueOf(raw.layout.toUpperCase.replaceAll("[^A-Z]", "_"))
            } catch case _ => CardLayout.values.find(_.toString.equalsIgnoreCase(raw.layout)).getOrElse(throw CardLoadException(name, set, "no valid layout"))

            // Rulings
            val rulings = raw.rulings.toSeq.flatMap(_.flatMap((r) => {
              val ruling = rulingContents.getOrElseUpdate(r.text, r.text)
              try {
                val date = rulingDates.getOrElseUpdate(r.date, format.parse(r.date))
                Some(date -> ruling)
              } catch case x: ParseException => { errors += CardLoadException(name, set, x.getMessage).toString; None }
            })).groupBy{ case (date, _) => date }.mapValues(_.map{ case (_, ruling) => ruling }).toMap

            val c = SingleCard(
              layout,
              name,
              costs.getOrElseUpdate(raw.manaCost, ManaCost.parse(raw.manaCost).get),
              colorSets.getOrElseUpdate(raw.colors.toString, raw.colors.map(ManaType.parse(_).get).toSet),
              colorSets.getOrElseUpdate(raw.colorIdentity.toString, raw.colorIdentity.map(ManaType.parse(_).get).toSet),
              supertypeSets.getOrElseUpdate(raw.supertypes.toString, ListSet.from(raw.supertypes.map((s) => allSupertypes.getOrElseUpdate(s, s)))),
              typeSets.getOrElseUpdate(raw.types.toString, ListSet.from(raw.types.map((t) => allTypes.getOrElseUpdate(t, t)))),
              subtypeSets.getOrElseUpdate(raw.subtypes.toString, ListSet.from(raw.subtypes.map((s) => allSubtypes.getOrElseUpdate(s, s)))),
              printedTypes.getOrElseUpdate(raw.originalType, raw.originalType),
              Rarity.parse(raw.rarity).getOrElse(Rarity.Unknown),
              set,
              texts.getOrElseUpdate(raw.text, raw.text),
              flavors.getOrElseUpdate(raw.flavorText, raw.flavorText),
              texts.getOrElseUpdate(raw.originalText, raw.originalText),
              artists.getOrElseUpdate(raw.artist, raw.artist),
              raw.identifiers.multiverseId.toInt,
              raw.identifiers.scryfallId,
              numbers.getOrElseUpdate(raw.number, raw.number),
              raw.power.map((p) => stats.getOrElseUpdate(p, CombatStat(p))),
              raw.toughness.map((t) => stats.getOrElseUpdate(t, CombatStat(t))),
              raw.loyalty.map((l) => counters.getOrElseUpdate(l, CounterStat(l))),
              raw.defense.map((d) => counters.getOrElseUpdate(d, CounterStat(d))),
              TreeMap.from(rulings.map{ case (d, r) => d -> r.toSeq }),
              raw.legalities.map{ case (format, legality) => formatNames.getOrElseUpdate(format, format) -> Legality.parse(legality).get }.toMap,
              raw.leadershipSkills.toSeq.flatMap(_.commands.collect{ case (format, legal) if legal => formatNames.getOrElseUpdate(format, format) }).sorted
            )

            // Collect unexpected card values
            if (c.artist.isEmpty)
              errors += s"${c.name} (${c.expansion}): missing artist"

            // Add to map of faces if the card has multiple faces
            if (layout.isMultiFaced) {
              multiUUIDs += raw.uuid -> c
              facesNames += c -> raw.name.split(Card.FaceSeparator).toSeq
              raw.otherFaceIds.map((i) => otherFaceIds += (c -> i)).getOrElse(throw CardLoadException(name, set, "other faces of multi-faced card not defined"))
            }

            progress += 1
            setProgress(progress*100/numCards)
            Some(c)
          } catch {
            case e: IllegalArgumentException =>
              errors += CardLoadException(name, set, e.getMessage, Some(e)).getMessage
              None
            case e: CardLoadException =>
              errors += e.getMessage
              None
          }
        })
      ))}.catchBreak(collection.mutable.Set.empty[Card])

      if (!isCancelled) {
        publish("Processing multi-faced cards...")
        cards --= facesNames.keys
        facesNames.foreach{ case (face, names) =>
          if (otherFaceIds.contains(face)) {
            val cardFaces = (otherFaceIds(face).map(multiUUIDs(_)).toSeq :+ face).sortBy((c) => names.indexOf(c.name))
            if (face.layout != CardLayout.MELD || cardFaces.size == 3)
              cards ++= createMultiFacedCard(face.layout, cardFaces)
          } else
            errors += CardLoadException(face.name, face.expansion, "other faces not found").getMessage
        }

        val missingFormats = formatNames.collect{ case (_, f) if !FormatConstraints.FormatNames.contains(f) => f }.toSeq.sorted
        if (!missingFormats.isEmpty)
          errors += s"""Could not find definitions for the following formats: ${missingFormats.mkString(", ")}"""
      }

      LoadedData(
        cards.toSet.toIndexedSeq.sortWith((a, b) => CardAttribute.Name.comparingEntry.compare(CardListEntry(a), CardListEntry(b)) < 0),
        expansions.toSeq.sorted,
        allSupertypes.values.toSeq.sorted,
        allTypes.values.toSeq.sorted,
        allSubtypes.values.toSeq.sorted,
        errors.toSeq
      )
    }

    if (!isCancelled && Files.exists(Path.of(SettingsDialog.settings.inventory.tags))) {
      publish("Processing tags...")

      CardAttribute.Tags.tags = JsonMethods.parse(Files.readAllLines(Path.of(SettingsDialog.settings.inventory.tags)).asScala.mkString("\n")) match {
        case JObject(entries) => entries.flatMap{
          case (id, JArray(tags)) => data.inventory.find(_.faces.exists(_.scryfallid == id)).map((c) => c -> collection.mutable.Set.from(tags.map{
            case JString(tag) => tag
            case x => throw MatchError(x)
          }))
          case x => throw MatchError(x)
        }
        case x => throw MatchError(x)
      }
    }

    data
  }

  protected override def process(chunks: java.util.List[String]) = chunks.asScala.foreach(consumer(_))
  protected override def done() = finished()
}

case class CardLoadException(name: String, expansion: Expansion, message: String, cause: Option[Throwable] = None) extends RuntimeException(s"$name ($expansion): $message", cause.getOrElse(null))