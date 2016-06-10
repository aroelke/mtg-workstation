package editor.gui.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
	
	private List<TristateCheckBox> categoryBoxes;
	private Collection<CategorySpec> checked;
	private Collection<CategorySpec> unchecked;
	
	public IncludeExcludePanel(List<CategorySpec> categories, List<Card> cards)
	{
		super(TRACK_WIDTH);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);
		categoryBoxes = new ArrayList<TristateCheckBox>();
		checked = new ArrayList<CategorySpec>();
		unchecked = new ArrayList<CategorySpec>();
		
		for (CategorySpec category: categories)
		{
			TristateCheckBox categoryBox = new TristateCheckBox(category.getName());
			int matches = (int)cards.stream().filter(category.getFilter()).count();
			if (matches == 0)
			{
				categoryBox.setSelected(false);
				unchecked.add(category);
			}
			else if (matches < cards.size())
				categoryBox.setMixed(true);
			else
			{
				categoryBox.setSelected(true);
				checked.add(category);
			}
			categoryBox.addActionListener((e) -> {
				if (categoryBox.isMixed())
					categoryBox.setSelected(false);
				if (categoryBox.isSelected())
				{
					checked.add(category);
					unchecked.remove(category);
				}
				else
				{
					checked.remove(category);
					unchecked.add(category);
				}
			});
			categoryBox.setBackground(Color.WHITE);
			add(categoryBox);
			categoryBoxes.add(categoryBox);
		}
	}
	
	public Collection<CategorySpec> getChecked()
	{
		return checked;
	}
	
	public Collection<CategorySpec> getUnchecked()
	{
		return unchecked;
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
			size.height = categoryBoxes.subList(0, Math.min(categoryBoxes.size(), MAX_PREFERRED_ROWS)).stream().mapToInt((c) -> c.getPreferredSize().height).sum();
			return size;
		}
	}
}
