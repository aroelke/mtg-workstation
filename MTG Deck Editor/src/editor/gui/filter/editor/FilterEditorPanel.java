package editor.gui.filter.editor;

import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.FilterPanel;

@SuppressWarnings("serial")
public abstract class FilterEditorPanel<F extends FilterLeaf<?>> extends FilterPanel<F>
{
	public abstract void setContents(FilterLeaf<?> filter);
}
