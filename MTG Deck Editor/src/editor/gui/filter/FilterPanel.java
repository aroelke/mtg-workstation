package editor.gui.filter;

import javax.swing.JPanel;

import editor.filter.Filter;

@SuppressWarnings("serial")
public abstract class FilterPanel<F extends Filter> extends JPanel
{
	protected FilterGroupPanel group;
	
	public FilterPanel()
	{
		super();
		group = null;
	}
	
	public abstract Filter filter();
	
	public abstract void setContents(F filter);
}
