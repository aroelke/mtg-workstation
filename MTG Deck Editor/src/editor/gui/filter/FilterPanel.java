package editor.gui.filter;

import javax.swing.JPanel;

import editor.filter.Filter;

/**
 * This class represents a panel that corresponds to a filter but
 * allows the user to edit its contents.
 * 
 * @author Alec Roelke
 *
 * @param <F> Type of filter being edited
 */
@SuppressWarnings("serial")
public abstract class FilterPanel<F extends Filter> extends JPanel
{
	/**
	 * Group that this FilterPanel belongs to.
	 */
	protected FilterGroupPanel group;
	
	/**
	 * Create a new FilterPanel that belongs to no group.
	 */
	public FilterPanel()
	{
		super();
		group = null;
	}
	
	/**
	 * @return The filter currently being edited by this FilterPanel.
	 * If the panel was created from an already-existing filter, that
	 * filter will not reflect changes made in the panel!  This function
	 * returns a copy of that filter modified according to the fields.
	 */
	public abstract Filter filter();
	
	/**
	 * Set the contents of this FilterPanel.
	 * 
	 * @param filter Filter containing the information that should
	 * be displayed by this FilterPanel.
	 */
	public abstract void setContents(F filter);
}
