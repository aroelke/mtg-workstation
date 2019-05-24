package editor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import editor.database.card.Card;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TristateCheckBox;
import editor.gui.generic.TristateCheckBox.State;
import editor.gui.settings.SettingsBuilder;
import editor.gui.settings.SettingsDialog;
import editor.util.MouseListenerFactory;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that displays a list of card tags with check boxes
 * next to them.  A check mark means all cards it was given have a tag, a "mixed"
 * icon means that some of them do, and an empty box means none of them do.  Boxes can
 * be selected to modify tags.  Tags are not actually modified by the panel, but
 * it provides functions indicating what has been modified so that other objects
 * can.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardTagPanel extends ScrollablePanel
{
    /**
     * Maximum amount of rows to display in a scroll pane.
     */
    private static final int MAX_PREFERRED_ROWS = 10;

    /**
     * Show a dialog allowing editing of the tags of the given cards and adding
     * new tags.
     *
     * @param cards cards whose tags should be edited
     */
    public static void editTags(List<Card> cards, Component parent)
    {
        JPanel contentPanel = new JPanel(new BorderLayout());
        CardTagPanel cardTagPanel = new CardTagPanel(cards);
        contentPanel.add(new JScrollPane(cardTagPanel), BorderLayout.CENTER);
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
        JTextField newTagField = new JTextField();
        lowerPanel.add(newTagField);
        JButton newTagButton = new JButton("Add");

        ActionListener addListener = (e) -> {
            if (!newTagField.getText().isEmpty())
            {
                if (cardTagPanel.addTag(newTagField.getText()))
                {
                    newTagField.setText("");
                    cardTagPanel.revalidate();
                    cardTagPanel.repaint();
                    SwingUtilities.getWindowAncestor(cardTagPanel).pack();
                }
            }
        };
        newTagButton.addActionListener(addListener);
        newTagField.addActionListener(addListener);
        lowerPanel.add(newTagButton);
        contentPanel.add(lowerPanel, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(parent, contentPanel, "Edit Card Tags", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
        {
            var tags = new HashMap<>(SettingsDialog.settings().inventory.tags.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> new HashSet<>(e.getValue()))));

            for (var entry : cardTagPanel.getTagged().entrySet())
                tags.compute(entry.getKey().multiverseid().get(0), (k, v) -> {
                    if (v == null)
                        v = new HashSet<>();
                    v.addAll(entry.getValue());
                    return v;
                });
            for (var entry : cardTagPanel.getUntagged().entrySet())
                tags.compute(entry.getKey().multiverseid().get(0), (k, v) -> {
                    if (v != null)
                    {
                        v.removeAll(entry.getValue());
                        if (v.isEmpty())
                            v = null;
                    }
                    return v;
                });
            SettingsDialog.applySettings(new SettingsBuilder(SettingsDialog.settings()).inventoryTags(tags).build());
        }
    }

    /**
     * Cards whose tags are to be modified.
     */
    private Collection<Card> cards;
    /**
     * Preferred viewport height of this panel.
     */
    private int preferredViewportHeight;
    /**
     * Boxes corresponding to tags.
     */
    private List<TristateCheckBox> tagBoxes;

    /**
     * Create a new CardTagPanel for editing the tags of the given collection
     * of cards.
     *
     * @param coll collection containing cards whose tags should be edited
     */
    public CardTagPanel(Collection<Card> coll)
    {
        super(TRACK_WIDTH);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        tagBoxes = new ArrayList<>();
        cards = coll;
        preferredViewportHeight = 0;

        setTags(SettingsDialog.settings().inventory.tags().stream().sorted().collect(Collectors.toList()));

        var tags = SettingsDialog.settings().inventory.tags.entrySet().stream().collect(Collectors.toMap((e) -> MainFrame.inventory().get(e.getKey()), Map.Entry::getValue));
        for (TristateCheckBox tagBox : tagBoxes)
        {
            long matches = cards.stream().filter((c) -> tags.get(c) != null && tags.get(c).contains(tagBox.getText())).count();
            if (matches == 0)
                tagBox.setState(State.UNSELECTED);
            else if (matches < cards.size())
                tagBox.setState(State.INDETERMINATE);
            else
                tagBox.setState(State.SELECTED);
        }
    }

    /**
     * Add a new tag to the list
     *
     * @param tag new tag to add
     * @return true if the tag was added, and false otherwise.
     */
    public boolean addTag(String tag)
    {
        var tags = tagBoxes.stream().map(TristateCheckBox::getText).collect(Collectors.toSet());
        if (tags.add(tag))
        {
            setTags(tags.stream().sorted().collect(Collectors.toList()));
            return true;
        }
        else
            return false;
    }

    /**
     * {@inheritDoc}
     * The maximum size of this CardTagPanel is MAX_PREFERRED_ROWS rows.
     */
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        if (tagBoxes.isEmpty())
            return getPreferredSize();
        else
        {
            Dimension size = getPreferredSize();
            size.height = preferredViewportHeight;
            return size;
        }
    }

    /**
     * Get the cards that were tagged.
     *
     * @return a map of cards onto the sets tags that were added to them.
     */
    public Map<Card, Set<String>> getTagged()
    {
        var tagged = new HashMap<Card, Set<String>>();
        for (Card card : cards)
            for (TristateCheckBox tagBox : tagBoxes)
                if (tagBox.getState() == State.SELECTED)
                    tagged.compute(card, (k, v) -> {
                        if (v == null)
                            v = new HashSet<>();
                        v.add(tagBox.getText());
                        return v;
                    });
        return tagged;
    }

    /**
     * Get the cards that had tags removed.
     *
     * @return a map of cards onto the sets of tags that were removed from them.
     */
    public Map<Card, Set<String>> getUntagged()
    {
        var untagged = new HashMap<Card, Set<String>>();
        for (Card card : cards)
            for (TristateCheckBox tagBox : tagBoxes)
                if (tagBox.getState() == State.UNSELECTED)
                    untagged.compute(card, (k, v) -> {
                        if (v == null)
                            v = new HashSet<>();
                        v.add(tagBox.getText());
                        return v;
                    });
        return untagged;
    }

    /**
     * Remove a tag from the list.
     *
     * @param tag tag to remove
     * @return true if the tag was removed, and false otherwise.
     */
    public boolean removeTag(String tag)
    {
        var tags = tagBoxes.stream().map(TristateCheckBox::getText).collect(Collectors.toSet());
        if (tags.remove(tag))
        {
            setTags(tags.stream().sorted().collect(Collectors.toList()));
            if (getParent() != null)
            {
                getParent().revalidate();
                getParent().repaint();
            }
            if (SwingUtilities.getWindowAncestor(this) != null)
                SwingUtilities.getWindowAncestor(this).pack();
            return true;
        }
        else
            return false;
    }

    /**
     * Refresh the tags displayed with the given list of tags.
     *
     * @param tags list of tags that should be displayed
     */
    private void setTags(List<String> tags)
    {
        tagBoxes = tagBoxes.stream().filter((t) -> tags.contains(t.getText())).collect(Collectors.toList());
        removeAll();
        preferredViewportHeight = 0;
        if (tags.isEmpty())
        {
            JLabel emptyLabel = new JLabel("<html><i>No tags have been created.</i></html>");
            add(emptyLabel);
        }
        else
        {
            for (String tag : tags)
            {
                JPanel tagPanel = new JPanel();
                tagPanel.setBackground(Color.WHITE);
                tagPanel.setLayout(new BorderLayout());

                final TristateCheckBox tagBox = tagBoxes.stream().filter((t) -> t.getText().equals(tag)).findFirst().orElse(new TristateCheckBox(tag));
                tagBox.setBackground(Color.WHITE);
                tagPanel.add(tagBox, BorderLayout.WEST);
                tagBoxes.add(tagBox);

                JLabel deleteButton = new JLabel(String.valueOf(UnicodeSymbols.MINUS) + " ");
                deleteButton.setForeground(Color.RED);
                deleteButton.addMouseListener(MouseListenerFactory.createPressListener((e) -> removeTag(tag)));
                tagPanel.add(deleteButton, BorderLayout.EAST);

                preferredViewportHeight = Math.min(preferredViewportHeight + tagPanel.getPreferredSize().height, tagPanel.getPreferredSize().height * MAX_PREFERRED_ROWS);
                add(tagPanel);
            }
        }
    }
}
