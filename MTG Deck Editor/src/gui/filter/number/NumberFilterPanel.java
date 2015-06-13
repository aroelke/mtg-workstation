package gui.filter.number;

import gui.filter.CardFilter;
import gui.filter.FilterPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.Comparison;
import database.Card;

/**
 * This class represents a FilterPanel that filters numerical characteristics of
 * Cards.  Ranges can be accomplished using two of them.  If numbers have non-
 * numerical characters in them (like "1a" or "7-*"), the numbers are extracted
 * from those Strings. 
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class NumberFilterPanel extends FilterPanel
{	
	/**
	 * Characteristic of the Card to be filtered.
	 */
	private Function<Card, String> operand;
	/**
	 * Numerical comparison to make with the operand.
	 */
	private JComboBox<Comparison> comparisonBox;
	/**
	 * Value to compare to the operand.
	 */
	private JSpinner spinner;
	/**
	 * Whether or not the value can vary.  Typically this only appears in the
	 * power and toughness characteristic of cards as a *.
	 */
	private JCheckBox variable;
	/**
	 * Characteristic that will be filtered, represented by a Function mapping
	 * Cards onto lists of MTGColors.
	 */
	private String code;
	
	public NumberFilterPanel(Function<Card, String> op, boolean canVary, String c)
	{
		super();
		
		// Use a GridBagLayout to push the components against the left side of the
		// panel
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0, 50, 0};
		layout.rowHeights = new int[] {0};
		layout.columnWeights = new double[] {0.0, 0.0, 1.0};
		layout.rowWeights = new double[] {1.0};
		setLayout(layout);
		
		// Combo box for choosing the type of comparison to make
		comparisonBox = new JComboBox<Comparison>(Comparison.values());
		GridBagConstraints boxConstraints = new GridBagConstraints();
		boxConstraints.fill = GridBagConstraints.VERTICAL;
		boxConstraints.anchor = GridBagConstraints.WEST;
		add(comparisonBox, boxConstraints);
		
		// Value to compare the characteristic against
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0.0, 0.0, null, 1.0));
		GridBagConstraints spinnerConstraints = new GridBagConstraints();
		spinnerConstraints.fill = GridBagConstraints.BOTH;
		spinnerConstraints.anchor = GridBagConstraints.WEST;
		add(spinner, spinnerConstraints);
		
		// Check box for selecting variable values.  If this is selected, then
		// a numerical comparison is not made and instead the filter will filter
		// cards whose characteristic contains a *
		variable = new JCheckBox("Contains *");
		variable.addActionListener((e) -> {spinner.setEnabled(!variable.isSelected()); comparisonBox.setEnabled(!variable.isSelected());});
		variable.setVisible(canVary);
		GridBagConstraints checkConstraints = new GridBagConstraints();
		checkConstraints.fill = GridBagConstraints.VERTICAL;
		checkConstraints.anchor = GridBagConstraints.WEST;
		add(variable, checkConstraints);
		
		operand = op;
		code = c;
	}
	
	/**
	 * @param includeEmpty Include empty (or missing) values of the characteristic
	 * @return A <code>Predicate<Card></code> that represents the filter of this
	 * NumberFilterPanel's characteristic.  If empty values should be included,
	 * then they are treated as 0.
	 */
	@Override
	public CardFilter getFilter()
	{
		Predicate<Card> f;
		if (variable.isSelected())
			f = (c) -> operand.apply(c).contains("*");
		else
			f = (c) -> !operand.apply(c).isEmpty() && (comparisonBox.getItemAt(comparisonBox.getSelectedIndex())).test(Card.numericValueOf(operand.apply(c)), (Double)spinner.getValue());
		return new CardFilter(f, toString());
	}

	/**
	 * @return <code>false</code> because this FilterPanel's value is controlled by
	 * a spinner, which always has a value in it.
	 */
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	/**
	 * @return A String representation of this NumberFilterPanel, which is its code
	 * followed by either a * or the comparison to make and the number to compare to.
	 */
	@Override
	public String repr()
	{
		return code + ":" + (variable.isSelected() ? "*" : comparisonBox.getSelectedItem().toString() + spinner.getValue());
	}

	/**
	 * Set the value of the comparison combo box and the spinner or the variable check box
	 * based on the contents of the input String.
	 * 
	 * @param content String to parse for content.
	 */
	@Override
	public void setContent(String content)
	{
		if (variable.isVisible() && content.equals("*"))
			variable.setSelected(true);
		else
		{
			comparisonBox.setSelectedItem(Comparison.get(content.charAt(0)));
			spinner.setValue(Double.valueOf(content.substring(1)));
		}
	}
}
