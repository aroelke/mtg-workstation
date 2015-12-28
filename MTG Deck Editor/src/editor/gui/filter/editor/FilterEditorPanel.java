package editor.gui.filter.editor;

import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.FilterPanel;

/**
 * This class represents a panel that can edit a single type of filter.
 * 
 * @author Alec Roelke
 *
 * @param <F> Type of filter this panel can edit
 */
@SuppressWarnings("serial")
public abstract class FilterEditorPanel<F extends FilterLeaf<?>> extends FilterPanel<F>
{
	/**
	 * Set the fields of this FilterEditorPanel based on the contents of
	 * the given filter.
	 * 
	 * @param filter Filter to use for initialization
	 */
	public abstract void setContents(FilterLeaf<?> filter);
}
