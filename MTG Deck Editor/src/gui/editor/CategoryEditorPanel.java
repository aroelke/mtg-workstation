package gui.editor;

import gui.filter.FilterGroupPanel;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	 * Show a dialog allowing the editing of categories.  If the OK button is pressed, return the panel as it was
	 * edited.
	 * 
	 * @param s String to initialize the panel as
	 * @return The panel in the state it was last in while editing it, or <code>null</code> if the Cancel button
	 * was pressed or the dialog was closed.
	 */
	public static CategoryEditorPanel showCategoryEditor(String s)
	{
		CategoryEditorPanel editor = new CategoryEditorPanel(s);
		while (true)
		{
			if (JOptionPane.showOptionDialog(null, editor, "Category Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
			{
				if (editor.name().isEmpty())
					JOptionPane.showMessageDialog(null, "Category must have a name.", "Error", JOptionPane.ERROR_MESSAGE);
				else if (editor.name().contains(String.valueOf(FilterGroupPanel.BEGIN_GROUP)))
					JOptionPane.showMessageDialog(null, "Category names cannot contain the character '" + FilterGroupPanel.BEGIN_GROUP + "'.", "Error", JOptionPane.ERROR_MESSAGE);
				else
					return editor;
			}
			else
				return null;
		}
	}
	
	/**
	 * Show a dialog allowing the editing of categories.  If the OK button is pressed, return the panel as it was
	 * edited.  The panel will start off blank.
	 * 
	 * @return The panel in the state it was last in while editing it, or <code>null</code> if the Cancel button
	 * was pressed or the dialog was closed.
	 */
	public static CategoryEditorPanel showCategoryEditor()
	{
		return showCategoryEditor("");
	}
	
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
		if (!s.isEmpty())
		{
			Matcher m = Deck.CATEGORY_PATTERN.matcher(s);
			if (m.matches())
			{
				nameField.setText(m.group(1));
				if (!m.group(2).isEmpty())
					for (String c: m.group(2).split(Deck.EXCEPTION_SEPARATOR))
						whitelist.add(c);
				if (!m.group(3).isEmpty())
					for (String c: m.group(3).split(Deck.EXCEPTION_SEPARATOR))
						blacklist.add(c);
				filter.setContents(m.group(5));
			}
			else
				throw new IllegalArgumentException("Illegal category string \"" + s + "\"");
		}
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
	
	/**
	 * @return The String representation of the category being edited, which is its name
	 * followed by the String representation of its filter.  Note the difference with
	 * Deck.Category's String representation, which also includes a whitelist and blacklist
	 * while this does not (because they cannot be edited using this panel).
	 */
	@Override
	public String toString()
	{
		StringJoiner white = new StringJoiner(Deck.EXCEPTION_SEPARATOR, String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
		for (String c: whitelist)
			white.add(c);
		StringJoiner black = new StringJoiner(Deck.EXCEPTION_SEPARATOR, String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
		for (String c: blacklist)
			black.add(c);
		return FilterGroupPanel.BEGIN_GROUP + nameField.getText() + FilterGroupPanel.END_GROUP + " " + white + " " + black + " " + FilterGroupPanel.BEGIN_GROUP + FilterGroupPanel.END_GROUP + " " + filter.toString();
	}
}
