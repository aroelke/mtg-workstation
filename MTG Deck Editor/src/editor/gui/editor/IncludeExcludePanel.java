package editor.gui.editor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.jidesoft.swing.TristateCheckBox;

import editor.collection.category.CategorySpec;
import editor.database.Card;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class IncludeExcludePanel extends JPanel
{
	private List<TristateCheckBox> categoryBoxes;
	private Collection<CategorySpec> checked;
	private Collection<CategorySpec> unchecked;
	
	public IncludeExcludePanel(List<CategorySpec> categories, List<Card> cards)
	{
		super();
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
}
