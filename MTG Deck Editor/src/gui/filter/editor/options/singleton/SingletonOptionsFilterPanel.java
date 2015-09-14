package gui.filter.editor.options.singleton;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import database.Card;
import gui.filter.FilterType;
import gui.filter.FilterTypePanel;
import gui.filter.editor.options.OptionsFilterPanel;
import util.Containment;

/**
 * This class represents a FilterPanel that filters Cards by a characteristic that has one value
 * among several options.
 * 
 * @author Alec
 *
 * @param <T> Type of the characteristic that will be filtered
 */
@SuppressWarnings("serial")
public class SingletonOptionsFilterPanel<T> extends OptionsFilterPanel
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
	 * Options that can be chosen from.
	 */
	private T[] options;
	
	/**
	 * Create a new SingletonOptionsFilterPanel.
	 * 
	 * @param type Type of filter the new SingletonOptionsFilterPanel edits
	 * @param o List of options to be chosen from
	 * @param f Card characteristic to filter
	 */
	public SingletonOptionsFilterPanel(FilterType type, T[] o, Function<Card, T> f)
	{
		super(type);
		
		param = f;
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
	 * @return A String representation of this SingletonOptionsFilterPanel's contents, which
	 * is its containment type followed by the list of selected options surrounded by braces.
	 */
	@Override
	protected String repr()
	{
		return contain.getSelectedItem().toString() + "{" + String.join(",", optionsBox.getSelectedValuesList().stream().map(String::valueOf).collect(Collectors.toList())) + "}";
	}

	/**
	 * Automatically selects values in the list from the given String.
	 * 
	 * @param content String to parse for values.
	 */
	@Override
	public void setContents(String content)
	{
		int index = content.indexOf('{');
		contain.setSelectedItem(Containment.get(content.substring(0, index)));
		String[] selectedOptions = content.substring(index + 1, content.length() - 1).split(",");
		for (String o: selectedOptions)
		{
			for (int i = 0; i < options.length; i++)
				if (o.equalsIgnoreCase(options[i].toString()))
					optionsBox.addSelectionInterval(i, i);
		}
		optionsBox.ensureIndexIsVisible(optionsBox.getSelectedIndex());
	}
}
