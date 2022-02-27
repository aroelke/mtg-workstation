package editor.gui.inventory

import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import editor.collection.Inventory
import editor.database.FormatConstraints
import editor.database.attributes.CombatStat
import editor.database.attributes.Expansion
import editor.database.attributes.Legality
import editor.database.attributes.Loyalty
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.attributes.Rarity
import editor.database.card.Card
import editor.database.card.CardLayout
import editor.database.card.FlipCard
import editor.database.card.MeldCard
import editor.database.card.ModalCard
import editor.database.card.SingleCard
import editor.database.card.SplitCard
import editor.database.card.TransformCard
import editor.database.version.DatabaseVersion
import editor.filter.leaf.options.multi.CardTypeFilter
import editor.filter.leaf.options.multi.SubtypeFilter
import editor.filter.leaf.options.multi.SupertypeFilter
import editor.gui.MainFrame
import editor.gui.generic.ScrollablePanel
import editor.gui.settings.SettingsDialog
import edu.emory.mathcs.backport.java.util.concurrent.CancellationException
import edu.emory.mathcs.backport.java.util.concurrent.ExecutionException

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
import java.util.Date
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
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters._
import scala.util.Using
import scala.util.control.Breaks._
import com.google.gson.JsonElement

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
        case n: Int => n
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
  inventory: Inventory = Inventory(),
  expansions: Seq[Expansion] = Seq.empty,
  supertypes: Seq[String] = Seq.empty,
  types: Seq[String] = Seq.empty,
  subtypes: Seq[String] = Seq.empty,
  warnings: Seq[String] = Seq.empty
)

private class InventoryLoader(file: File, consumer: (String) => Unit, finished: () => Unit) extends SwingWorker[LoadedData, String] {
  private val v500 = DatabaseVersion(5, 0, 0)

  private val errors = collection.mutable.ArrayBuffer[String]()

  private def createMultiFacedCard(layout: CardLayout, faces: Seq[Card]) = {
    import CardLayout._

    def converToNormal(card: Card) = SingleCard(
      CardLayout.NORMAL,
      card.name(0),
      card.manaCost(0),
      card.colors,
      card.colorIdentity,
      card.supertypes,
      card.types,
      card.subtypes,
      card.printedTypes.get(0),
      card.rarity,
      card.expansion,
      card.oracleText.get(0),
      card.flavorText.get(0),
      card.printedText.get(0),
      card.artist.get(0),
      card.multiverseid.get(0),
      card.scryfallid.get(0),
      card.number.get(0),
      card.power.get(0),
      card.toughness.get(0),
      card.loyalty.get(0),
      java.util.TreeMap(card.rulings),
      card.legality,
      card.commandFormats
    )

    var error = false
    val face = faces(0)
    val result = layout match {
      case SPLIT | AFTERMATH | ADVENTURE =>
        if (faces.size < 2) {
          errors += s"$face (${face.expansion}): Can't find other face(s) for split card."
          error = true
        } else {
          faces.foreach{ f => if (f.layout != face.layout) {
            errors += s"$face (${face.expansion}): Can't join non-split faces into a split card."
            error = true
          }}
        }
        if (!error) Set(SplitCard(faces)) else Set.empty
      case FLIP =>
        if (faces.size < 2) {
          errors += s"$face: (${face.expansion}): Can't find other side of flip card."
          error = true
        } else if (faces.size > 2) {
          errors += s"$face: (${face.expansion}): Too many sides for flip card."
          error = true
        } else if (faces(0).layout != FLIP || faces(1).layout != FLIP) {
          errors += s"$face (${face.expansion}): Can't join non-flip cards into a flip card."
          error = true
        }
        if (!error) Set(FlipCard(faces(0), faces(1))) else Set.empty
      case TRANSFORM =>
        if (faces.size < 2) {
          errors += s"$face (${face.expansion}): Can't find other face of double-faced card."
          error = true
        } else if (faces.size > 2) {
          errors += s"$face (${face.expansion}): Too many faces for double-faced card."
          error = true
        } else if (faces(0).layout != TRANSFORM || faces(1).layout != TRANSFORM) {
          errors += s"$face (${face.expansion}): Can't join single-faced cards into double-faced cards"
          error = true
        }
        if (!error) Set(TransformCard(faces(0), faces(1))) else Set.empty
      case MODAL_DFC =>
        if (faces.size < 2) {
          errors += s"$face (${face.expansion}): Can't find other face of modal double-faced card."
          error = true
        } else if (faces.size > 2) {
          errors += s"$face (${face.expansion}): Too many faces for modal double-faced card."
          error = true
        } else if (faces(0).layout != MODAL_DFC || faces(1).layout != MODAL_DFC) {
          errors += s"$face (${face.expansion}): Can't join single-faced cards into modal double-faced cards."
          error = true
        }
        if (!error) Set(ModalCard(faces(0), faces(1))) else Set.empty
      case MELD =>
        if (faces.size < 3) {
          errors += s"$face (${face.expansion}): Can't find some faces of meld card."
          error = true
        } else if (faces.size > 3) {
          errors += s"$face (${face.expansion}): Too many faces for meld card."
          error = true
        } else if (faces(0).layout != MELD || faces(1).layout != MELD || faces(2).layout != MELD) {
          errors += s"$face (${face.expansion}): Can't join single-faced cards into meld cards."
          error = true
        }
        if (!error) Set(MeldCard(faces(0), faces(1), faces(2)), MeldCard(faces(1), faces(0), faces(2))) else Set.empty
      case _ => Set.empty
    }
    if (error) faces.map(converToNormal).toSet else result
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

      val root = JsonParser().parse(reader).getAsJsonObject
      val version = if (root.has("meta")) DatabaseVersion.parseVersion(root.get("meta").getAsJsonObject.get("version").getAsString) else DatabaseVersion(0, 0, 0)

      val entries = (if (version < v500) root else root.get("data").getAsJsonObject).entrySet.asScala
      val numCards = entries.foldLeft(0)(_ + _.getValue.getAsJsonObject.get("cards").getAsJsonArray.size)

      // We don't use String.intern() here because the String pool that is maintained must include extra data that adds several MB
      // to the overall memory consumption of the inventory
      val costs = collection.mutable.Map[String, ManaCost]()
      val colorLists = collection.mutable.Map[String, Seq[ManaType]]()
      val allSupertypes = collection.mutable.Map[String, String]()
      val supertypeSets = collection.mutable.Map[String, collection.mutable.LinkedHashSet[String]]()
      val allTypes = collection.mutable.Map[String, String]()
      val typeSets = collection.mutable.Map[String, collection.mutable.LinkedHashSet[String]]()
      val allSubtypes = collection.mutable.Map[String, String]()
      val subtypeSets = collection.mutable.Map[String, collection.mutable.LinkedHashSet[String]]()
      val printedTypes = collection.mutable.Map[String, String]()
      val texts = collection.mutable.Map[String, String]()
      val flavors = collection.mutable.Map[String, String]()
      val artists = collection.mutable.Map[String, String]()
      val formats = collection.mutable.Map.from(FormatConstraints.FormatNames.map((n) => n -> n).toMap)
      val numbers = collection.mutable.Map[String, String]()
      val stats = collection.mutable.Map[String, CombatStat]()
      val loyalties = collection.mutable.Map[String, Loyalty]()
      val rulingDates = collection.mutable.Map[String, Date]()
      val rulingContents = collection.mutable.Map[String, String]()

      publish(s"Reading cards from ${file.getName}...")
      var progress = 0
      setProgress(progress)
      val cards = tryBreakable { entries.flatMap{ (e) =>
        if (isCancelled) {
          expansions.clear()
          break
        }

        val setProperties = e.getValue.getAsJsonObject
        val setCards = setProperties.get("cards").getAsJsonArray
        val set = Expansion(
          setProperties.get("name").getAsString,
          Option(setProperties.get("block")).map(_.getAsString).getOrElse(Expansion.NoBlock),
          setProperties.get("code").getAsString,
          setCards.size,
          LocalDate.parse(setProperties.get("releaseDate").getAsString, Expansion.DateFormatter)
        )
        expansions += set

        publish(s"Loading cards from $set...")
        setCards.asScala.flatMap{ cardElement =>
          // Create the new card for the expansion
          val card = cardElement.getAsJsonObject

          // Card's name
          val name = {
            if (card.has("faceName"))
              card.get("faceName").getAsString
            else if (card.has("name"))
              card.get("name").getAsString
            else
              throw CardLoadException("<unknown>", set, "card with missing name")
          }

          // Card's multiverseid and Scryfall id
          val scryfallid = {
            if (version < v500) {
              Option(card.get("scryfallId")).map(_.getAsString)
            } else {
              Option(card.get("identifiers")).map(_.getAsJsonObject).flatMap(id => Option(id.get("scryfallId")).map(_.getAsString))
            }
          }.getOrElse(throw CardLoadException(name, set, "no Scryfall ID"))
          val multiverseid = {
            if (version < v500) {
              Option(card.get("multiverseId")).map(_.getAsInt)
            } else {
              Option(card.get("identifiers")).map(_.getAsJsonObject).flatMap(id => Option(id.get("multiverseId")).map(_.getAsInt))
            }
          }.getOrElse(-1)

          // If the card is a token, skip it
          try {
            val layout = Option(card.get("layout")).map(l => CardLayout.valueOf(l.getAsString.toUpperCase.replaceAll("[^A-Z]", "_"))).getOrElse(throw CardLoadException(name, set, "no valid layout"))

            // Rulings
            val rulings = collection.mutable.TreeMap[Date, collection.mutable.ArrayBuffer[String]]()
            Option(card.get("rulings")).toSeq.flatMap(_.getAsJsonArray.asScala.map(_.getAsJsonObject)).foreach{ o =>
              (Option(o.get("text")), Option(o.get("date"))) match {
                case (Some(t), Some(d)) =>
                  val r = t.getAsString
                  val ruling = rulingContents.getOrElseUpdate(r, r)
                  try {
                    val date = rulingDates.getOrElseUpdate(d.getAsString, format.parse(d.getAsString))
                    if (!rulings.contains(date))
                      rulings += date -> collection.mutable.ArrayBuffer[String]()
                    rulings(date) += ruling
                  } catch case x: ParseException => errors += CardLoadException(name, set, x.getMessage).toString
                case _ => errors += CardLoadException(name, set, "ruling missing date or text").getMessage
              }
            }

            // Format legality
            val legality = Option(card.get("legalities"))
              .map(_.getAsJsonObject.entrySet.asScala.map((e) => formats.getOrElseUpdate(e.getKey, e.getKey) -> Legality.parseLegality(e.getValue.getAsString)).toMap)
              .getOrElse(Map.empty)

            // Formats the card can be commander in
            val commandFormats = Option(card.get("leadershipSkills")).toSeq.flatMap(_.getAsJsonObject.entrySet.asScala.collect{ case e if e.getValue.getAsBoolean =>
              formats.getOrElseUpdate(e.getKey, e.getKey)
            }.toSeq.sorted)

            def getOrError[T](key: String, value: (JsonElement) => T, error: String) = Option(card.get(key)).map(value).getOrElse(throw CardLoadException(name, set, error))
            val cost = Option(card.get("manaCost")).map(_.getAsString).getOrElse("")
            val colors = getOrError("colors", _.getAsJsonArray, "invalid colors")
            val identity = getOrError("colorIdentity", _.getAsJsonArray, "invalid color identity")
            val supers = getOrError("supertypes", _.getAsJsonArray, "invalid supertypes")
            val types = getOrError("types", _.getAsJsonArray, "invalid types")
            val subs = getOrError("subtypes", _.getAsJsonArray, "invalid subtypes")
            val oTypes = Option(card.get("originalType")).map(_.getAsString).getOrElse("")
            val text = Option(card.get("text")).map(_.getAsString).getOrElse("")
            val flavor = Option(card.get("flavorText")).map(_.getAsString).getOrElse("")
            val printed = Option(card.get("originalText")).map(_.getAsString).getOrElse("")
            val artist = Option(card.get("artist")).map(_.getAsString).getOrElse("")
            val number = Option(card.get("number")).map(_.getAsString).getOrElse("")
            val power = Option(card.get("power")).map(_.getAsString).getOrElse("")
            val toughness = Option(card.get("toughness")).map(_.getAsString).getOrElse("")
            val loyalty = Option(card.get("loyalty")).map(l => if (l.isJsonNull) "X" else l.getAsString).getOrElse("")

            val c = SingleCard(
              layout,
              name,
              costs.getOrElseUpdate(cost, ManaCost.parseManaCost(cost)),
              colorLists.getOrElseUpdate(colors.toString, {
                val col = collection.mutable.ArrayBuffer[ManaType]()
                colors.asScala.foreach((e) => col += ManaType.parseManaType(e.getAsString))
                col.toSeq
              }),
              colorLists.getOrElseUpdate(identity.toString, {
                val col = collection.mutable.ArrayBuffer[ManaType]()
                identity.asScala.foreach((e) => col += ManaType.parseManaType(e.getAsString))
                col.toSeq
              }),
              supertypeSets.getOrElseUpdate(supers.toString, {
                val s = collection.mutable.LinkedHashSet[String]()
                supers.asScala.foreach((e) => s += allSupertypes.getOrElseUpdate(e.getAsString, e.getAsString))
                s
              }).toSet,
              typeSets.getOrElseUpdate(types.toString, {
                val s = collection.mutable.LinkedHashSet[String]()
                types.asScala.foreach((e) => s += allTypes.getOrElseUpdate(e.getAsString, e.getAsString))
                s
              }).toSet,
              subtypeSets.getOrElseUpdate(subs.toString, {
                val s = collection.mutable.LinkedHashSet[String]()
                subs.asScala.foreach((e) => s += allSubtypes.getOrElseUpdate(e.getAsString, e.getAsString))
                s
              }).toSet,
              printedTypes.getOrElseUpdate(oTypes, oTypes),
              Rarity.parseRarity(card.get("rarity").getAsString),
              set,
              texts.getOrElseUpdate(text, text),
              flavors.getOrElseUpdate(flavor, flavor),
              texts.getOrElseUpdate(printed, printed),
              artists.getOrElseUpdate(artist, artist),
              multiverseid,
              scryfallid,
              numbers.getOrElseUpdate(number, number),
              stats.getOrElseUpdate(power, CombatStat(power)),
              stats.getOrElseUpdate(toughness, CombatStat(toughness)),
              loyalties.getOrElseUpdate(loyalty, Loyalty(loyalty)),
              java.util.TreeMap(rulings.map{ case (d, r) => d -> r.asJava }.asJava),
              legality.asJava,
              commandFormats.asJava
            )

            // Collect unexpected card values
            if (c.artist.isEmpty)
              errors += s"${c.unifiedName} (${c.expansion}): missing artist"

            // Add to map of faces if the card has multiple faces
            if (layout.isMultiFaced) {
              if (version < v500) {
                if (card.has("names"))
                  faces += c -> card.get("names").getAsJsonArray.asScala.map(_.getAsString).toSeq
                else
                  throw CardLoadException(name, set, "other faces of multi-faced card not defined")
              } else {
                multiUUIDs += card.get("uuid").getAsString -> c
                facesNames += c -> card.get("name").getAsString.split(Card.FACE_SEPARATOR).toSeq
                if (card.has("otherFaceIds"))
                  otherFaceIds += c -> card.get("otherFaceIds").getAsJsonArray.asScala.map(_.getAsString).toSeq
                else
                  throw CardLoadException(name, set, "other faces of multi-faced card not defined")
              }
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
        }
      }}.catchBreak(collection.mutable.Set.empty[Card])

      if (!isCancelled) {
        publish("Processing multi-faced cards...")
        if (version <= v500) {
          val facesList = collection.mutable.ArrayBuffer(faces.keySet.toSeq:_*)
          while (!facesList.isEmpty) {
            val face = facesList.remove(0)
            val otherFaces = collection.mutable.ArrayBuffer[Card]()
            if (version < v500 || face.layout != CardLayout.MELD) {
              val faceNames = faces(face)
              facesList.foreach((c) => if (faceNames.contains(c.unifiedName) && c.expansion == face.expansion) {
                otherFaces += c
              })
              facesList --= otherFaces
              otherFaces += face
              otherFaces.sortInPlaceBy((a) => faceNames.indexOf(a.unifiedName))
            }
            cards --= otherFaces

            if (face.layout == CardLayout.MELD) {
              val first = otherFaces(1)
              otherFaces(1) = otherFaces(2)
              otherFaces(2) = first
            }
            cards ++= createMultiFacedCard(face.layout, otherFaces.toSeq)
          }
        } else {
          cards --= facesNames.keys
          facesNames.foreach{ case (face, names) =>
            if (otherFaceIds.contains(face)) {
              val cardFaces = (otherFaceIds(face).map(multiUUIDs(_)).toSeq :+ face).sortBy((c) => names.indexOf(c.unifiedName))
              if (face.layout != CardLayout.MELD || cardFaces.size == 3)
                cards ++= createMultiFacedCard(face.layout, cardFaces)
            } else
              errors += CardLoadException(face.unifiedName, face.expansion, "other faces not found").getMessage
          }
        }

        val missingFormats = formats.collect{ case (_, f) if !FormatConstraints.FormatNames.contains(f) => f }.toSeq.sorted
        if (!missingFormats.isEmpty)
          errors += s"""Could not find definitions for the following formats: ${missingFormats.mkString(", ")}"""
      }

      LoadedData(Inventory(cards.toSet.asJava), expansions.toSeq.sorted, allSupertypes.values.toSeq.sorted, allTypes.values.toSeq.sorted, allSubtypes.values.toSeq.sorted, errors.toSeq)
    }

    if (!isCancelled && Files.exists(Path.of(SettingsDialog.settings.inventory.tags))) {
      publish("Processing tags...")
      val tk = new TypeToken[java.util.Map[String, java.util.Set[String]]] {}
      val raw = MainFrame.Serializer.fromJson(Files.readAllLines(Path.of(SettingsDialog.settings.inventory.tags)).asScala.mkString("\n"), tk.getType).asInstanceOf[java.util.Map[String, java.util.Set[String]]].asScala.map{ case (n, t) => n -> t.asScala.toSet }.toMap
      Card.tags.clear()
      Card.tagMap.putAll(raw.map{ case (name, tags) => data.inventory.find(name) -> tags.asJava }.asJava)
    }

    data
  }

  protected override def process(chunks: java.util.List[String]) = chunks.asScala.foreach(consumer(_))
  protected override def done() = finished()
}

case class CardLoadException(name: String, expansion: Expansion, message: String, cause: Option[Throwable] = None) extends RuntimeException(s"$name ($expansion): $message", cause.getOrElse(null))