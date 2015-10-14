package gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import database.Card;
import database.CategorySpec;
import gui.ColorButton;
import gui.filter.FilterGroupPanel;

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
	 * @param s TODO: Fill this out
	 * @return The panel in the state it was last in while editing it, or <code>null</code> if the Cancel button
	 * was pressed or the dialog was closed.
	 */
	public static CategoryEditorPanel showCategoryEditor(CategorySpec s)
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
		return showCategoryEditor(null);
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
	 * Button displaying the color of the category, and allowing change of that color.
	 */
	private ColorButton colorButton;
	/**
	 * TODO: Comment this
	 */
	private CategorySpec spec;
	
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
		namePanel.add(Box.createHorizontalStrut(5));
		namePanel.add(colorButton = new ColorButton());
		colorButton.addActionListener((e) -> {
			colorButton.setColor(JColorChooser.showDialog(null, "Choose a Color", colorButton.color()));
			colorButton.repaint();
		});
		add(namePanel, BorderLayout.NORTH);
		
		add(filter = new FilterGroupPanel(), BorderLayout.CENTER);
	}
	
	/**
	 * Create a new CategoryEditorPanel, and then fill its contents from the specified
	 * category string.
	 * 
	 * @param s TODO: Fill this out
	 */
	public CategoryEditorPanel(CategorySpec s)
	{
		this();
		if (s != null)
		{
			spec = s;
			nameField.setText(spec.name);
			colorButton.setColor(spec.color);
			filter.setContents(spec.filterString);
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
	 * @return TODO: Comment this
	 */
	public Set<Card> whitelist()
	{
		return spec.whitelist;
	}
	
	/**
	 * TODO: Comment this
	 */
	public Set<Card> blacklist()
	{
		return spec.blacklist;
	}
	
	/**
	 * @return The color of the category.
	 */
	public Color color()
	{
		return colorButton.color();
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public CategorySpec spec()
	{
		return spec;
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
		spec.name = nameField.getText();
		spec.color = colorButton.color();
		spec.filterString = filter.toString();
		return spec.toString();
	}
}
