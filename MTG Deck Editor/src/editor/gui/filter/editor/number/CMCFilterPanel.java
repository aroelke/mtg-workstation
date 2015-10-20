package editor.gui.filter.editor.number;

import java.util.stream.Collectors;

import editor.database.characteristics.ManaCost;
import editor.gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters cards by converted mana cost.
 * All cards have converted mana costs, so empty values don't exist.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CMCFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new CMCFilterPanel.
	 */
	public CMCFilterPanel()
	{
		super(FilterType.CMC, (c) -> c.mana().stream().map(ManaCost::cmc).collect(Collectors.toList()));
	}
}
