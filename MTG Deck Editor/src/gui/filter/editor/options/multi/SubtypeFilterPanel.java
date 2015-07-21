package gui.filter.editor.options.multi;

import gui.filter.FilterType;
import database.Card;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SubtypeFilterPanel extends MultiOptionsFilterPanel<String>
{
	public SubtypeFilterPanel()
	{
		super(Card.subtypeList, (c) -> c.subtypes, FilterType.SUBTYPE.code);
	}
}
