package editor.gui.filter.editor.options.singleton;

import editor.database.Card;
import editor.database.characteristics.Rarity;
import editor.gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters cards based on their rarities.
 * 
 * @author Alec Roelke
 * @see editor.database.characteristics.Rarity
 */
@SuppressWarnings("serial")
public class RarityFilterPanel extends SingletonOptionsFilterPanel<Rarity>
{
	/**
	 * Create a new RarityFilterPanel.
	 */
	public RarityFilterPanel()
	{
		super(FilterType.RARITY, Rarity.values(), Card::rarity);
	}
}
