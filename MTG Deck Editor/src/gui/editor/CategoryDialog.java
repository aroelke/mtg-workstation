package gui.editor;

import gui.filter.FilterContainer;
import gui.filter.FilterDialog;
import gui.filter.FilterType;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

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
	 * Panel containing the content of this CategoryDialog.
	 */
	private JPanel contentPanel;
	/**
	 * List of panels that each filter a single characteristic of a Card.
	 */
	private List<FilterContainer> filters;
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
		contentPanel = new JPanel();
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
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new TitledBorder("Filter"));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		// Panel containing buttons
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Panel containing the button to add a filter to the category
		JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(addPanel);
		
		// Button for adding new filters to the category
		JButton addButton = new JButton("Add");
		addButton.addActionListener((e) -> addFilterPanel());
		addPanel.add(addButton);
		
		// Panel containing buttons to close the CategoryDialog
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(closePanel);
		
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
		
		// Initialize the list of filter panels and create the first one
		filters = new ArrayList<FilterContainer>();
		reset();
		
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
	 * Add a new filter panel.
	 */
	@Override
	public FilterContainer addFilterPanel()
	{
		FilterContainer filter = new FilterContainer(this);
		filters.add(filter);
		contentPanel.add(filter);
		pack();
		return filter;
	}
	
	/**
	 * Remove an existing filter panel.
	 * 
	 * @param panel Filter panel to remove
	 * @return <code>true</code> if the panel was successfully removed and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean removeFilterPanel(FilterContainer panel)
	{
		if (filters.size() > 1 && filters.contains(panel))
		{
			filters.remove(panel);
			contentPanel.remove(panel);
			filters.get(0).alwaysAnd(true);
			pack();
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Clear the list of filter panels and remove them all from the form.
	 */
	public void reset()
	{
		filters.clear();
		contentPanel.removeAll();
		addFilterPanel();
		filters.get(0).alwaysAnd(true);
		nameField.setText("");
	}
	
	/**
	 * Parse a String for filters and create the appropriate filter panels and set their values,
	 * but don't show the dialog.  Follow this call with createNewCategory() or editCategory() to
	 * actually open the dialog.
	 * 
	 * The string should begin with the category's name, and then pairs of "AND" or "OR" and the filter's
	 * string surrounded by <>.  The name should also be surrounded by <> if it has spaces.
	 * 
	 * @param s String to parse
	 */
	public void initializeFromString(String s)
	{
		// Split the String by white space, but ignore white space between <>
		filters.clear();
		contentPanel.removeAll();
		Matcher m = Pattern.compile("<([^>]*)>|[^\\s]+").matcher(s);
		List<String> filterStrings = new ArrayList<String>();
		while (m.find())
		{
			if (m.group(1) != null)
				filterStrings.add(m.group(1));
			else
				filterStrings.add(m.group());
		}
		
		// The first string is the filter's name.
		nameField.setText(filterStrings.remove(0));
		
		if (filterStrings.size()%2 != 0 || filterStrings.size() == 0)
		{
			reset();
			throw new IllegalArgumentException("Illegal category string \"" + s + "\"");
		}
		else
		{
			for (int i = 0; i < filterStrings.size(); i += 2)
			{
				FilterContainer newPanel = addFilterPanel();
		
				// Figure out if the filter should be ANDed or ORed
				String mode = filterStrings.get(i);
				if (!mode.equalsIgnoreCase("AND") && !mode.equalsIgnoreCase("OR"))
				{
					reset();
					throw new IllegalArgumentException("Illegal composition mode \"" + mode + "\"");
				}
				newPanel.setAnd(mode.equals("AND"));
				
				// Get the new filter panel's contents from the string
				// See the filter types to see how this works
				String[] filterString = filterStrings.get(i + 1).split(":", 2);
				newPanel.setContents(FilterType.fromCode(filterString[0]), filterString[1]);
			}
			filters.get(0).alwaysAnd(true);
		}
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
		Predicate<Card> composedFilter = (c) -> true;
		for (FilterContainer filter: filters)
		{
			if (filter.isAnd())
				composedFilter = composedFilter.and(filter.getFilter());
			else
				composedFilter = composedFilter.or(filter.getFilter());
		}
		return composedFilter;
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
		StringBuilder str = new StringBuilder();
		str.append("<").append(nameField.getText()).append("> ");
		for (FilterContainer f: filters)
			str.append(f.isAnd() ? "AND " : "OR ").append("<").append(f.getFilter().repr()).append("> ");
		return str.toString().trim();
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
