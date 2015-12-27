package editor.gui.filter.editor;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;
import editor.filter.leaf.options.multi.LegalityFilter;

@SuppressWarnings("serial")
public class LegalityFilterPanel extends OptionsFilterPanel<String>
{
	public static LegalityFilterPanel create(LegalityFilter f)
	{
		LegalityFilterPanel panel = new LegalityFilterPanel(f);
		panel.setContents(f);
		return panel;
	}
	
	private JCheckBox restrictedBox;

	private LegalityFilterPanel(LegalityFilter f)
	{
		super(FilterType.FORMAT_LEGALITY, Card.formatList);
		add(restrictedBox = new JCheckBox("Restricted"), BorderLayout.EAST);
	}
	
	@Override
	public Filter filter()
	{
		LegalityFilter filter = (LegalityFilter)super.filter();
		filter.restricted = restrictedBox.isSelected();
		return filter;
	}
	
	@Override
	public void setContents(OptionsFilter<String> filter)
	{
		if (filter.type != FilterType.FORMAT_LEGALITY)
			throw new IllegalArgumentException("Illegal legality filter type " + filter.type.name());
		else
			setContents((LegalityFilter)filter);
	}
	
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof LegalityFilter)
			setContents((LegalityFilter)filter);
		else
			throw new IllegalArgumentException("Illegal legality filter " + filter.type.name());
			
	}
	
	public void setContents(LegalityFilter filter)
	{
		super.setContents(filter);
		restrictedBox.setSelected(filter.restricted);
	}
}
