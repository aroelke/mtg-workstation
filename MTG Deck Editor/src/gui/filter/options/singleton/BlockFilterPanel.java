package gui.filter.options.singleton;

import gui.filter.FilterType;
import database.characteristics.Expansion;

/**
 * This class represents a FilterPanel that filters Cards by the block they
 * belong to.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class BlockFilterPanel extends SingletonOptionsFilterPanel<String>
{
	/**
	 * Create a new BlockFilterPanel.
	 */
	public BlockFilterPanel()
	{
		super(Expansion.blocks, (c) -> c.set.block, FilterType.BLOCK.code);
	}
}
