package editor.gui.filter.editor;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.TypeLineFilter;
import editor.gui.generic.ComboBoxPanel;
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
	public TypeLineFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		line = new JTextField();
		add(line);
	}
	
	/**
	 * Create a new TypeLineFilterPanel, using the given TypeLineFilter
	 * to initialize its fields.
	 * 
	 * @param f filter to use for initialization
	 */
	public TypeLineFilterPanel(TypeLineFilter f)
	{
		this();
		setContents(f);
	}
	
	@Override
	public Filter filter()
	{
		TypeLineFilter filter = (TypeLineFilter)FilterFactory.createFilter(FilterFactory.TYPE_LINE);
		filter.contain = contain.getSelectedItem();
		filter.line = line.getText();
		return filter;
	}

	@Override
	public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
	{
		if (filter instanceof TypeLineFilter)
			setContents((TypeLineFilter)filter);
		else
			throw new IllegalArgumentException("Illegal type line filter " + filter.type());
	}

	@Override
	public void setContents(TypeLineFilter filter)
	{
		contain.setSelectedItem(filter.contain);
		line.setText(filter.line);
	}
}
