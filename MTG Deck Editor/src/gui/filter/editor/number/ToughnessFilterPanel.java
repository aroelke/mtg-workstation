package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.function.Predicate;

import javax.swing.JCheckBox;

import database.Card;

/**
 * This class represents a FilterPanel that filters Cards by toughness.  If the
 * Card does not have a toughness value, it is filtered out.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ToughnessFilterPanel extends NumberFilterPanel
{
	private JCheckBox variable;
	
	/**
	 * Create a new ToughnessFilterPanel.
	 */
	public ToughnessFilterPanel()
	{
		super((c) -> c.toughness.value, true, FilterType.TOUGHNESS.code);
		
		// Check box for selecting variable values.  If this is selected, then
		// a numerical comparison is not made and instead the filter will filter
		// cards whose characteristic contains a *
		variable = new JCheckBox("Contains *");
		variable.addActionListener((e) -> {spinner.setEnabled(!variable.isSelected()); comparisonBox.setEnabled(!variable.isSelected());});
		add(variable);
	}
	
	/**
	 * @return The <code>Predicate<Card></code> representing this panel's filter, which is either the same
	 * as NumberFilterPanel's filter or filters out cards that don't have * in their toughness values.  It will
	 * also filter out cards that don't have power.
	 * @see NumberFilterPanel#getFilter()
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		Predicate<Card> hasToughness = (c) -> !c.toughness.expression.isEmpty();
		return hasToughness.and(variable.isSelected() ? (c) -> c.toughness.variable() : super.getFilter());
	}
	
	/**
	 * @return a String representation of this PowerFilterPanel, which is the same as NumberFilterPanel's
	 * String representation unless variable toughness values are filtered, in which case it is the code
	 * followed by *.
	 * @see NumberFilterPanel#toString()
	 */
	@Override
	public String toString()
	{
		return variable.isSelected() ? FilterType.TOUGHNESS.code + ":*" : super.toString();
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
