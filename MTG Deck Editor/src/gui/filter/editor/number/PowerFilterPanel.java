package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.function.Predicate;

import javax.swing.JCheckBox;

import database.Card;

/**
 * This class represents a FilterPanel that filters Cards by power.  If the card
 * doesn't have a power value, simply including this filter will filter them out.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class PowerFilterPanel extends NumberFilterPanel
{
	private JCheckBox variable;
	
	/**
	 * Create a new PowerFilterPanel.
	 */
	public PowerFilterPanel()
	{
		super((c) -> c.power.value, true, FilterType.POWER.code);
		
		// Check box for selecting variable values.  If this is selected, then
		// a numerical comparison is not made and instead the filter will filter
		// cards whose characteristic contains a *
		variable = new JCheckBox("Contains *");
		variable.addActionListener((e) -> {spinner.setEnabled(!variable.isSelected()); comparisonBox.setEnabled(!variable.isSelected());});
		add(variable);
	}
	
	/**
	 * @return The <code>Predicate<Card></code> representing this panel's filter, which is either the same
	 * as NumberFilterPanel's filter or filters out cards that don't have * in their power values.  It will
	 * also filter out cards that don't have power.
	 * @see NumberFilterPanel#getFilter()
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		Predicate<Card> hasPower = (c) -> !c.power.expression.isEmpty();
		return hasPower.and(variable.isSelected() ? (c) -> c.power.variable() : super.getFilter());
	}
	
	/**
	 * @return a String representation of this PowerFilterPanel, which is the same as NumberFilterPanel's
	 * String representation unless variable power values are filtered, in which case it is the code
	 * followed by *.
	 * @see NumberFilterPanel#toString()
	 */
	@Override
	public String toString()
	{
		return variable.isSelected() ? FilterType.POWER.code + ":*" : super.toString();
	}
	
	/**
	 * Parse a String and set this panel's contents accordingly.  If the contents are "*,"
	 * then the "Contains *" box will be checked.  Otherwise, the drop-down and spinner
	 * will be populated according to the String.
	 * 
	 * @param content String to parse
	 * @see NumberFilterPanel#setContents(String)
	 */
	@Override
	public void setContents(String content)
	{
		if (content.contains("*"))
		{
			variable.setSelected(true);
			spinner.setEnabled(false);
			comparisonBox.setEnabled(false);
		}
		else
			super.setContents(content);
	}
}
