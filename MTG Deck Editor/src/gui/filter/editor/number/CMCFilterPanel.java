package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.Arrays;

import database.ManaCost;

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
		super((c) -> Arrays.stream(c.mana()).mapToDouble(ManaCost::cmc).toArray(), FilterType.CMC.code);
	}
}
