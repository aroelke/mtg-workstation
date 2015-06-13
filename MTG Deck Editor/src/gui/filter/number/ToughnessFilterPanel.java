package gui.filter.number;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters Cards by toughness.  If the
 * Card does not have a toughness value, it is filtered out.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ToughnessFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new ToughnessFilterPanel.
	 */
	public ToughnessFilterPanel()
	{
		super((c) -> c.toughness, true, FilterType.TOUGHNESS.code);
	}
}
