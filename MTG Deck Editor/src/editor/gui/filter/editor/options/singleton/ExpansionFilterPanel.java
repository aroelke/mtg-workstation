package editor.gui.filter.editor.options.singleton;

import editor.database.characteristics.Expansion;
import editor.gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters Cards by the name of the
 * Expansion they belong to.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ExpansionFilterPanel extends SingletonOptionsFilterPanel<Expansion>
{
	/**
	 * Create a new ExpansionFilterPanel.
	 */
	public ExpansionFilterPanel()
	{
		super(FilterType.EXPANSION, Expansion.expansions, (c) -> c.expansion());
	}
}
