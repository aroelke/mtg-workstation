package editor.gui.filter.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import editor.database.characteristics.ManaType;
import editor.database.symbol.ColorSymbol;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.FilterLeaf;
import editor.gui.generic.ComboBoxPanel;
import editor.util.Containment;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by a color characteristic.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ColorFilterPanel extends FilterEditorPanel<ColorFilter>
{
	/**
	 * Type of the filter being edited.
	 */
	private String type;
	/**
	 * Combo box showing the containment options.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Map of colors onto their corresponding check boxes.
	 */
	private Map<ManaType, JCheckBox> colors;
	/**
	 * Check box indicating that only multicolored cards should be matched.
	 */
	private JCheckBox multiCheckBox;
	
	/**
	 * Create a new ColorFilterPanel.
	 */
	public ColorFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Containment options
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		// Check boxes for selecting colors
		colors = new HashMap<ManaType, JCheckBox>();
		for (ManaType color: ManaType.colors())
		{
			JCheckBox box = new JCheckBox();
			colors[color] = box;
			add(box);
			JLabel symbol = new JLabel(ColorSymbol.get(color).getIcon(13));
			add(symbol);
		}
		
		// Check box for multicolored cards
		multiCheckBox = new JCheckBox("Multicolored");
		add(multiCheckBox);
	}
	
	/**
	 * Create a new ColorFilterPanel with initial contents obtained
	 * from the given ColorFilter.
	 * 
	 * @param f ColorFilter to get the contents from
	 */
	public ColorFilterPanel(ColorFilter f)
	{
		this();
		setContents(f);
	}
	
	/**
	 * @return A new ColorFilter whose contents reflect the selections
	 * made.
	 */
	@Override
	public Filter filter()
	{
		ColorFilter filter = (ColorFilter)FilterFactory.createFilter(type);
		filter.contain = contain.getSelectedItem();
		filter.colors.addAll(colors.keySet().stream().filter((c) -> colors[c].isSelected()).collect(Collectors.toSet()));
		filter.multicolored = multiCheckBox.isSelected();
		return filter;
	}

	/**
	 * Set the contents of this ColorFilterPanel according to the contents
	 * of the given ColorFilter.
	 * 
	 * @param filter Filter to use
	 */
	@Override
	public void setContents(ColorFilter filter)
	{
		type = filter.type;
		contain.setSelectedItem(filter.contain);
		for (ManaType color: ManaType.colors())
			colors[color].setSelected(filter.colors.contains(color));
		multiCheckBox.setSelected(filter.multicolored);
	}

	/**
	 * Set the contents of this ColorFilterPanel according to the contents
	 * of the given FilterLeaf.
	 * 
	 * @param filter Filter to use
	 * @throws IllegalArgumentException if the given filter is not an instance of
	 * ColorFilter
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof ColorFilter)
			setContents((ColorFilter)filter);
		else
			throw new IllegalArgumentException("Illegal color filter " + filter.type);
	}
}
