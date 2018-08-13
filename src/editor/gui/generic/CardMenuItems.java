package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.collection.CardList;
import editor.database.card.Card;

/**
 * This class represents a list of menu items for manipulating cards in a deck. There are six:
 * add a single copy, fill a playset of copies, add some number of copies, remove one copy,
 * remove all copies, and remove some number of copies.
 *
 * @author Alec Roelke
 */
public class CardMenuItems
{
    /**
     * Array containing the menu items for manipulating card copies.
     */
    private JMenuItem[] items;

    /**
     * Create a new list of items for manipulating card copies using the given functions to do the
     * manipulation.
     *
     * @param parent parent component for the dialogs that will show up
     * @param list   supplier to get the list to change
     * @param cards  supplier to get the cards to use to make changes
     */
    public CardMenuItems(Component parent, Supplier<? extends CardList> list, Supplier<List<Card>> cards)
    {
        IntConsumer addN = (n) -> {
            if (list.get() != null)
                list.get().addAll(cards.get().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
        };
        Runnable fillPlayset = () -> {
            CardList l = list.get();
            if (l != null)
            {
                Map<Card, Integer> toAdd = new HashMap<>();
                for (Card c : cards.get())
                {
                    if (l.contains(c))
                        toAdd.put(c, 4 - l.getData(c).count());
                    else
                        toAdd.put(c, 4);
                }
                l.addAll(toAdd);
            }
        };
        IntConsumer removeN = (n) -> {
            if (list.get() != null)
                list.get().removeAll(cards.get().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
        };
        items = new JMenuItem[6];

        // Add single copy item
        items[0] = new JMenuItem("Add Single Copy");
        items[0].addActionListener((e) -> addN.accept(1));

        // Fill playset item
        items[1] = new JMenuItem("Fill Playset");
        items[1].addActionListener((e) -> fillPlayset.run());

        // Add variable item
        items[2] = new JMenuItem("Add Copies...");
        items[2].addActionListener((e) -> {
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
            contentPanel.add(spinner, BorderLayout.SOUTH);
            if (JOptionPane.showConfirmDialog(parent, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                addN.accept((Integer)spinner.getValue());
        });

        // Remove single copy item
        items[3] = new JMenuItem("Remove Single Copy");
        items[3].addActionListener((e) -> removeN.accept(1));

        // Remove all item
        items[4] = new JMenuItem("Remove All Copies");
        items[4].addActionListener((e) -> removeN.accept(Integer.MAX_VALUE));

        // Remove variable item
        items[5] = new JMenuItem("Remove Copies...");
        items[5].addActionListener((e) -> {
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(new JLabel("Copies to remove:"), BorderLayout.WEST);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
            contentPanel.add(spinner, BorderLayout.SOUTH);
            if (JOptionPane.showConfirmDialog(parent, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                removeN.accept((Integer)spinner.getValue());
        });
    }

    /**
     * @return the menu item for adding a single copy of a card.
     */
    public JMenuItem addSingle()
    {
        return items[0];
    }

    /**
     * @return the menu item for filling out a playset of cards.
     */
    public JMenuItem fillPlayset()
    {
        return items[1];
    }

    /**
     * @return the menu item for adding a specified number of copies of a card.
     */
    public JMenuItem addN()
    {
        return items[2];
    }

    /**
     * @return the menu item for removing a single copy of a card.
     */
    public JMenuItem removeSingle()
    {
        return items[3];
    }

    /**
     * @return the menu item for removing all copies of a card.
     */
    public JMenuItem removeAll()
    {
        return items[4];
    }

    /**
     * @return the menu item for removing a specified number of copies of a card.
     */
    public JMenuItem removeN()
    {
        return items[5];
    }

    /**
     * Convenience method for adding items that add cards to decks to menus.
     * 
     * @param menu container to add items to
     */
    public void addAddItems(Container menu)
    {
        menu.add(addSingle());
        menu.add(fillPlayset());
        menu.add(addN());
    }

    /**
     * Convenience method for adding items that remove cards from  decks to menus.
     * 
     * @param menu container to add items to
     */
    public void addRemoveItems(Container menu)
    {
        menu.add(removeSingle());
        menu.add(removeAll());
        menu.add(removeN());
    }
}
