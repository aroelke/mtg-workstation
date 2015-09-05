package gui.filter.editor;

import java.awt.GridLayout;
import java.util.function.Predicate;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import database.Card;
import gui.filter.FilterType;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EmptyFilterPanel extends FilterEditorPanel
{
	public EmptyFilterPanel()
	{
		super(FilterType.NONE);
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel("This clause will not match any cards.");
		add(label);
	}
	
	@Override
	public void setContents(String content)
	{}

	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> false;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	protected String repr()
	{
		return "";
	}
}
