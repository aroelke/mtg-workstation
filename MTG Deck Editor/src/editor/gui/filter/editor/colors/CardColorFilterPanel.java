package editor.gui.filter.editor.colors;

import editor.gui.filter.FilterType;

/**
 * This class represnts a FilterPanel that filters the colors of a Card.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardColorFilterPanel extends ColorFilterPanel
{
	/**
	 * Create anew CardColorFilterPanel.
	 */
	public CardColorFilterPanel()
	{
		super(FilterType.COLOR, (c) -> c.colors());
	}
}