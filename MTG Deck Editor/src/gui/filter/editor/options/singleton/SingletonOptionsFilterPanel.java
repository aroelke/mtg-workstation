package gui.filter.editor.options.singleton;

import gui.filter.FilterTypePanel;
import gui.filter.editor.options.OptionsFilterPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import util.Containment;
import database.Card;

/**
 * This class represents a FilterPanel that filters Cards by a characteristic that has one value
 * among several options.
 * 
 * TODO: Devise a method to allow clearing the list
 * 
 * @author Alec
 *
 * @param <T> Type of the characteristic that will be filtered
 */
@SuppressWarnings("serial")
public class SingletonOptionsFilterPanel<T extends CharSequence> extends OptionsFilterPanel
{
	/**
	 * Function representing the characteristic to be filtered.
	 */
	private Function<Card, T> param;
	/**
	 * Combo box indicating containment.  This box contains a more restricted set of
	 * containments, including only "contains any of" and "contains none of".
	 */
	private JComboBox<Containment> contain;
	/**
	 * Box showing the options to choose from.
	 */
	private JList<T> optionsBox;
	/**
	 * Code for determining what type of filter this is from a String.
	 * @see gui.filter.editor.FilterEditorPanel#setContents(String)
	 */
	private String code;
	/**
	 * Options that can be chosen from.
	 */
	private T[] options;
	
	/**
	 * Create a new SingletonOptionsFilterPanel.
	 * 
	 * @param o List of options to be chosen from
	 * @param f Card characteristic to filter
	 * @param c This FilterPanel's code.
	 */
	public SingletonOptionsFilterPanel(T[] o, Function<Card, T> f, String c)
	{
		super();
		
		param = f;
		code = c;
		options = o;
		
		// Use a GridBagLayout to push everything up against the panel's left side
		GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] {FilterTypePanel.ROW_HEIGHT, FilterTypePanel.ROW_HEIGHT, FilterTypePanel.ROW_HEIGHT, FilterTypePanel.ROW_HEIGHT, FilterTypePanel.ROW_HEIGHT};
		layout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0};
		layout.columnWidths = new int[] {0, 0};
		layout.columnWeights = new double[] {0.0, 1.0};
		setLayout(layout);
		
		// Set containment combo box (only has "any of" and "none of")
		contain = new JComboBox<Containment>(Containment.singletonValues());
		GridBagConstraints comparisonConstraints = new GridBagConstraints();
		comparisonConstraints.gridx = 0;
		comparisonConstraints.gridy = 2;
		comparisonConstraints.fill = GridBagConstraints.BOTH;
		add(contain, comparisonConstraints);
		
		// List pane showing the available options
		optionsBox = new JList<T>(options);
		optionsBox.setVisibleRowCount(-1);
		GridBagConstraints optionsConstraints = new GridBagConstraints();
		optionsConstraints.gridx = 1;
		optionsConstraints.gridy = 0;
		optionsConstraints.gridheight = 5;
		optionsConstraints.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(optionsBox), optionsConstraints);
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns true if the Card's value
	 * for the characteristic is among those selected, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> ((Containment)contain.getSelectedItem()).test(optionsBox.getSelectedValuesList(), Arrays.asList(param.apply(c)));
	}

	/**
	 * @return <code>true</code> if this SingletonOptionsFilterPanel has nothing in it selected,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return optionsBox.getSelectedIndices().length == 0;
	}
	
	/**
	 * @return A String representation of this SingletonOptionsFilterPanel, which is its code
	 * followed by the list of selected values separated by commas in {}.
	 */
	@Override
	public String toString()
	{
		return code + ":{" + String.join(",", optionsBox.getSelectedValuesList()) + "}";
	}

	/**
	 * Automatically selects values in the list from the given String.
	 * 
	 * @param content String to parse for values.
	 */
	@Override
	public void setContents(String content)
	{
		String[] selectedOptions = content.substring(1, content.length() - 1).split(",");
		for (String o: selectedOptions)
		{
			for (int i = 0; i < options.length; i++)
				if (o.equalsIgnoreCase(options[i].toString()))
					optionsBox.addSelectionInterval(i, i);
		}
	}
}
