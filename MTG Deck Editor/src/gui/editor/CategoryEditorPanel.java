package gui.editor;

import gui.filter.FilterGroupPanel;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import database.Card;
import database.Deck;

/**
 * This class represents a panel that presents a name field and a filter field that
 * allows for editing the contents of a category.  It also has invisible blacklist
 * and whitelist fields that are only populated when parsing a category string.
 * This class is meant to be used and then thrown away.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryEditorPanel extends JPanel
{
	/**
	 * Text field for editing the category's name.
	 */
	private JTextField nameField;
	/**
	 * FilterGroupPanel for editing the category's filter.
	 */
	private FilterGroupPanel filter;
	/**
	 * Set of Strings containing the UIDs of the cards in the category's whitelist.  This
	 * will not be filled unless a category string is parsed.
	 */
	private Set<String> whitelist;
	/**
	 * Set of Strings containing the UIDs of the cards in the category's blacklist.  This
	 * will not be filled unless a category string is parsed.
	 */
	private Set<String> blacklist;
	
	/**
	 * Create a new CategoryEditorPanel.
	 */
	public CategoryEditorPanel()
	{
		super(new BorderLayout());
		
		JPanel namePanel = new JPanel();
		namePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.add(new JLabel("Category Name: "));
		namePanel.add(nameField = new JTextField());
		add(namePanel, BorderLayout.NORTH);
		
		add(filter = new FilterGroupPanel(), BorderLayout.CENTER);
		
		whitelist = new HashSet<String>();
		blacklist = new HashSet<String>();
	}
	
	/**
	 * Create a new CategoryEditorPanel, and then fill its contents from the specified
	 * category string.
	 * 
	 * @param s
	 */
	public CategoryEditorPanel(String s)
	{
		this();
		Matcher m = Deck.CATEGORY_PATTERN.matcher(s);
		if (m.matches())
		{
			nameField.setText(m.group(1));
			whitelist.addAll(Arrays.asList(m.group(2).split(Deck.EXCEPTION_SEPARATOR)));
			blacklist.addAll(Arrays.asList(m.group(3).split(Deck.EXCEPTION_SEPARATOR)));
			filter.setContents(m.group(4));
		}
		else
			throw new IllegalArgumentException("Illegal category string \"" + s + "\"");
	}
	
	/**
	 * @return The name of the category being edited.
	 */
	public String name()
	{
		return nameField.getText();
	}
	
	/**
	 * @return The <code>Predicate<Card></code> representing the filter of the category
	 * being edited.
	 */
	public Predicate<Card> filter()
	{
		return filter.filter();
	}
	
	/**
	 * @return The String representation of the filter for the category being edited.
	 */
	public String repr()
	{
		return filter.toString();
	}
	
	/**
	 * @return The list of UIDs of the cards that should be included in the category
	 * even if they don't pass through the filter.  This is not editable using this
	 * panel.
	 */
	public Set<String> whitelist()
	{
		return whitelist;
	}
	
	/**
	 * @return The list of UIDs of the cards that should be excluded from the category
	 * even if they do pass through the filter.  This is not editable using this
	 * panel.
	 */
	public Set<String> blacklist()
	{
		return blacklist;
	}
}
