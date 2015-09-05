package gui.filter.editor.options.singleton;

import gui.filter.FilterType;
import database.characteristics.Rarity;

/**
 * This class represents a FilterPanel that filters cards based on their rarities.
 * 
 * @author Alec Roelke
 * @see database.characteristics.Rarity
 */
@SuppressWarnings("serial")
public class RarityFilterPanel extends SingletonOptionsFilterPanel<Rarity>
{
	/**
	 * Create a new RarityFilterPanel.
	 */
	public RarityFilterPanel()
	{
		super(FilterType.RARITY, Rarity.values(), (c) -> c.rarity(), FilterType.RARITY.code);
	}
}
