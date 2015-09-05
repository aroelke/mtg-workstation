package gui.filter.editor.options.singleton;

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
		super(FilterType.EXPANSION, Expansion.expansions, (c) -> c.expansion().name, FilterType.EXPANSION.code);
	}
}
