package gui.filter.editor.options.multi;

import gui.filter.FilterType;
import database.Card;

/**
 * This class represents a panel that can filter cards by their supertype.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SupertypeFilterPanel extends MultiOptionsFilterPanel<String>
{
	/**
	 * Create a new SupertypeFilterPanel.
	 */
	public SupertypeFilterPanel()
	{
		super(FilterType.SUPERTYPE, Card.supertypeList, (c) -> c.supertypes(), FilterType.SUPERTYPE.code);
	}
}
