package gui.filter.editor.number;

import gui.filter.FilterType;

import java.util.stream.Collectors;

import database.characteristics.ManaCost;

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
		super((c) -> c.mana().stream().map(ManaCost::cmc).collect(Collectors.toList()), FilterType.CMC.code);
	}
}
