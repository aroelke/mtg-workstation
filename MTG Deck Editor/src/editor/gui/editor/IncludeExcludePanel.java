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
 * TODO: Comment this
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class IncludeExcludePanel extends ScrollablePanel
{
	private static final int MAX_PREFERRED_ROWS = 10;
	
	private Map<CategorySpec, TristateCheckBox> categoryBoxes;
	private Collection<Card> cards;
	private int preferredViewportHeight;
	
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
	
	public IncludeExcludePanel(List<CategorySpec> categories, Card card)
	{
		this(categories, Arrays.asList(card));
	}

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
