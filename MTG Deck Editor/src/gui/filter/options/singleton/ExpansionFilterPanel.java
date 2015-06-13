package gui.filter.options.singleton;

import gui.filter.FilterType;
import database.characteristics.Expansion;

/**
 * This class represents a FilterPanel that filters Cards by the name of the
 * Expansion they belong to.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ExpansionFilterPanel extends SingletonOptionsFilterPanel<String>
{
	/**
	 * Create a new ExpansionFilterPanel.
	 */
	public ExpansionFilterPanel()
	{
		super(Expansion.expansions, (c) -> c.set.name, FilterType.EXPANSION.code);
	}
}
