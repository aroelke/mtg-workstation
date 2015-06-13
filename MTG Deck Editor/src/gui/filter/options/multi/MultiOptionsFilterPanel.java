package gui.filter.options.multi;

import gui.filter.FilterContainer;
import gui.filter.options.OptionsFilterPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import util.Containment;
import database.Card;

/**
 * This class represents a FilterPanel that filters Cards by a characteristic that has multiple
 * values among several options.
 * 
 * @author Alec
 *
 * @param <T> Type of the characteristic that will be filtered
 */
@SuppressWarnings("serial")
public class MultiOptionsFilterPanel<T extends CharSequence> extends OptionsFilterPanel
{
	/**
	 * Function representing the characteristic to be filtered.
	 */
	private Function<Card, List<T>> param;
	/**
	 * Combo box indicating containment.
	 */
	private JComboBox<Containment> contain;
	/**
	 * Box showing the options to choose from.
	 */
	protected JList<T> optionsBox;
	/**
	 * Code for determining what type of filter this is from a String.
	 * @see gui.filter.FilterPanel#setContent(String)
	 */
	private String code;
	/**
	 * Options that can be chosen from.
	 */
	private T[] options;
	
	/**
	 * Create a new MultiOptionsFilterPanel.
	 * 
	 * @param o List of options to be chosen from
	 * @param f Card characteristic to filter
	 * @param c This FilterPanel's code.
	 */
	public MultiOptionsFilterPanel(T[] o, Function<Card, List<T>> f, String c)
	{
		super();
		
		param = f;
		code = c;
		options = o;
		
		// Use a GridBagLayout to push everything up against the panel's left side
		GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] {FilterContainer.ROW_HEIGHT, FilterContainer.ROW_HEIGHT, FilterContainer.ROW_HEIGHT, FilterContainer.ROW_HEIGHT, FilterContainer.ROW_HEIGHT};
		layout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0};
		layout.columnWidths = new int[] {0, 0};
		layout.columnWeights = new double[] {0.0, 1.0};
		setLayout(layout);
		
		// Set containment combo box
		contain = new JComboBox<Containment>(Containment.values());
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
	 * @return A <code>Predicate<Card></code> that returns true if the Card's values
	 * for are characteristic is among those selected with the appropriate containment
	 * type, and <code>false</code> otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> contain.getItemAt(contain.getSelectedIndex()).test(param.apply(c), optionsBox.getSelectedValuesList());
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
	public void setContent(String content)
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
