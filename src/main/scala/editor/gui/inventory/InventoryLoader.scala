package editor.gui.inventory

import javax.swing.SwingWorker
import editor.collection.Inventory
import editor.database.version.DatabaseVersion
import java.io.File
import editor.database.card.Card
import editor.database.card.SingleCard
import editor.database.card.CardLayout
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters._
import editor.database.card.SplitCard
import editor.database.card.FlipCard
import editor.database.card.TransformCard
import editor.database.card.ModalCard
import editor.database.card.MeldCard
import java.text.SimpleDateFormat
import editor.database.attributes.Expansion
import scala.util.Using
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileInputStream
import com.google.gson.JsonParser
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.FormatConstraints
import editor.database.attributes.CombatStat
import editor.database.attributes.Loyalty
import java.util.Date
import java.time.LocalDate
import java.text.ParseException
import editor.database.attributes.Legality
import editor.database.attributes.Rarity
import editor.filter.leaf.options.multi.SupertypeFilter
import editor.filter.leaf.options.multi.CardTypeFilter
import editor.filter.leaf.options.multi.SubtypeFilter
import java.nio.file.Files
import java.nio.file.Path
import editor.gui.settings.SettingsDialog
import editor.gui.MainFrame
import com.google.gson.reflect.TypeToken
import java.awt.Frame
import javax.swing.JDialog
import java.awt.Dialog
import javax.swing.WindowConstants
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import java.awt.Component
import javax.swing.JProgressBar
import javax.swing.JTextArea
import javax.swing.JScrollPane
import javax.swing.JPanel
import java.awt.FlowLayout
import javax.swing.JButton
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import edu.emory.mathcs.backport.java.util.concurrent.ExecutionException
import javax.swing.JOptionPane
import edu.emory.mathcs.backport.java.util.concurrent.CancellationException
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import javax.swing.JCheckBox

object InventoryLoader {
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
        Inventory()
      case _: CancellationException => Inventory()
    }
    if (SettingsDialog.settings.inventory.warn && !loader.warnings.isEmpty) {
      SwingUtilities.invokeLater(() => {
        val warnings = ("Errors ocurred while loading the following card(s):<ul style=\"margin-top:0;margin-left:20pt\">" +: loader.warnings).mkString("<html>", "<li>", "</ul></html>")
        val warningPanel = JPanel(BorderLayout())
        val warningLabel = JLabel(warnings)
        warningPanel.add(warningLabel, BorderLayout.CENTER)
        val suppressBox = JCheckBox("Don't show this warning in the future", !SettingsDialog.settings.inventory.warn)
        warningPanel.add(suppressBox, BorderLayout.SOUTH)
        JOptionPane.showMessageDialog(null, warningPanel, "Warning", JOptionPane.WARNING_MESSAGE)
        SettingsDialog.settings = SettingsDialog.settings.copy(inventory = SettingsDialog.settings.inventory.copy(warn = !suppressBox.isSelected))
      })
    }
    SettingsDialog.setInventoryWarnings(loader.warnings)
    result
  }
}

class InventoryLoader private(file: File, consumer: (String) => Unit, finished: () => Unit) extends SwingWorker[Inventory, String] {
  private val v500 = DatabaseVersion(5, 0, 0)

  private val errors = collection.mutable.ArrayBuffer[String]()

  def warnings = errors.toSeq

  private def converToNormal(card: Card) = SingleCard(
    CardLayout.NORMAL,
    card.name.get(0),
    card.manaCost.get(0),
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

  private def createMultiFacedCard(layout: CardLayout, faces: Seq[Card]) = {
    import CardLayout._

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
        if (!error) Set(SplitCard(faces.asJava)) else Set.empty
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
    if (error) faces.map(converToNormal(_)).toSet else result
  }

  protected override def doInBackground() = {
    val format = SimpleDateFormat("yyyy-MM-dd")

    publish(s"Opening ${file.getName}...")

    val cards = collection.mutable.ArrayBuffer[Card]()
    val faces = collection.mutable.Map[Card, Seq[String]]()
    val expansions = collection.mutable.Set[Expansion]()
    val blockNames = collection.mutable.Set[String]()
    val multiUUIDs = collection.mutable.Map[String, Card]()
    val facesNames = collection.mutable.Map[Card, Seq[String]]()
    val otherFaceIds = collection.mutable.Map[Card, Seq[String]]()

    // Read the inventory file
    Using.resource(BufferedReader(InputStreamReader(FileInputStream(file), "UTF8"))){ reader =>
      publish(s"Parsing ${file.getName}...")

      val root = JsonParser().parse(reader).getAsJsonObject
      val version = if (root.has("meta")) DatabaseVersion.parseVersion(root.get("meta").getAsJsonObject.get("version").getAsString) else DatabaseVersion(0, 0, 0)

      val entries = (if (version < v500) root else root.get("data").getAsJsonObject).entrySet.asScala.map((e) => e.getKey -> e.getValue).toMap
      var numCards = entries.map{ case (_, e) => e.getAsJsonObject.get("cards").getAsJsonArray.size }.sum

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
      setProgress(0)
      entries.foreach{ case (_, e) =>
        val setProperties = e.getAsJsonObject
        val setCards = setProperties.get("cards").getAsJsonArray
        val set = Expansion(
          setProperties.get("name").getAsString,
          Option(setProperties.get("block")).map(_.getAsString).getOrElse(Expansion.NoBlock),
          setProperties.get("code").getAsString,
          setCards.size,
          LocalDate.parse(setProperties.get("releaseDate").getAsString, Expansion.DateFormatter)
        )
        expansions += set
        blockNames += set.block

        publish(s"Loading cards from $set...")
        setCards.asScala.foreach{ cardElement =>
          // Create the new card for the expansion
          val card = cardElement.getAsJsonObject

          // Card's multiverseid and Scryfall id
          val scryfallid = (if (version < v500) card.get("scryfallId") else card.get("identifiers").getAsJsonObject.get("scryfallId")).getAsString
          val multiverseid = Option(if (version < v500) card.get("multiverseid") else card.get("identifiers").getAsJsonObject.get("multiverseid")).map(_.getAsInt).getOrElse(-1)

          // Card's name
          val name = card.get(if (card.has("faceName")) "faceName" else "name").getAsString

          // If the card is a token, skip it
          try {
            val layout = CardLayout.valueOf(card.get("layout").getAsString.toUpperCase.replaceAll("[^A-Z]", "_"))

            // Rulings
            val rulings = collection.mutable.TreeMap[Date, collection.mutable.ArrayBuffer[String]]()
            Option(card.get("rulings")).toSeq.flatMap(_.getAsJsonArray.asScala.map(_.getAsJsonObject)).foreach{ o =>
              val r = o.get("text").getAsString
              val ruling = rulingContents.getOrElseUpdate(r, r)
              try {
                val date = rulingDates.getOrElseUpdate(o.get("date").getAsString, format.parse(o.get("date").getAsString))
                if (!rulings.contains(date))
                  rulings += date -> collection.mutable.ArrayBuffer[String]()
                rulings(date) += ruling
              } catch case x: ParseException => errors += s"$name ($set): ${x.getMessage}"
            }

            // Format legality
            val legality = card.get("legalities").getAsJsonObject.entrySet.asScala.map((e) => formats.getOrElseUpdate(e.getKey, e.getKey) -> Legality.parseLegality(e.getValue.getAsString)).toMap

            // Formats the card can be commander in
            val commandFormats = Option(card.get("leadershipSkills")).toSeq.flatMap(_.getAsJsonObject.entrySet.asScala.collect{ case e if e.getValue.getAsBoolean =>
              formats.getOrElseUpdate(e.getKey, e.getKey)
            }.toSeq.sorted)

            val c = SingleCard(
              layout,
              name,
              costs.getOrElseUpdate(Option(card.get("manaCost")).map(_.getAsString).getOrElse(""), ManaCost.parseManaCost(Option(card.get("manaCost")).map(_.getAsString).getOrElse(""))),
              colorLists.getOrElseUpdate(card.get("colors").getAsJsonArray.toString, {
                val col = collection.mutable.ArrayBuffer[ManaType]()
                card.get("colors").getAsJsonArray.asScala.foreach((e) => col += ManaType.parseManaType(e.getAsString))
                col.toSeq
              }).asJava,
              colorLists.getOrElseUpdate(card.get("colorIdentity").getAsJsonArray.toString, {
                val col = collection.mutable.ArrayBuffer[ManaType]()
                card.get("colorIdentity").getAsJsonArray.asScala.foreach((e) => col += ManaType.parseManaType(e.getAsString))
                col.toSeq
              }).asJava,
              supertypeSets.getOrElseUpdate(card.get("supertypes").getAsJsonArray.toString, {
                val s = collection.mutable.LinkedHashSet[String]()
                card.get("supertypes").getAsJsonArray.asScala.foreach((e) => s += allSupertypes.getOrElseUpdate(e.getAsString, e.getAsString))
                s
              }).asJava,
              typeSets.getOrElseUpdate(card.get("types").getAsJsonArray.toString, {
                val s = collection.mutable.LinkedHashSet[String]()
                card.get("types").getAsJsonArray.asScala.foreach((e) => s += allTypes.getOrElseUpdate(e.getAsString, e.getAsString))
                s
              }).asJava,
              subtypeSets.getOrElseUpdate(card.get("subtypes").getAsJsonArray.toString, {
                val s = collection.mutable.LinkedHashSet[String]()
                card.get("subtypes").getAsJsonArray.asScala.foreach((e) => s += allSubtypes.getOrElseUpdate(e.getAsString, e.getAsString))
                s
              }).asJava,
              printedTypes.getOrElseUpdate(Option(card.get("originalType")).map(_.getAsString).getOrElse(""), Option(card.get("originalType")).map(_.getAsString).getOrElse("")),
              Rarity.parseRarity(card.get("rarity").getAsString),
              set,
              texts.getOrElseUpdate(Option(card.get("text")).map(_.getAsString).getOrElse(""), Option(card.get("text")).map(_.getAsString).getOrElse("")),
              flavors.getOrElseUpdate(Option(card.get("flavorText")).map(_.getAsString).getOrElse(""), Option(card.get("flavorText")).map(_.getAsString).getOrElse("")),
              texts.getOrElseUpdate(Option(card.get("originalText")).map(_.getAsString).getOrElse(""), Option(card.get("originalText")).map(_.getAsString).getOrElse("")),
              artists.getOrElseUpdate(Option(card.get("artist")).map(_.getAsString).getOrElse(""), Option(card.get("artist")).map(_.getAsString).getOrElse("")),
              multiverseid,
              scryfallid,
              numbers.getOrElseUpdate(Option(card.get("number")).map(_.getAsString).getOrElse(""), Option(card.get("number")).map(_.getAsString).getOrElse("")),
              stats.getOrElseUpdate(Option(card.get("power")).map(_.getAsString).getOrElse(""), CombatStat(Option(card.get("power")).map(_.getAsString).getOrElse(""))),
              stats.getOrElseUpdate(Option(card.get("tougness")).map(_.getAsString).getOrElse(""), CombatStat(Option(card.get("toughness")).map(_.getAsString).getOrElse(""))),
              loyalties.getOrElseUpdate(Option(card.get("loyalty")).map(l => if (l.isJsonNull) "X" else l.getAsString).getOrElse(""), Loyalty(Option(card.get("loyalty")).map(l => if (l.isJsonNull) "X" else l.getAsString).getOrElse(""))),
              java.util.TreeMap(rulings.map{ case (d, r) => d -> r.asJava }.asJava),
              legality.asJava,
              commandFormats.asJava
            )

            // Collect unexpected card values
            if (c.artist.isEmpty)
              errors += s"${c.unifiedName} (${c.expansion}): Missing artist!"

            // Add to map of faces if the card has multiple faces
            if (layout.isMultiFaced) {
              if (version < v500)
                faces += c -> card.get("names").getAsJsonArray.asScala.map(_.getAsString).toSeq
              else {
                multiUUIDs += card.get("uuid").getAsString -> c
                facesNames += c -> card.get("name").getAsString.split(Card.FACE_SEPARATOR).toSeq
                otherFaceIds += c -> card.get("otherFaceIds").getAsJsonArray.asScala.map(_.getAsString).toSeq
              }
            }

            cards += c
            setProgress(cards.size*100/numCards)
          } catch case e: IllegalArgumentException => errors += s"$name ($set): ${e.getMessage}"
        }
      }

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
            val second = otherFaces(2)
            otherFaces.remove(1, 2)
            otherFaces.insertAll(1, Seq(second, first))
          }
          cards ++= createMultiFacedCard(face.layout, otherFaces.toSeq)
        }
      } else {
        cards --= facesNames.keys
        faces.foreach{ case (face, names) =>
          val cardFaces = collection.mutable.ArrayBuffer(otherFaceIds(face).map(multiUUIDs(_)).toSeq:_*)
          cardFaces += face
          cardFaces.sortInPlaceBy((c) => names.indexOf(c.unifiedName))

          if (face.layout != CardLayout.MELD || cardFaces.size == 3)
            cards ++= createMultiFacedCard(face.layout, cardFaces.toSeq)
        }
      }

      publish("Removing duplicate entries...")
      val unique = collection.mutable.Map[String, Card]()
      cards.foreach((c) => if (!unique.contains(c.scryfallid.get(0))) {
        unique += c.scryfallid.get(0) -> c
      })
      cards.clear()
      cards ++= unique.values

      // Store the lists of expansion and block names and types and sort them alphabetically
      Expansion.expansions = expansions.toArray.sorted
      Expansion.blocks = blockNames.toArray.sorted
      SupertypeFilter.supertypeList = allSupertypes.values.toArray.sorted
      CardTypeFilter.typeList = allTypes.values.toArray.sorted
      SubtypeFilter.subtypeList = allSubtypes.values.toArray.sorted
    }

    val inventory = Inventory(cards.asJava)

    if (Files.exists(Path.of(SettingsDialog.settings.inventory.tags))) {
      val tk = new TypeToken[java.util.Map[String, java.util.Set[String]]] {}
      val raw = MainFrame.Serializer.fromJson(Files.readAllLines(Path.of(SettingsDialog.settings.inventory.tags)).asScala.mkString("\n"), tk.getType).asInstanceOf[java.util.Map[String, java.util.Set[String]]].asScala.map{ case (n, t) => n -> t.asScala.toSet }.toMap
      Card.tags.clear()
      Card.tags.putAll(raw.map{ case (name, tags) => inventory.find(name) -> tags.asJava }.asJava)
    }

    inventory
  }

  protected override def process(chunks: java.util.List[String]) = chunks.asScala.foreach(consumer(_))
  protected override def done() = finished()
}
