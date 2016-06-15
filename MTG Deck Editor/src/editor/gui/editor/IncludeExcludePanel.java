package editor.gui.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;

import com.jidesoft.swing.TristateCheckBox;

import editor.collection.category.CategorySpec;
import editor.database.Card;
import editor.gui.generic.ScrollablePanel;

/**
 * This class represents a panel that displays a list of categories with boxes next to them indicating
 * card inclusion in each of them.  A check mark means all cards it was given are included, a "mixed" icon
 * indicates some of the cards are included, and an empty box indicates none are included.  Boxes can be
 * selected to toggle inclusion/exclusion of cards (mixed cannot be selected manually).  Card inclusion is
 * not changed by the panel, but it provides information that allows it to be externally.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class IncludeExcludePanel extends ScrollablePanel
{
	/**
	 * Maximum amount of rows to display in a scroll pane.
	 */
	private static final int MAX_PREFERRED_ROWS = 10;
	
	/**
	 * Categories and their corresponding check boxes.
	 */
	private Map<CategorySpec, TristateCheckBox> categoryBoxes;
	/**
	 * List of Cards to display inclusion for.
	 */
	private Collection<Card> cards;
	/**
	 * Preferred viewport height of this panel.
	 */
	private int preferredViewportHeight;
	
	/**
	 * Create a new IncludeExcludePanel showing inclusion of the given cards in the given
	 * categories.
	 * 
	 * @param categories Categories to display
	 * @param c Cards to show inclusion for
	 */
	public IncludeExcludePanel(List<CategorySpec> categories, Collection<Card> c)
	{
		super(TRACK_WIDTH);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);
		categoryBoxes = new HashMap<CategorySpec, TristateCheckBox>();
		cards = c;
		preferredViewportHeight = 0;
		
		for (CategorySpec category: categories)
		{
			TristateCheckBox categoryBox = new TristateCheckBox(category.getName());
			long matches = cards.stream().filter(category::includes).count();
			if (matches == 0)
				categoryBox.setSelected(false);
			else if (matches < cards.size())
				categoryBox.setMixed(true);
			else
				categoryBox.setSelected(true);
			categoryBox.addActionListener((e) -> {
				if (categoryBox.isMixed())
					categoryBox.setSelected(false);
			});
			categoryBox.setBackground(Color.WHITE);
			add(categoryBox);
			categoryBoxes.put(category, categoryBox);
			preferredViewportHeight = Math.min(preferredViewportHeight + categoryBox.getPreferredSize().height, categoryBox.getPreferredSize().height*MAX_PREFERRED_ROWS);
		}
	}
	
	/**
	 * Create a new IncludeExcludePanel for a single Card.
	 * 
	 * @param categories Categories to show inclusion for
	 * @param card Card to show inclusion for
	 */
	public IncludeExcludePanel(List<CategorySpec> categories, Card card)
	{
		this(categories, Arrays.asList(card));
	}
	
	/**
	 * @return A Map containing the cards that were selected for inclusion and the new categories
	 * they were included in.
	 */
	public Map<Card, Set<CategorySpec>> getIncluded()
	{
		Map<Card, Set<CategorySpec>> included = new HashMap<Card, Set<CategorySpec>>();
		for (Card card: cards)
			for (CategorySpec category: categoryBoxes.keySet())
				if (categoryBoxes.get(category).getState() == TristateCheckBox.STATE_SELECTED && !category.includes(card))
					included.compute(card, (k, v) -> {
						if (v == null)
							v = new HashSet<CategorySpec>();
						v.add(category);
						return v;
					});
		return included;
	}
	
	/**
	 * @return A Map containing the cards that were deselected for exclusion and the categories
	 * they should be excluded from.
	 */
	public Map<Card, Set<CategorySpec>> getExcluded()
	{
		Map<Card, Set<CategorySpec>> excluded = new HashMap<Card, Set<CategorySpec>>();
		for (Card card: cards)
			for (CategorySpec category: categoryBoxes.keySet())
				if (categoryBoxes.get(category).getState() == TristateCheckBox.STATE_UNSELECTED && category.includes(card))
					excluded.compute(card, (k, v) -> {
						if (v == null)
							v = new HashSet<CategorySpec>();
						v.add(category);
						return v;
					});
		return excluded;
	}

	/**
	 * @return The preferred viewport size of this IncludeExcludePanel, which is the size of
	 * its contents up to MAX_PREFERRED_ROWS rows.
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		if (categoryBoxes.isEmpty())
			return getPreferredSize();
		else
		{
			Dimension size = getPreferredSize();
			size.height = preferredViewportHeight;
			return size;
		}
	}
}
