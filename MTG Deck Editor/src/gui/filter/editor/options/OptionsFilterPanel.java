package gui.filter.editor.options;

import gui.filter.FilterType;
import gui.filter.editor.FilterEditorPanel;

/**
 * This class represents a FilterPanel that presents a set of options
 * to the user to choose from to fill out the filter.
 * 
 * TODO: Extract out common elements from each kind of options filter panel.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class OptionsFilterPanel extends FilterEditorPanel
{
	/**
	 * Maximum number of rows to show in the list pane.
	 */
	public static final int MAX_ROWS = 7;
	
	/**
	 * Create a new OptionsFilterPanel.
	 * 
	 * @param type Type of filter the new OptionsFilterPanel edits
	 */
	public OptionsFilterPanel(FilterType type)
	{
		super(type);
	}
}
