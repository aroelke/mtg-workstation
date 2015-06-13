package gui.filter.number;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters cards by converted mana cost.
 * All cards have converted mana costs, so empty values don't exist.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CMCFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new CMCFilterPanel.
	 */
	public CMCFilterPanel()
	{
		super((c) -> String.valueOf(c.mana.cmc()), false, FilterType.CMC.code);
	}
}
