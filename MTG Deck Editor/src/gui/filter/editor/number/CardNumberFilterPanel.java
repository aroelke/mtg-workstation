package gui.filter.editor.number;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters cards by collector's
 * number.  If the number is missing (has a "--" value), then it is treated
 * as 0.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardNumberFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new CardNumberFilterPanel.
	 */
	public CardNumberFilterPanel()
	{
		super((c) -> Double.valueOf(c.number.replace("--", "0")), false, FilterType.CARD_NUMBER.code);
	}
}
