package editor.gui.filter.editor.options.multi;

import editor.database.Card;
import editor.gui.filter.FilterType;

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
		super(FilterType.SUPERTYPE, Card.supertypeList, Card::supertypes);
	}
}
