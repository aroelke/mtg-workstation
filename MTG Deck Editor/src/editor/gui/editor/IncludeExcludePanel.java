package editor.gui.editor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import editor.database.Card;

@SuppressWarnings("serial")
public class IncludeExcludePanel extends JPanel
{
	private List<JCheckBox> categoryBoxes;
	
	public IncludeExcludePanel(List<String> categoryNames, List<Card> cards)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);
		categoryBoxes = new ArrayList<JCheckBox>();
		for (String name: categoryNames)
		{
			JCheckBox categoryBox = new JCheckBox(name);
			categoryBox.setBackground(Color.WHITE);
			add(categoryBox);
			categoryBoxes.add(categoryBox);
		}
	}
	
	public IncludeExcludePanel(List<String> categoryNames, Card card)
	{
		this(categoryNames, Arrays.asList(card));
	}
}
