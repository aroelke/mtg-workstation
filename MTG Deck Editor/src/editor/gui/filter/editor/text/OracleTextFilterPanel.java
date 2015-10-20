package editor.gui.filter.editor.text;

import editor.gui.filter.FilterType;

/**
 * This class represents a FilterPanel that can filter Cards by their rules text.
 * This does not take printed text into account.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class OracleTextFilterPanel extends TextFilterPanel
{
	/**
	 * Create a new OracleTextFilterPanel.
	 */
	public OracleTextFilterPanel()
	{
		super(FilterType.RULES_TEXT, (c) -> c.normalizedText(), FilterType.RULES_TEXT.code);
	}
}
