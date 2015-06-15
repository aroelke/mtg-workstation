package gui.filter;

import javax.swing.JPanel;

/**
 * TODO: Comment this class
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class FilterPanel extends JPanel
{
	private FilterGroup group;
	
	public FilterPanel(FilterGroup g)
	{
		super();
		setGroup(g);
	}
	
	public FilterGroup getGroup()
	{
		return group;
	}
	
	public void setGroup(FilterGroup g)
	{
		group = g;
	}
	
	public abstract CardFilter getFilter();
	
	public abstract void setContents(String contents);
	
	public abstract boolean isEmpty();
}
