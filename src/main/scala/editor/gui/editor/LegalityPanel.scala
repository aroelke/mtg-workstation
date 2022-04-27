package editor.gui.editor

import editor.collection.CardList
import editor.collection.deck.Deck
import editor.database.FormatConstraints
import editor.database.attributes.Legality
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.database.symbol.ManaSymbolInstances.ColorSymbol
import editor.gui.generic.ComponentUtils
import editor.gui.settings.SettingsDialog
import editor.util.UnicodeSymbols

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListSelectionModel
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import scala.jdk.CollectionConverters._
import scala.util.matching._

/**
 * Panel for showing the format legality of the deck, optionally including a sideboard (if
 * the format permits one). For formats with commanders, the commander is inferred from among
 * the cards in the deck and/or the additional lists. If the deck is not legal in any formats,
 * the reasons why can be displayed.
 * 
 * @constructor create a new panel for displaying format legality for a deck
 * @param editor frame containing the deck to determine legality for
 *
 * @author Alec Roelke
 */
class LegalityPanel(editor: EditorFrame) extends Box(BoxLayout.Y_AXIS) {
  private val MainDeck = "Main Deck"
  private val AllLists = "All Lists"
  private val PartnerPattern = "partner(?: with (.+) \\()?".r

  setPreferredSize(Dimension(400, 250))

  private val warnings = FormatConstraints.FormatNames.map(_ -> collection.mutable.ArrayBuffer[String]()).toMap

  // Panel containing format lists
  private val listsPanel = JPanel(GridLayout(1, 2))
  add(listsPanel)

  // Panel containing legal formats list
  private val legalPanel = JPanel(BorderLayout())
  legalPanel.setBorder(BorderFactory.createTitledBorder("Legal in:"))
  listsPanel.add(legalPanel)

  // Legal formats list.  Selection is disabled in this list
  private val legalList = JList[String]()
  legalList.setSelectionModel(new DefaultListSelectionModel {
    override def getSelectionMode = ListSelectionModel.SINGLE_SELECTION
    override def setSelectionInterval(index0: Int, index1: Int) = super.setSelectionInterval(-1, -1)
  })
  legalPanel.add(JScrollPane(legalList), BorderLayout.CENTER)

  // Panel containing illegal formats list
  private val illegalPanel = JPanel(BorderLayout())
  illegalPanel.setBorder(BorderFactory.createTitledBorder("Illegal in:"))
  listsPanel.add(illegalPanel)

  // Illegal formats list.  Only one element can be selected at a time.
  private val illegalList = JList[String]()
  illegalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  illegalPanel.add(JScrollPane(illegalList), BorderLayout.CENTER)

  // Panel containing check box for enabling commander search
  private val cmdrPanel = Box.createHorizontalBox()
  private val cmdrCheck = JCheckBox("", SettingsDialog.settings.editor.legality.searchForCommander)
  cmdrCheck.setText(if (cmdrCheck.isSelected) "Search for commander in:" else "Search for commander")
  cmdrPanel.add(cmdrCheck)
  private val names = collection.mutable.ArrayBuffer(MainDeck, AllLists)
  names ++= editor.extras.map(_.name)
  private val cmdrBox = JComboBox(names.toArray)
  cmdrBox.setVisible(SettingsDialog.settings.editor.legality.searchForCommander)
  if (SettingsDialog.settings.editor.legality.main)
    cmdrBox.setSelectedIndex(names.indexOf(MainDeck))
  else if (SettingsDialog.settings.editor.legality.all)
    cmdrBox.setSelectedIndex(names.indexOf(AllLists))
  else {
    val name = SettingsDialog.settings.editor.legality.list
    cmdrBox.setSelectedIndex(if (names.contains(name)) names.indexOf(name) else names.indexOf(MainDeck))
  }
  cmdrBox.setMaximumSize(cmdrBox.getPreferredSize)
  cmdrPanel.add(cmdrBox)
  cmdrPanel.add(Box.createHorizontalGlue)
  add(cmdrPanel)

  // Panel containing check box for including a sideboard
  private val (sideCheck, sideCombo) = if (!editor.extras.isEmpty) {
    val sb = SettingsDialog.settings.editor.legality.sideboard

    add(Box.createVerticalStrut(2))
    val sideboardBox = Box.createHorizontalBox
    val check = JCheckBox("", !sb.isEmpty && editor.getExtraNames.contains(sb))
    sideboardBox.add(check)
    val combo = JComboBox(editor.extras.map(_.name).toArray)
    combo.setSelectedIndex(math.max(0, editor.extras.map(_.name).indexOf(sb)))
    combo.setMaximumSize(combo.getPreferredSize)
    sideboardBox.add(combo)
    sideboardBox.add(Box.createHorizontalGlue)
    add(sideboardBox)
    (Some(check), Some(combo))
  } else (None, None)

  private val listener: ActionListener = _ -> {
    sideCheck.foreach((c) => c.setText(if (c.isSelected) "Sideboard is:" else "Include sideboard"))
    sideCombo.foreach(_.setVisible(sideCheck.exists(_.isSelected)))

    cmdrCheck.setText(s"""Search for commander${if (cmdrCheck.isSelected) " in:" else ""}""")
    cmdrBox.setVisible(cmdrCheck.isSelected)
    checkLegality(editor.lists(EditorFrame.MainDeck), if (!cmdrCheck.isSelected) Deck() else (cmdrBox.getSelectedItem.toString match {
        case MainDeck => editor.lists(EditorFrame.MainDeck)
        case AllLists => editor.allExtras
        case _ => editor.lists(cmdrBox.getSelectedItem.toString)
    }), Option.when(!editor.extras.isEmpty && sideCheck.exists(_.isSelected))(sideCombo.map((c) => editor.lists(c.getItemAt(c.getSelectedIndex))).getOrElse(Deck())))
  }
  sideCheck.foreach(_.addActionListener(listener))
  sideCombo.foreach(_.addActionListener(listener))
  cmdrCheck.addActionListener(listener)
  cmdrBox.addActionListener(listener)

  // Panel containing text box that shows why a deck is illegal in a format
  private val warningsPanel = JPanel(BorderLayout())
  warningsPanel.setBorder(BorderFactory.createTitledBorder("Warnings"))
  add(warningsPanel)

  // Text box that shows reasons for illegality
  private val warningsList = JList[String]()
  warningsList.setSelectionModel(new DefaultListSelectionModel {
    override def getSelectionMode = ListSelectionModel.SINGLE_SELECTION
    override def setSelectionInterval(index0: Int, index1: Int) = super.setSelectionInterval(-1, -1)
  })
  warningsList.setCellRenderer((_, v, _, _, _) => s"(?:${ManaCost.Pattern.regex})+".r.findFirstMatchIn(v).map((m) => {
    val cell = Box.createHorizontalBox
    cell.add(JLabel(v.substring(0, m.start(0))))
    ManaCost.parse(m.group(0)).get.foreach((symbol) => cell.add(JLabel(symbol.getIcon(ComponentUtils.TextSize))))
    cell
  }).getOrElse(JLabel(v)))
  warningsPanel.add(JScrollPane(warningsList), BorderLayout.CENTER)

  // Click on a list element to show why it is illegal
  illegalList.addListSelectionListener(_ => {
    if (illegalList.getSelectedIndex >= 0)
      warningsList.setListData(warnings(illegalList.getSelectedValue).map((s) => s"${UnicodeSymbols.BULLET} $s").toArray)
    else
      warningsList.setListData(Array.empty[String])
  })

  listener.actionPerformed(ActionEvent(cmdrCheck, 0, "", ActionEvent.ACTION_PERFORMED))

  /**
   * Check the legality of a deck and update the panel accordingly.
   * 
   * @param deck deck to check
   * @param commanderSearch list to look for the commander in; use an empty list to indicate
   * a search in all lists in the frame
   * @param sideboard which list to use as sideboard if applicable
   */
  def checkLegality(deck: CardList, commanderSearch: CardList, sideboard: Option[CardList]) = {
    val isoNameCounts = collection.mutable.Map[Card, Int]()
    deck.asScala.foreach{ c =>
      var counted = false
      isoNameCounts.keySet.foreach{ name =>
        if (!counted && name.compareName(c) == 0) {
          isoNameCounts(name) += deck.getEntry(name).count
          counted = true
        }
      }
      if (!counted)
        isoNameCounts.put(c, deck.getEntry(c).count())
    }
    val deckColorIdentity = deck.asScala.flatMap(_.colorIdentity).toSet

    warnings.foreach{ case (format, warning) =>
      warning.clear()
      val constraints = FormatConstraints.Constraints(format)

      // Commander(s) exist(s) and deck matches color identity
      val (commander, partners) = if (!commanderSearch.isEmpty && constraints.hasCommander) {
        val possibleCommanders = commanderSearch.asScala.filter(_.commandFormats.contains(format))
        val commander = possibleCommanders.exists(c => deckColorIdentity.forall(c.colorIdentity.contains(_)))

        val possiblePartners = possibleCommanders
            .flatMap((c) => c.normalizedOracle.map(c -> PartnerPattern.findFirstMatchIn(_)))
            .collect{ case (c, Some(m)) if c.commandFormats.contains(format) => c -> Option(m.group(1)).map(_.toLowerCase).getOrElse("") }
            .toMap
        val partners = possiblePartners.exists{ case (card, partner) => possibleCommanders.exists{ commander =>
          val colorIdentity = if ((partner.isEmpty && commander.normalizedOracle.flatMap(PartnerPattern.findFirstMatchIn(_)).exists(_.group(1) == null)) ||
                                  partner.equalsIgnoreCase(commander.name))
            (card.colorIdentity ++ commander.colorIdentity).toSet
          else
            Set.empty

          deckColorIdentity.forall(colorIdentity.contains(_))
        }}
        (commander, partners)
      } else (false, false)
      if (!commanderSearch.isEmpty && constraints.hasCommander && !commander && !partners)
        warnings(format) += s"""Could not find a $format-legal legendary creature whose color identity contains ${deckColorIdentity.toSeq.sortBy(_.ordinal).map(ColorSymbol(_).toString).mkString}"""

      // Deck size
      if (constraints.hasCommander) {
        if (((commanderSearch.isEmpty || commanderSearch == deck) && deck.total != constraints.deckSize) ||
            ((!commanderSearch.isEmpty && commanderSearch != deck) &&
             (commander && deck.total != constraints.deckSize - 1) || (partners && deck.total != constraints.deckSize - 2)))
          warnings(format) += s"Deck does not contain exactly ${constraints.deckSize - 1} cards plus a commander or ${constraints.deckSize - 2} cards plus two partner commanders"
      } else if (deck.total < constraints.deckSize)
        warnings(format) += s"Deck contains fewer than ${constraints.deckSize} cards"

      // Individual card legality and count
      deck.asScala.foreach{ c =>
        val maxCopies = constraints.maxCopies
        if (!c.legalityIn(format).isLegal)
          warnings(format) += s"${c.name} is illegal in $format"
        else if (isoNameCounts.contains(c) && !c.ignoreCountRestriction) {
          if (c.legalityIn(format) == Legality.RESTRICTED && isoNameCounts(c) > 1)
            warnings(format) += s"${c.name} is restricted in $format"
          else if (isoNameCounts(c) > maxCopies)
            warnings(format) += s"Deck contains more than $maxCopies copies of ${c.name}"
        }
      }

      // Sideboard size
      sideboard.foreach{ sb =>
        if (sb.total > constraints.sideboardSize)
          warnings(format) += s"Sideboard contains more than ${constraints.sideboardSize} cards"
      }
    }

    // Collate the legality lists
    val illegal = warnings.collect{ case (s, w) if !w.isEmpty => s }.toSeq.sorted
    val legal = FormatConstraints.FormatNames.filterNot(illegal.contains(_)).sorted

    warningsList.setListData(Array.empty[String])
    legalList.setListData(legal.toArray)
    illegalList.setListData(illegal.toArray)
  }
}
