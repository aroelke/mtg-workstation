package gui.filter.colors;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters a Card's color identity, which is
 * its colors plus the colors of any symbols that appear in its text box.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ColorIdentityFilterPanel extends ColorFilterPanel
{
	/**
	 * Create a new ColorIdentityFilterPanel.
	 */
	public ColorIdentityFilterPanel()
	{
		super((c) -> c.colorIdentity, FilterType.COLOR_IDENTITY.code);
	}
}