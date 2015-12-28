package editor.gui.filter.editor;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.TypeLineFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

/**
 * This class represents a panel that corresponds to a filter that
 * groups cards by their entire type lines.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TypeLineFilterPanel extends FilterEditorPanel<TypeLineFilter>
{
	/**
	 * Create a new TypeLineFilterPanel, using the given TypeLineFilter
	 * to initialize its fields.
	 * 
	 * @param f Filter to use for initialization
	 * @return The new TypeLineFilterPanel.
	 */
	public static TypeLineFilterPanel create(TypeLineFilter f)
	{
		TypeLineFilterPanel panel = new TypeLineFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	/**
	 * Combo box for editing set containment.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Text field for editing the type line to match.
	 */
	private JTextField line;
	
	/**
	 * Create a new TypeLineFilterPanel.
	 */
	private TypeLineFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		line = new JTextField();
		add(line);
	}
	
	/**
	 * @return The TypeLineFilter corresponding to values of this
	 * TypeLineFilterPanel's fields.
	 */
	@Override
	public Filter filter()
	{
		TypeLineFilter filter = (TypeLineFilter)FilterType.TYPE_LINE.createFilter();
		filter.contain = contain.getSelectedItem();
		filter.line = line.getText();
		return filter;
	}

	/**
	 * Fill in the fields of this TypeLineFilterPanel using the contents
	 * of the given TypeLineFilter.
	 * 
	 * @param filter Filter to use for filling out fields
	 */
	@Override
	public void setContents(TypeLineFilter filter)
	{
		contain.setSelectedItem(filter.contain);
		line.setText(filter.line);
	}

	/**
	 * Fill in the fields of this TypeLineFilterPanel using the contents
	 * of the given FilterLeaf.
	 * 
	 * @param filter Filter to use for filling out fields
	 * @throws IllegalArgumentException if the given filter is not a
	 * TypeLineFilter.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof TypeLineFilter)
			setContents((TypeLineFilter)filter);
		else
			throw new IllegalArgumentException("Illegal type line filter " + filter.type.name());
	}
}
