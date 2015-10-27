package editor.gui.editor;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import editor.database.CategorySpec;
import editor.gui.ColorButton;
import editor.gui.filter.FilterGroupPanel;

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
				if (editor.nameField.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Category must have a name.", "Error", JOptionPane.ERROR_MESSAGE);
				else if (editor.nameField.getText().contains(String.valueOf(FilterGroupPanel.BEGIN_GROUP)))
					JOptionPane.showMessageDialog(null, "Category names cannot contain the character '" + FilterGroupPanel.BEGIN_GROUP + "'.", "Error", JOptionPane.ERROR_MESSAGE);
				else
				{
					editor.updateSpec();
					return editor;
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
	 * The category specification being edited by this CategoryEditorPanel.
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
		
		spec = new CategorySpec(nameField.getText(), colorButton.color(), filter.filter(), filter.toString());
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
	 * Update this CategoryEditorPanel's specification to match its contents.
	 */
	public void updateSpec()
	{
		spec.name = nameField.getText();
		spec.color = colorButton.color();
		spec.filter = filter.filter();
		spec.filterString = filter.toString();
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
