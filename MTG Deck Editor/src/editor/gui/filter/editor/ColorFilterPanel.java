package editor.gui.filter.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import editor.database.characteristics.MTGColor;
import editor.database.symbol.ColorSymbol;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

@SuppressWarnings("serial")
public class ColorFilterPanel extends FilterEditorPanel<ColorFilter>
{
	public static ColorFilterPanel create(ColorFilter f)
	{
		ColorFilterPanel panel = new ColorFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	private FilterType type;
	private ComboBoxPanel<Containment> contain;
	private Map<MTGColor, JCheckBox> colors;
	private JCheckBox multiCheckBox;
	
	private ColorFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		colors = new HashMap<MTGColor, JCheckBox>();
		for (MTGColor color: MTGColor.values())
		{
			JCheckBox box = new JCheckBox();
			colors.put(color, box);
			add(box);
			JLabel symbol = new JLabel(ColorSymbol.get(color).getIcon(13));
			add(symbol);
		}
		
		multiCheckBox = new JCheckBox("Multicolored");
		add(multiCheckBox);
	}
	
	@Override
	public Filter filter()
	{
		ColorFilter filter = (ColorFilter)type.createFilter();
		filter.contain = contain.getSelectedItem();
		filter.colors.addAll(colors.keySet().stream().filter((c) -> colors.get(c).isSelected()).collect(Collectors.toSet()));
		filter.multicolored = multiCheckBox.isSelected();
		return filter;
	}

	@Override
	public void setContents(ColorFilter filter)
	{
		type = filter.type;
		contain.setSelectedItem(filter.contain);
		for (MTGColor color: MTGColor.values())
			colors.get(color).setSelected(filter.colors.contains(color));
		multiCheckBox.setSelected(filter.multicolored);
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof ColorFilter)
			setContents((ColorFilter)filter);
		else
			throw new IllegalArgumentException("Illegal color filter " + filter.type.name());
	}
}
