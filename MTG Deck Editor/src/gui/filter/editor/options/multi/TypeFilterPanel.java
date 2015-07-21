package gui.filter.editor.options.multi;

import gui.filter.FilterType;
import database.Card;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TypeFilterPanel extends MultiOptionsFilterPanel<String>
{
	public TypeFilterPanel()
	{
		super(Card.typeList, (c) -> c.types, FilterType.TYPE.code);
	}
}
