package editor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jidesoft.swing.TristateCheckBox;

import editor.database.card.Card;
import editor.gui.generic.ScrollablePanel;
import editor.util.MouseListenerFactory;

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
	 * Boxes corresponding to tags.
	 */
	private List<TristateCheckBox> tagBoxes;
	/**
	 * Cards whose tags are to be modified.
	 */
	private Collection<Card> cards;
	/**
	 * Preferred viewport height of this panel.
	 */
	private int preferredViewportHeight;
	
	/**
	 * Create a new CardTagPanel for editing the tags of the given collection
	 * of Cards.
	 * 
	 * @param coll Collection containing cards whose tags should be edited
	 */
	public CardTagPanel(Collection<Card> coll)
	{
		super(TRACK_WIDTH);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);
		tagBoxes = new ArrayList<TristateCheckBox>();
		cards = coll;
		preferredViewportHeight = 0;
		
		setTags(Card.tags().stream().sorted().collect(Collectors.toList()));
		
		for (TristateCheckBox tagBox: tagBoxes)
		{
			long matches = cards.stream().filter((c) -> Card.tags.get(c) != null && Card.tags.get(c).contains(tagBox.getText())).count();
			if (matches == 0)
				tagBox.setSelected(false);
			else if (matches < cards.size())
				tagBox.setMixed(true);
			else
				tagBox.setSelected(true);
		}
	}
	
	/**
	 * Refresh the tags displayed with the given list of tags.
	 * 
	 * @param tags List of tags that should be displayed
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
			for (String tag: tags)
			{
				JPanel tagPanel = new JPanel();
				tagPanel.setBackground(Color.WHITE);
				tagPanel.setLayout(new BorderLayout());
				
				final TristateCheckBox tagBox = tagBoxes.stream().filter((t) -> t.getText().equals(tag)).findFirst().orElse(new TristateCheckBox(tag));
				tagBox.addActionListener((e) -> {
					if (tagBox.isMixed())
						tagBox.setSelected(false);
				});
				tagBox.setBackground(Color.WHITE);
				tagPanel.add(tagBox, BorderLayout.WEST);
				tagBoxes.add(tagBox);
				
				JLabel deleteButton = new JLabel("× ");
				deleteButton.setForeground(Color.RED);
				deleteButton.addMouseListener(MouseListenerFactory.createPressListener((e) -> removeTag(tag)));
				tagPanel.add(deleteButton, BorderLayout.EAST);
				
				preferredViewportHeight = Math.min(preferredViewportHeight + tagPanel.getPreferredSize().height, tagPanel.getPreferredSize().height*MAX_PREFERRED_ROWS);
				add(tagPanel);
			}
		}
	}
	
	/**
	 * Add a new tag to the list
	 * 
	 * @param tag New tag to add
	 * @return <code>true</code> if the tag was added, and <code>false</code> otherwise.
	 */
	public boolean addTag(String tag)
	{
		Set<String> tags = tagBoxes.stream().map(TristateCheckBox::getText).collect(Collectors.toSet());
		if (tags.add(tag))
		{
			setTags(tags.stream().sorted().collect(Collectors.toList()));
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Remove a tag from the list.
	 * 
	 * @param tag Tag to remove
	 * @return <code>true</code> if the tag was removed, and <code>false</code> otherwise.
	 */
	public boolean removeTag(String tag)
	{
		Set<String> tags = tagBoxes.stream().map(TristateCheckBox::getText).collect(Collectors.toSet());
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
	 * @return A map of cards onto the sets tags that were added to them.
	 */
	public Map<Card, Set<String>> getTagged()
	{
		Map<Card, Set<String>> tagged = new HashMap<Card, Set<String>>();
		for (Card card: cards)
			for (TristateCheckBox tagBox: tagBoxes)
				if (tagBox.getState() == TristateCheckBox.STATE_SELECTED)
					tagged.compute(card, (k, v) -> {
						if (v == null)
							v = new HashSet<String>();
						v.add(tagBox.getText());
						return v;
					});
		return tagged;
	}
	
	/**
	 * @return A map of cards onto the sets of tags that were removed from them.
	 */
	public Map<Card, Set<String>> getUntagged()
	{
		Map<Card, Set<String>> untagged = new HashMap<Card, Set<String>>();
		for (Card card: cards)
			for (TristateCheckBox tagBox: tagBoxes)
				if (tagBox.getState() == TristateCheckBox.STATE_UNSELECTED)
					untagged.compute(card, (k, v) -> {
						if (v == null)
							v = new HashSet<String>();
						v.add(tagBox.getText());
						return v;
					});
		return untagged;
	}
	
	/**
	 * @return The preferred viewport size of this panel, which is the size of its contents
	 * up to MAX_PREFERRED_ROWS rows.
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
}
