package gui.filter.editor.text;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters Cards by name.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class NameFilterPanel extends TextFilterPanel
{
	/**
	 * Create a new NameFilterPanel.
	 */
	public NameFilterPanel()
	{
		super(FilterType.NAME, (c) -> c.normalizedName(), FilterType.NAME.code);
	}
}
