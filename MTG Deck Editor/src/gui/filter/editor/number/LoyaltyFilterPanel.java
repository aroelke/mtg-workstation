package gui.filter.editor.number;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters cards by loyalty.  If a
 * card doesn't have a loyalty value, simply including this filter will filter
 * them out.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LoyaltyFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new LoyaltyFilterPanel.
	 */
	public LoyaltyFilterPanel()
	{
		super((c) -> String.valueOf(c.loyalty), false, FilterType.LOYALTY.code);
	}
}
