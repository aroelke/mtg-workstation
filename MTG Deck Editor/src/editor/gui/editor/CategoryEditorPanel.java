package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import editor.collection.category.CategorySpec;
import editor.database.card.Card;
import editor.filter.Filter;
import editor.gui.CardList;
import editor.gui.filter.FilterGroupPanel;
import editor.gui.generic.ColorButton;
import editor.gui.generic.ScrollablePanel;

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
	 * Maximum height that the category panel should attain before scrolling.
	 */
	public static final int MAX_HEIGHT = 500;
	
	/**
	 * Show a dialog allowing the editing of categories.  If the OK button is pressed, return the panel as it was
	 * edited.
	 * 
	 * @param parent Component to be used to determine the Frame of the dialog
	 * @param s Specification for the initial contents of the editor
	 * @return The CategorySpec of the panel in the state it was last in while editing it, or <code>null</code> if
	 * the Cancel button was pressed or the dialog was closed.
	 */
	public static CategorySpec showCategoryEditor(Container parent, CategorySpec s)
	{
		CategoryEditorPanel editor = new CategoryEditorPanel(s);
		editor.filter.addChangeListener((e) -> SwingUtilities.getWindowAncestor((Component)e.getSource()).pack());
		ScrollablePanel editorPanel = new ScrollablePanel(new BorderLayout(), ScrollablePanel.TRACK_WIDTH)
		{
			@Override
			public Dimension getPreferredScrollableViewportSize()
			{
				Dimension size = editor.getPreferredSize();
				size.height = Math.min(MAX_HEIGHT, size.height);
				return size;
			}
		};
		editorPanel.add(editor, BorderLayout.CENTER);
		while (true)
		{
			JScrollPane editorPane = new JScrollPane(editorPanel);
			editorPane.setBorder(new EmptyBorder(0, 0, 0, 0));
			if (JOptionPane.showOptionDialog(parent, editorPane, "Category Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
			{
				if (editor.nameField.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Category must have a name.", "Error", JOptionPane.ERROR_MESSAGE);
				else if (editor.nameField.getText().contains(String.valueOf(Filter.BEGIN_GROUP)))
					JOptionPane.showMessageDialog(null, "Category names cannot contain the character '" + Filter.BEGIN_GROUP + "'.", "Error", JOptionPane.ERROR_MESSAGE);
				else
				{
					editor.updateSpec();
					return editor.spec;
				}
			}
			else
				return null;
		}
	}
	
	/**
	 * Show a dialog allowing the editing of categories.  If the OK button is pressed, return the panel as it was
	 * edited.  The panel will start off blank.
	 * 
	 * @param parent Component to be used to determine the Frame of the dialog
	 * @return The CategorySpec of the panel in the state it was last in while editing it, or <code>null</code> if
	 * the Cancel button was pressed or the dialog was closed.
	 */
	public static CategorySpec showCategoryEditor(Container parent)
	{
		return showCategoryEditor(parent, null);
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
	 * The category specification being edited by this CategoryEditorPanel.
	 */
	private CategorySpec spec;
	/**
	 * List displaying the category's whitelist.
	 */
	private CardList whitelist;
	/**
	 * List displaying the category's blacklist.
	 */
	private CardList blacklist;
	
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
			Color newColor = JColorChooser.showDialog(null, "Choose a Color", colorButton.color());
			if (newColor != null)
			{
				colorButton.setColor(newColor);
				colorButton.repaint();
			}
		});
		add(namePanel, BorderLayout.NORTH);
		
		add(filter = new FilterGroupPanel(), BorderLayout.CENTER);
		
		JPanel listPanel = new JPanel(new GridLayout(0, 2));
		JPanel whitelistPanel = new JPanel(new BorderLayout());
		whitelistPanel.setBorder(new TitledBorder("Whitelist"));
		whitelistPanel.add(new JScrollPane(whitelist = new CardList()), BorderLayout.CENTER);
		listPanel.add(whitelistPanel);
		JPanel blacklistPanel = new JPanel(new BorderLayout());
		blacklistPanel.setBorder(new TitledBorder("Blacklist"));
		blacklistPanel.add(new JScrollPane(blacklist = new CardList()), BorderLayout.CENTER);
		listPanel.add(blacklistPanel);
		add(listPanel, BorderLayout.SOUTH);
		
		spec = new CategorySpec(nameField.getText(), colorButton.color(), filter.filter());
	}
	
	/**
	 * Create a new CategoryEditorPanel, and then fill its contents from the specified
	 * category specification.
	 * 
	 * @param s Specifications for the initial state of the editor
	 */
	public CategoryEditorPanel(CategorySpec s)
	{
		this();
		if (s != null)
		{
			spec = new CategorySpec(s);
			nameField.setText(spec.getName());
			colorButton.setColor(spec.getColor());
			filter.setContents(spec.getFilter());
			whitelist.setCards(spec.getWhitelist().stream().sorted(Card::compareName).collect(Collectors.toList()));
			blacklist.setCards(spec.getBlacklist().stream().sorted(Card::compareName).collect(Collectors.toList()));
		}
	}
	
	/**
	 * Update this CategoryEditorPanel's specification to match its contents.
	 */
	public void updateSpec()
	{
		spec.setName(nameField.getText());
		spec.setColor(colorButton.color());
		spec.setFilter(filter.filter());
	}
	
	/**
	 * @return The category specification being edited by this CategoryEditorPanel.
	 * Make sure to call {@link CategoryEditorPanel#updateSpec()} in order to get its
	 * contents.
	 */
	public CategorySpec spec()
	{
		return spec;
	}
	
	/**
	 * @return The String representation of the category being edited, which is its name
	 * followed by the String representation of its filter.
	 */
	@Override
	public String toString()
	{
		return spec.toString();
	}
}
