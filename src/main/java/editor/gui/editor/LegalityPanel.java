package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import editor.collection.CardList;
import editor.database.attributes.Legality;
import editor.database.attributes.ManaType;
import editor.database.card.Card;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that shows the formats a deck is legal and illegal in.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LegalityPanel extends Box
{
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
        checkLegality(editor.getList(EditorFrame.MAIN_DECK));

        // Panel containing format lists
        JPanel listsPanel = new JPanel(new GridLayout(1, 2));
        add(listsPanel);

        // Panel containing legal formats list
        JPanel legalPanel = new JPanel(new BorderLayout());
        legalPanel.setBorder(BorderFactory.createTitledBorder("Legal in:"));
        listsPanel.add(legalPanel);

        // Legal formats list.  Selection is disabled in this list
        JList<String> legalList = new JList<>(legal.toArray(String[]::new));
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
        JList<String> illegalList = new JList<>(illegal.toArray(String[]::new));
        illegalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        illegalPanel.add(new JScrollPane(illegalList), BorderLayout.CENTER);

        // Panel containing check box for enabling commander search
        Box cmdrPanel = new Box(BoxLayout.X_AXIS);
        JCheckBox cmdrCheck = new JCheckBox("Search for commander", false);
        cmdrPanel.add(cmdrCheck);
        JComboBox<String> cmdrBox = new JComboBox<>(new String[] {"Main Deck", "All Lists"});
        cmdrBox.setVisible(false);
        cmdrPanel.add(cmdrBox);
        cmdrPanel.add(Box.createHorizontalGlue());
        add(cmdrPanel);
        cmdrCheck.addActionListener((e) -> {
            cmdrCheck.setText("Search for commander" + (cmdrCheck.isSelected() ? " in:" : ""));
            cmdrBox.setVisible(cmdrCheck.isSelected());
        });

        // Panel containing text box that shows why a deck is illegal in a format
        JPanel warningsPanel = new JPanel(new BorderLayout());
        warningsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Warnings"), BorderFactory.createLoweredBevelBorder()));
        add(warningsPanel);

        // Text box that shows reasons for illegality
        JTextPane warningsPane = new JTextPane();
        warningsPane.setEditable(false);
        warningsPane.setFont(UIManager.getFont("Label.font"));
        warningsPanel.add(new JScrollPane(warningsPane), BorderLayout.CENTER);

        // Click on a list element to show why it is illegal
        illegalList.addListSelectionListener((e) -> {
            StringJoiner str = new StringJoiner("\n" + UnicodeSymbols.BULLET + " ", String.valueOf(UnicodeSymbols.BULLET) + " ", "");
            for (String warning : warnings.get(illegalList.getSelectedValue()))
                str.add(warning);
            warningsPane.setText(str.toString());
            warningsPane.setCaretPosition(0);
        });
    }

    /**
     * Check which formats a deck is legal in, and the reasons for why it is illegal in
     * others.
     *
     * @param deck deck to check
     */
    public void checkLegality(CardList deck)
    {
        // Deck size
        for (String format : LegalityFilter.formatList)
        {
            if (format.equals("commander"))
            {
                if (deck.total() != 100)
                    warnings.get(format).add("Deck does not contain exactly 100 cards");
            }
            else if (format.equals("brawl"))
            {
                if (deck.total() != 60)
                    warnings.get(format).add("Deck does not contain exactly 60 cards");
            }
            else
            {
                if (deck.total() < 60)
                    warnings.get(format).add("Deck contains fewer than 60 cards");
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

        // Commander only: commander exists and matches deck color identity
        var possibleCommanders = deck.stream().filter(Card::canBeCommander).collect(Collectors.toList());
        if (possibleCommanders.isEmpty())
            warnings.get("commander").add("Deck does not contain a legendary creature");
        else
        {
            List<ManaType> deckColorIdentityList = new ArrayList<>();
            for (Card c : deck)
                deckColorIdentityList.addAll(c.colors());
            var deckColorIdentity = new ArrayList<>(deckColorIdentityList);
            for (Card c : new ArrayList<>(possibleCommanders))
                if (!c.colors().containsAll(deckColorIdentity))
                    possibleCommanders.remove(c);
            if (possibleCommanders.isEmpty())
                warnings.get("commander").add("Deck does not contain a legendary creature whose color identity contains " + deckColorIdentity.toString());
        }

        // Collate the legality lists
        illegal = warnings.keySet().stream().filter((s) -> !warnings.get(s).isEmpty()).collect(Collectors.toList());
        Collections.sort(illegal);
        legal = new ArrayList<>(Arrays.asList(LegalityFilter.formatList));
        legal.removeAll(illegal);
    }
}
