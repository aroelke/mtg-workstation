package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import editor.collection.CardList;
import editor.collection.deck.Deck;
import editor.database.FormatConstraints;
import editor.database.attributes.Legality;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.card.Card;
import editor.database.symbol.ColorSymbol;
import editor.database.symbol.ManaSymbol;
import editor.gui.generic.ComponentUtils;
import editor.gui.settings.SettingsDialog;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that shows the formats a deck is legal and illegal in.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LegalityPanel extends Box
{
    /** Item to show for searching the main deck. */
    private static final String MAIN_DECK = "Main Deck";
    /** Item to show for searching all of the lists. */
    private static final String ALL_LISTS = "All Lists";
    /** Regex pattern used to detect if a card can be a partner. */
    private static final Pattern PARTNER_PATTERN = Pattern.compile("partner(?: with (.+) \\()?");

    /**
     * Array containing formats the deck is illegal in.
     */
    private List<String> illegal;
    /**
     * Array containing formats the deck is legal in.
     */
    private List<String> legal;
    /**
     * Map of formats to reasons for being illegal in them.  Contents of the map are lists
     * of Strings, which will be empty for legal formats.
     */
    private Map<String, List<String>> warnings;
    /**
     * List showing which formats the deck is legal in.
     */
    private JList<String> legalList;
    /**
     * List showing which formats the deck is illegal in.
     */
    private JList<String> illegalList;
    /**
     * List showing reasons for why the deck is illegal in the selected format.
     */
    private JList<String> warningsList;

    /**
     * Create a new LegalityPanel showing the legality of a deck.
     *
     * @param editor editor frame containing the deck to check
     */
    public LegalityPanel(EditorFrame editor)
    {
        super(BoxLayout.Y_AXIS);
        setPreferredSize(new Dimension(400, 250));

        warnings = FormatConstraints.FORMAT_NAMES.stream().collect(Collectors.toMap(Function.identity(), (l) -> new ArrayList<String>()));

        // Panel containing format lists
        JPanel listsPanel = new JPanel(new GridLayout(1, 2));
        add(listsPanel);

        // Panel containing legal formats list
        JPanel legalPanel = new JPanel(new BorderLayout());
        legalPanel.setBorder(BorderFactory.createTitledBorder("Legal in:"));
        listsPanel.add(legalPanel);

        // Legal formats list.  Selection is disabled in this list
        legalList = new JList<>();
        legalList.setSelectionModel(new DefaultListSelectionModel()
        {
            @Override
            public int getSelectionMode()
            {
                return ListSelectionModel.SINGLE_SELECTION;
            }

            @Override
            public void setSelectionInterval(int index0, int index1)
            {
                super.setSelectionInterval(-1, -1);
            }
        });
        legalPanel.add(new JScrollPane(legalList), BorderLayout.CENTER);

        // Panel containing illegal formats list
        JPanel illegalPanel = new JPanel(new BorderLayout());
        illegalPanel.setBorder(BorderFactory.createTitledBorder("Illegal in:"));
        listsPanel.add(illegalPanel);

        // Illegal formats list.  Only one element can be selected at a time.
        illegalList = new JList<>();
        illegalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        illegalPanel.add(new JScrollPane(illegalList), BorderLayout.CENTER);

        // Panel containing check box for enabling commander search
        Box cmdrPanel = Box.createHorizontalBox();
        JCheckBox cmdrCheck = new JCheckBox("", SettingsDialog.settings().editor.legality.searchForCommander);
        cmdrCheck.setText(cmdrCheck.isSelected() ? "Search for commander in:" : "Search for commander");
        cmdrPanel.add(cmdrCheck);
        List<String> names = new ArrayList<>(List.of(MAIN_DECK, ALL_LISTS));
        names.addAll(editor.getExtraNames());
        var cmdrBox = new JComboBox<>(names.toArray(String[]::new));
        cmdrBox.setVisible(SettingsDialog.settings().editor.legality.searchForCommander);
        if (SettingsDialog.settings().editor.legality.main)
            cmdrBox.setSelectedIndex(names.indexOf(MAIN_DECK));
        else if (SettingsDialog.settings().editor.legality.all)
            cmdrBox.setSelectedIndex(names.indexOf(ALL_LISTS));
        else
        {
            String name = SettingsDialog.settings().editor.legality.list;
            cmdrBox.setSelectedIndex(names.contains(name) ? names.indexOf(name) : names.indexOf(MAIN_DECK));
        }
        cmdrBox.setMaximumSize(cmdrBox.getPreferredSize());
        cmdrPanel.add(cmdrBox);
        cmdrPanel.add(Box.createHorizontalGlue());
        add(cmdrPanel);

        // Panel containing check box for including a sideboard
        final JCheckBox sideCheck;
        final JComboBox<String> sideCombo;
        if (!editor.getExtraNames().isEmpty())
        {
            String sb = SettingsDialog.settings().editor.legality.sideboard;

            add(Box.createVerticalStrut(2));
            Box sideboardBox = Box.createHorizontalBox();
            sideCheck = new JCheckBox("", !sb.isEmpty() && editor.getExtraNames().contains(sb));
            sideboardBox.add(sideCheck);
            sideCombo = new JComboBox<>(editor.getExtraNames().toArray(String[]::new));
            sideCombo.setSelectedIndex(Math.max(0, editor.getExtraNames().indexOf(sb)));
            sideCombo.setMaximumSize(sideCombo.getPreferredSize());
            sideboardBox.add(sideCombo);
            sideboardBox.add(Box.createHorizontalGlue());
            add(sideboardBox);
        }
        else
        {
            sideCheck = null;
            sideCombo = null;
        }

        ActionListener listener = (e) -> {
            if (!editor.getExtraNames().isEmpty())
            {
                sideCheck.setText(sideCheck.isSelected() ? "Sideboard is:" : "Include sideboard");
                sideCombo.setVisible(sideCheck.isSelected());
            }

            cmdrCheck.setText("Search for commander" + (cmdrCheck.isSelected() ? " in:" : ""));
            cmdrBox.setVisible(cmdrCheck.isSelected());
            checkLegality(editor.getList(EditorFrame.MAIN_DECK), !cmdrCheck.isSelected() ? new Deck() : switch (cmdrBox.getSelectedItem().toString()) {
                case MAIN_DECK -> editor.getList(EditorFrame.MAIN_DECK);
                case ALL_LISTS -> editor.getExtraCards();
                default -> editor.getList(cmdrBox.getSelectedItem().toString());
            }, !editor.getExtraNames().isEmpty() && sideCheck.isSelected() ? Optional.of(editor.getList(sideCombo.getItemAt(sideCombo.getSelectedIndex()))) : Optional.empty());
        };
        if (!editor.getExtraNames().isEmpty())
        {
            sideCheck.addActionListener(listener);
            sideCombo.addActionListener(listener);
        }
        cmdrCheck.addActionListener(listener);
        cmdrBox.addActionListener(listener);

        // Panel containing text box that shows why a deck is illegal in a format
        JPanel warningsPanel = new JPanel(new BorderLayout());
        warningsPanel.setBorder(BorderFactory.createTitledBorder("Warnings"));
        add(warningsPanel);

        // Text box that shows reasons for illegality
        warningsList = new JList<>();
        warningsList.setSelectionModel(new DefaultListSelectionModel()
        {
            @Override
            public int getSelectionMode()
            {
                return ListSelectionModel.SINGLE_SELECTION;
            }

            @Override
            public void setSelectionInterval(int index0, int index1)
            {
                super.setSelectionInterval(-1, -1);
            }
        });
        warningsList.setCellRenderer((l, v, i, s, c) -> {
            Matcher m = ManaCost.MANA_COST_PATTERN.matcher(v);
            if (m.find())
            {
                Box cell = Box.createHorizontalBox();
                cell.add(new JLabel(v.substring(0, m.start())));
                for (ManaSymbol symbol : ManaCost.parseManaCost(m.group()))
                    cell.add(new JLabel(symbol.getIcon(ComponentUtils.TEXT_SIZE)));
                return cell;
            }
            else
                return new JLabel(v);
        });
        warningsPanel.add(new JScrollPane(warningsList), BorderLayout.CENTER);

        // Click on a list element to show why it is illegal
        illegalList.addListSelectionListener((e) -> {
            if (illegalList.getSelectedIndex() >= 0)
                warningsList.setListData(warnings.get(illegalList.getSelectedValue()).stream().map((w) -> UnicodeSymbols.BULLET + " " + w).toArray(String[]::new));
                else
                    warningsList.setListData(new String[0]);
        });

        listener.actionPerformed(new ActionEvent(cmdrCheck, 0, "", ActionEvent.ACTION_PERFORMED));
    }

    /**
     * Check which formats a deck is legal in, and the reasons for why it is illegal in
     * others.
     *
     * @param deck deck to check
     */
    public void checkLegality(CardList deck, CardList commanderSearch, Optional<CardList> sideboard)
    {
        Map<Card, Integer> isoNameCounts = new HashMap<>();
        for (Card c : deck)
        {
            boolean counted = false;
            for (Card name : isoNameCounts.keySet())
            {
                if (name.compareName(c) == 0)
                {
                    isoNameCounts.compute(name, (k, v) -> v += deck.getEntry(name).count());
                    counted = true;
                    break;
                }
            }
            if (!counted)
                isoNameCounts.put(c, deck.getEntry(c).count());
        }
        Set<ManaType> deckColorIdentity = deck.stream().flatMap((c) -> c.colorIdentity().stream()).collect(Collectors.toSet());

        for (final String format : warnings.keySet())
        {
            warnings.get(format).clear();
            final FormatConstraints constraints = FormatConstraints.CONSTRAINTS.get(format);

            // Commander(s) exist(s) and deck matches color identity
            boolean commander = false;
            boolean partners = false;
            if (!commanderSearch.isEmpty())
            {
                if (constraints.hasCommander())
                {
                    var possibleCommanders = commanderSearch.stream().filter((c) -> c.commandFormats().contains(format)).collect(Collectors.toList());
                    for (Card c : new ArrayList<>(possibleCommanders))
                    {
                        if (c.colorIdentity().containsAll(deckColorIdentity))
                        {
                            commander = true;
                            break;
                        }
                    }
                    var possiblePartners = possibleCommanders.stream()
                        .flatMap((c) -> c.normalizedOracle().stream().map((o) -> new SimpleEntry<>(c, PARTNER_PATTERN.matcher(o))))
                        .filter((e) -> e.getKey().commandFormats().contains(format) && e.getValue().find())
                        .map((e) -> new SimpleEntry<>(e.getKey(), e.getValue().group(1) != null ? e.getValue().group(1).toLowerCase() : ""))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    search: for (var p : possiblePartners.entrySet())
                    {
                        for (Card c : possibleCommanders)
                        {
                            var colorIdentity = new HashSet<ManaType>();
                            if (p.getValue().isEmpty())
                            {
                                if (c.normalizedOracle().stream().map((o) -> PARTNER_PATTERN.matcher(o)).anyMatch((m) -> m.find() && m.group(1) == null))
                                {
                                    colorIdentity.addAll(p.getKey().colorIdentity());
                                    colorIdentity.addAll(c.colorIdentity());
                                }
                            }
                            else if (p.getValue().equalsIgnoreCase(c.unifiedName()))
                            {
                                colorIdentity.addAll(p.getKey().colorIdentity());
                                colorIdentity.addAll(c.colorIdentity());
                            }
                            if (colorIdentity.containsAll(deckColorIdentity))
                            {
                                partners = true;
                                break search;
                            }
                        }
                    }
                    if (!(commander || partners))
                        warnings.get(format).add("Could not find a legendary creature whose color identity contains " +
                        deckColorIdentity.stream().sorted().map((t) -> ColorSymbol.SYMBOLS.get(t).toString()).collect(Collectors.joining()));
                }
            }

            // Deck size
            if (constraints.hasCommander())
            {
                if (((commanderSearch.isEmpty() || commanderSearch == deck) && deck.total() != constraints.deckSize()) ||
                    ((!commanderSearch.isEmpty() && commanderSearch != deck) &&
                     (commander && deck.total() != constraints.deckSize() - 1) || (partners && deck.total() != constraints.deckSize() - 2)))
                    warnings.get(format).add("Deck does not contain exactly " + (constraints.deckSize() - 1) + " cards plus a commander or " +
                                                                                (constraints.deckSize() - 2) + " cards plus two partner commanders");
            }
            else
            {
                if (deck.total() < constraints.deckSize())
                    warnings.get(format).add("Deck contains fewer than " + constraints.deckSize() + " cards");
            }

            // Individual card legality and count
            for (Card c : deck)
            {
                final int maxCopies = constraints.maxCopies();
                if (!c.legalityIn(format).isLegal)
                    warnings.get(format).add(c.unifiedName() + " is illegal in " + format);
                else if (isoNameCounts.containsKey(c) && !c.ignoreCountRestriction())
                {
                    if (c.legalityIn(format) == Legality.RESTRICTED && isoNameCounts.get(c) > 1)
                        warnings.get(format).add(c.unifiedName() + " is restricted in " + format);
                    else if (isoNameCounts.get(c) > maxCopies)
                        warnings.get(format).add("Deck contains more than " + maxCopies + " copies of " + c.unifiedName());
                }
            }

            // Sideboard size
            sideboard.ifPresent((sb) -> {
                if (sb.total() > constraints.sideboardSize())
                    warnings.get(format).add("Sideboard contains more than " + constraints.sideboardSize() + " cards");
            });
        }

        // Collate the legality lists
        illegal = warnings.keySet().stream().filter((s) -> !warnings.get(s).isEmpty()).collect(Collectors.toList());
        Collections.sort(illegal);
        legal = new ArrayList<>(FormatConstraints.FORMAT_NAMES);
        legal.removeAll(illegal);
        Collections.sort(legal);

        warningsList.setListData(new String[0]);
        legalList.setListData(legal.toArray(String[]::new));
        illegalList.setListData(illegal.toArray(String[]::new));
    }
}
