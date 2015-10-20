package editor.gui.filter.editor.options.singleton;

import editor.database.characteristics.Expansion;
import editor.gui.filter.FilterType;

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
		super(FilterType.BLOCK, Expansion.blocks, (c) -> c.expansion().block);
	}
}
