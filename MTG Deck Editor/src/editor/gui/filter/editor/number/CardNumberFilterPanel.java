package editor.gui.filter.editor.number;

import editor.gui.filter.FilterType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class represents a FilterPanel that filters cards by collector's
 * number.  If the number is missing (has a "--" value), then it is treated
 * as 0.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardNumberFilterPanel extends NumberFilterPanel
{
	/**
	 * Create a new CardNumberFilterPanel.
	 */
	public CardNumberFilterPanel()
	{
		super(FilterType.CARD_NUMBER, (c) -> Arrays.stream(c.number()).map((v) -> Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""))).collect(Collectors.toList()));
	}
}
