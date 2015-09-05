package gui.filter.editor.options.multi;

import gui.filter.FilterType;
import database.Card;

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
