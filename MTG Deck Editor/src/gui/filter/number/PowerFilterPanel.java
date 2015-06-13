package gui.filter.number;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters Cards by power.  If the card
 * doesn't have a power value, simply including this filter will filter them out.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class PowerFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new PowerFilterPanel.
	 */
	public PowerFilterPanel()
	{
		super((c) -> c.power, true, FilterType.POWER.code);
	}
}
