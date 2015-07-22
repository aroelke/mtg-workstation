package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.function.Predicate;

import database.Card;

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
		super((c) -> c.loyalty.isEmpty() ? 0.0 : Double.valueOf(c.loyalty), false, FilterType.LOYALTY.code);
	}
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		Predicate<Card> hasLoyalty = (c) -> !c.loyalty.isEmpty();
		return hasLoyalty.and(super.getFilter());
	}
}
