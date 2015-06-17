package gui.editor;

import gui.filter.FilterDialog;
import gui.filter.FilterGroup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Predicate;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import database.Card;
import database.Deck;

/**
 * This class represents a dialog for creating a new category for a decklist or editing an existing
 * one.  It has a field for the category's name as well as a panel for customizing the filter for
 * the category's view of cards.  Each category must have a unique name.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryDialog extends FilterDialog
{
	/**
	 * TODO: Comment this
	 */
	private FilterGroup filter;
	/**
	 * Text field for the category's name.
	 */
	private JTextField nameField;
	/**
	 * Master list of cards in the deck.
	 */
	private Deck cards;
	/**
	 * CategoryPanel that will either be returned or be edited.
	 */
	private CategoryPanel category;
	
	/**
	 * Create a new CategoryDialog.
	 * 
	 * @param owner Owner Component of this CategoryDialog
	 * @param list Master list of Cards in the deck
	 */
	public CategoryDialog(Component owner, Deck list)
	{
		super(SwingUtilities.windowForComponent(owner), "Advanced Filter");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		cards = list;
		category = null;
		
		// Initialize the content pane
		getContentPane().setLayout(new BorderLayout());
		
		// Name editing field
		JPanel namePanel = new JPanel();
		namePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.add(new JLabel("Filter Name:"));
		nameField = new JTextField();
		namePanel.add(nameField);
		getContentPane().add(namePanel, BorderLayout.NORTH);
		
		// Panel for editing filters
		getContentPane().add(filter = new FilterGroup(this), BorderLayout.CENTER);
		
		// Panel containing buttons to close the CategoryDialog
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(closePanel, BorderLayout.SOUTH);
		
		// OK button for accepting changes and creating/updating a category
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new OKListener());
		closePanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		// Button for rejecting changes and not creating a category or updating an existing
		// one
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {category = null; reset(); setVisible(false); dispose();});
		closePanel.add(cancelButton);
		
		pack();
		
		// When the window closes, reset the filters and dispose of it and make it invisible
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				category = null;
				reset();
				setVisible(false);
				dispose();
			}
		});
	}
	
	/**
	 * Reset this CategoryDialog to its initial state, with only one filter
	 * set to a name filter.
	 */
	@Override
	public void reset()
	{
		getContentPane().remove(filter);
		getContentPane().add(filter = new FilterGroup(this), BorderLayout.CENTER);
		nameField.setText("");
		pack();
	}
	
	/**
	 * Parse a String for filters and create the appropriate filter panels and set their values,
	 * but don't show the dialog.  Follow this call with createNewCategory() or editCategory() to
	 * actually open the dialog.
	 * 
	 * The string should begin with the category's name, and then inside <> either AND or OR followed
	 * by filters which are also surrounded by <>.  They may also start with AND or OR which are
	 * followed by filters.
	 * 
	 * @param s String to parse
	 */
	public void initializeFromString(String s)
	{
		String[] contents = s.split("\\s+", 2);
		nameField.setText(contents[0]);
		filter.setContents(contents[1]);
	}
	
	/**
	 * @return The name of the category to be created.
	 */
	public String name()
	{
		return nameField.getText();
	}
	
	/**
	 * @return The composite filter of all the panels.
	 */
	public Predicate<Card> filter()
	{
		return filter.getFilter();
	}
	
	/**
	 * Show the filter dialog and wait until a filter is customized, then return a new category
	 * from it.  See the OK button action for details on how the category is created.
	 * 
	 * @return The panel that should be displayed for the new category, or <code>null</code>
	 * if the window was closed or the action cancelled.
	 */
	public CategoryPanel createNewCategory()
	{
		category = null;
		setVisible(true);
		return category;
	}
	
	/**
	 * Show the dialog and wait for the category to be customized, and then edit the given category.
	 * See the OK button action for details on how the category is edited.
	 * 
	 * @param toEdit Category to edit.
	 * @return <code>true</code> if the category was edited, and <code>false</code> if the
	 * dialog was closed or the action cancelled (or if an error ocurred).
	 */
	public boolean editCategory(CategoryPanel toEdit)
	{
		if (!cards.containsCategory(toEdit.name()))
		{
			JOptionPane.showMessageDialog(null, "Decklist must contain category \"" + toEdit.name() + "\".", "Error", JOptionPane.ERROR_MESSAGE);
			reset();
			return false;
		}
		
		category = toEdit;
		initializeFromString(toEdit.repr());
		setVisible(true);
		return category != null;
	}
	
	/**
	 * @return A String representation of this CategoryDialog.
	 */
	@Override
	public String toString()
	{
		return name() + " " + filter.toString();
	}
	
	/**
	 * This class represents the action that must be taken when the OK button is pressed.  That action is
	 * to close the dialog and return the category that was created or edit the category to edit.
	 * 
	 * @author Alec Roelke
	 */
	private class OKListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (nameField.getText().isEmpty())
				JOptionPane.showMessageDialog(null, "Categories must have names.", "Error", JOptionPane.ERROR_MESSAGE);
			else
			{
				try
				{
					if (category != null)
						category.edit(nameField.getText(), CategoryDialog.this.toString(), filter());
					else
						category = new CategoryPanel(nameField.getText(), CategoryDialog.this.toString(), cards, filter());
					setVisible(false);
					reset();
					dispose();
				}
				catch (Exception x)
				{
					JOptionPane.showMessageDialog(null, x.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}
