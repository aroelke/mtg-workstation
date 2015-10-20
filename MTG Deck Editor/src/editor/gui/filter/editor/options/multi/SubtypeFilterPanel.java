package editor.gui.filter.editor.options.multi;

import editor.database.Card;
import editor.gui.filter.FilterType;

/**
 * This class represents a panel that can filter cards by their subtype.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SubtypeFilterPanel extends MultiOptionsFilterPanel<String>
{
	/**
	 * Create a new SubtypeFilterPanel.
	 */
	public SubtypeFilterPanel()
	{
		super(FilterType.SUBTYPE, Card.subtypeList, Card::subtypes);
	}
}
