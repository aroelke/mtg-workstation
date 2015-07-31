package gui.filter.editor.text;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filter Cards by flavor text.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class FlavorFilterPanel extends TextFilterPanel
{
	/**
	 * Create a new FlavorFilterPanel.
	 */
	public FlavorFilterPanel()
	{
		// TODO: Correct the documentation of this function
		super((c) -> c.normalizedFlavor(), FilterType.FLAVOR_TEXT.code);
	}
}
