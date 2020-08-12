package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
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
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.gui.generic.ComponentUtils;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that shows the formats a deck is legal and illegal in.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LegalityPanel extends Box
{
    private static final String MAIN_DECK = "Main Deck";
    private static final String ALL_LISTS = "All Lists";

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
    private JList<String> legalList;
    private JList<String> illegalList;
    private JList<String> warningsList;

    /**
     * Create a new LegalityPanel showing the legality of a deck.
     *
     * @param legality legality of a deck.  Make sure to have it calculate
     *                 the legality of a deck, or nothing will be shown.
     */
    public LegalityPanel(EditorFrame editor)
    {
        super(BoxLayout.Y_AXIS);
        setPreferredSize(new Dimension(400, 250));

        warnings = Arrays.stream(LegalityFilter.formatList).collect(Collectors.toMap(Function.identity(), (l) -> new ArrayList<String>()));

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
        Box cmdrPanel = new Box(BoxLayout.X_AXIS);
        JCheckBox cmdrCheck = new JCheckBox("Search for commander", false);
        cmdrPanel.add(cmdrCheck);
        List<String> names = new ArrayList<>(List.of(MAIN_DECK, ALL_LISTS));
        names.addAll(editor.getExtraNames());
        JComboBox<String> cmdrBox = new JComboBox<>(names.toArray(String[]::new));
        cmdrBox.setVisible(false);
        cmdrPanel.add(cmdrBox);
        cmdrPanel.add(Box.createHorizontalGlue());
        add(cmdrPanel);

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
        ActionListener cmdrListener = (e) -> {
            cmdrCheck.setText("Search for commander" + (cmdrCheck.isSelected() ? " in:" : ""));
            cmdrBox.setVisible(cmdrCheck.isSelected());
            checkLegality(editor.getList(EditorFrame.MAIN_DECK), !cmdrCheck.isSelected() ? new Deck() : switch (cmdrBox.getSelectedItem().toString()) {
                case MAIN_DECK -> editor.getList(EditorFrame.MAIN_DECK);
                case ALL_LISTS -> editor.getExtraCards();
                default -> editor.getList(cmdrBox.getSelectedItem().toString());
            });
        };
        cmdrCheck.addActionListener(cmdrListener);
        cmdrBox.addActionListener(cmdrListener);

        checkLegality(editor.getList(EditorFrame.MAIN_DECK), new Deck());
    }

    /**
     * Check which formats a deck is legal in, and the reasons for why it is illegal in
     * others.
     *
     * @param deck deck to check
     */
    public void checkLegality(CardList deck, CardList commanderSearch)
    {
        for (var warning : warnings.values())
            warning.clear();

        // Deck size
        for (String format : FormatConstraints.FORMAT_NAMES)
        {
            final FormatConstraints constraints = FormatConstraints.CONSTRAINTS.get(format);
            if (constraints.hasCommander)
            {
                if (((commanderSearch.isEmpty() || commanderSearch == deck) && deck.total() != constraints.deckSize) || ((!commanderSearch.isEmpty() && commanderSearch != deck) && deck.total() != constraints.deckSize - 1))
                    warnings.get(format).add("Deck does not contain exactly " + (constraints.deckSize - 1) + " cards plus a commander");
            }
            else
            {
                if (deck.total() < constraints.deckSize)
                    warnings.get(format).add("Deck contains fewer than " + constraints.deckSize + " cards");
            }
        }

        // Individual card legality and count
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
        for (Card c : deck)
        {
            for (String format : LegalityFilter.formatList)
            {
                if (!c.legalityIn(format).isLegal)
                    warnings.get(format).add(c.unifiedName() + " is illegal in " + format);
                else if (isoNameCounts.containsKey(c) && !c.ignoreCountRestriction())
                {
                    if (format.equals("commander"))
                    {
                        if (isoNameCounts.get(c) > 1)
                            warnings.get(format).add("Deck contains more than 1 copy of " + c.unifiedName());
                    }
                    else
                    {
                        if (c.legalityIn(format) == Legality.RESTRICTED && isoNameCounts.get(c) > 1)
                            warnings.get(format).add(c.unifiedName() + " is restricted in " + format);
                        else if (isoNameCounts.get(c) > 4)
                            warnings.get(format).add("Deck contains more than 4 copies of " + c.unifiedName());
                    }
                }
            }
        }

        // Commander/Brawl only: commander exists and matches deck color identity
        if (!commanderSearch.isEmpty())
        {
            var possibleCommanders = commanderSearch.stream().filter(Card::canBeCommander).collect(Collectors.toList());
            if (possibleCommanders.isEmpty())
            {
                final String warning = "Could not find a legendary creature";
                warnings.get("commander").add(warning);
                warnings.get("brawl").add(warning);
            }
            else
            {
                Set<ManaType> deckColorIdentity = new HashSet<>();
                for (Card c : deck)
                    deckColorIdentity.addAll(c.colorIdentity());
                for (Card c : new ArrayList<>(possibleCommanders))
                    if (!c.colorIdentity().containsAll(deckColorIdentity))
                        possibleCommanders.remove(c);
                if (possibleCommanders.isEmpty())
                    warnings.get("commander").add("Could not find a legendary creature whose color identity contains " + deckColorIdentity.stream().sorted().map((t) -> ColorSymbol.SYMBOLS.get(t).toString()).collect(Collectors.joining()));
            }
        }

        // Collate the legality lists
        illegal = warnings.keySet().stream().filter((s) -> !warnings.get(s).isEmpty()).collect(Collectors.toList());
        Collections.sort(illegal);
        legal = new ArrayList<>(Arrays.asList(LegalityFilter.formatList));
        legal.removeAll(illegal);
        Collections.sort(legal);

        warningsList.setListData(new String[0]);
        legalList.setListData(legal.toArray(String[]::new));
        illegalList.setListData(illegal.toArray(String[]::new));
    }
}
