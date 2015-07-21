package gui.filter.editor.options.multi;

import gui.filter.FilterType;
import database.Card;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SupertypeFilterPanel extends MultiOptionsFilterPanel<String>
{
	public SupertypeFilterPanel()
	{
		super(Card.supertypeList, (c) -> c.supertypes, FilterType.SUPERTYPE.code);
	}
}
