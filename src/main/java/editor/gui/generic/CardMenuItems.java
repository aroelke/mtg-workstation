package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
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

import editor.database.card.Card;
import editor.gui.editor.EditorFrame;
import editor.gui.settings.SettingsDialog;

/**
 * This class represents a list of menu items for manipulating cards in a deck. There are six:
 * add a single copy, fill a playset of copies, add some number of copies, remove one copy,
 * remove all copies, and remove some number of copies.
 *
 * @author Alec Roelke
 */
public class CardMenuItems implements Iterable<JMenuItem>
{
    /**
     * Array containing the menu items for manipulating card copies.
     */
    private final JMenuItem[] items;

    /**
     * Create a new list of items for manipulating card copies using the given functions to do the
     * manipulation.
     *
     * @param monitor supplier for getting the editor to make changes to
     * @param cards supplier for determining which cards to make changes with
     * @param main whether or not this CardMenuItems should operate on the main deck
     */
    public CardMenuItems(final Supplier<Optional<EditorFrame>> monitor, final Supplier<? extends Collection<Card>> cards, final boolean main)
    {
        final Function<EditorFrame, Optional<Integer>> id = (f) -> main ? Optional.of(EditorFrame.MAIN_DECK) : f.getSelectedExtraID();
        final IntConsumer addN = (i) -> monitor.get().ifPresent((f) -> id.apply(f).ifPresent((n) -> f.addCards(n, cards.get(), i)));
        final Runnable fillPlayset = () -> monitor.get().ifPresent((f) -> id.apply(f).ifPresent((n) -> {
            f.modifyCards(n, cards.get().stream().collect(Collectors.toMap(Function.identity(), (c) -> {
                if (f.hasCard(n, c))
                    return Math.max(0, SettingsDialog.PLAYSET_SIZE() - f.getList(n).getEntry(c).count());
                else
                    return SettingsDialog.PLAYSET_SIZE();
            })));
        }));
        final IntConsumer removeN = (i) -> {
            monitor.get().ifPresent((f) -> id.apply(f).ifPresent((n) -> f.removeCards(n, cards.get(), i)));
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
            if (JOptionPane.showConfirmDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
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
            if (JOptionPane.showConfirmDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                removeN.accept((Integer)spinner.getValue());
        });
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
     * @return the menu item for adding a specified number of copies of a card.
     */
    public JMenuItem addN()
    {
        return items[2];
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

    @Override
    public Iterator<JMenuItem> iterator()
    {
        return Arrays.asList(items).iterator();
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
     * @return the menu item for removing a single copy of a card.
     */
    public JMenuItem removeSingle()
    {
        return items[3];
    }

    /**
     * Convenience method for enabling or disabling all menu items at once.
     * 
     * @param en whether items should be enabled or not
     */
    public void setEnabled(boolean en)
    {
        for (JMenuItem item : items)
            item.setEnabled(en);
    }

    /**
     * Convenience method for controlling all items' visibilities.
     * 
     * @param visible whether items should be visible or not
     */
    public void setVisible(boolean visible)
    {
        for (JMenuItem item : items)
            item.setVisible(visible);
    }
}
