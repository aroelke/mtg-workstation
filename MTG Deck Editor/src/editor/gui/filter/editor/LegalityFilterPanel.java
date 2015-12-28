package editor.gui.filter.editor;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;
import editor.filter.leaf.options.multi.LegalityFilter;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by format legality and whether they are restricted or not.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LegalityFilterPanel extends OptionsFilterPanel<String>
{
	/**
	 * Create a new LegalityFilterPanel using the given LegalityFilter
	 * to initialize its fields.
	 * 
	 * @param f Filter to use for initialization
	 * @return The created LegalityFilterPanel
	 */
	public static LegalityFilterPanel create(LegalityFilter f)
	{
		LegalityFilterPanel panel = new LegalityFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	/**
	 * Check box indicating whether or not restricted cards should
	 * be filtered.
	 */
	private JCheckBox restrictedBox;

	/**
	 * Create a new LegalityFilterPanel.
	 */
	private LegalityFilterPanel()
	{
		super(FilterType.FORMAT_LEGALITY, Card.formatList);
		add(restrictedBox = new JCheckBox("Restricted"), BorderLayout.EAST);
	}
	
	/**
	 * @return The filter this LegalityFilter is editing.
	 */
	@Override
	public Filter filter()
	{
		LegalityFilter filter = (LegalityFilter)super.filter();
		filter.restricted = restrictedBox.isSelected();
		return filter;
	}
	
	/**
	 * If the given filter is a LegalityFilter, set the fields of this
	 * LegalityFilterPanel to its contents.
	 * 
	 * @param filter Filter to use for setting contents
	 * @throws IllegalArgumentException if the given filter is not a LegalityFilter
	 */
	@Override
	public void setContents(OptionsFilter<String> filter)
	{
		if (filter.type != FilterType.FORMAT_LEGALITY)
			throw new IllegalArgumentException("Illegal legality filter type " + filter.type.name());
		else
			setContents((LegalityFilter)filter);
	}
	
	/**
	 * If the given filter is a LegalityFilter, set the fields of this
	 * LegalityFilterPanel to its contents.
	 * 
	 * @param filter Filter to use for setting contents
	 * @throws IllegalArgumentException if the given filter is not a LegalityFilter
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof LegalityFilter)
			setContents((LegalityFilter)filter);
		else
			throw new IllegalArgumentException("Illegal legality filter " + filter.type.name());
			
	}
	
	/**
	 * Set the fields of this LegalityFilterPanel to the given
	 * LegalityFilter's contents
	 * 
	 * @param filter Filter to use for setting contents
	 */
	public void setContents(LegalityFilter filter)
	{
		super.setContents(filter);
		restrictedBox.setSelected(filter.restricted);
	}
}
