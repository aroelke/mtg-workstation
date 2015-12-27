package editor.gui.filter.editor;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import editor.database.characteristics.ManaCost;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

/**
 * TODO: Figure out error-checking for this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ManaCostFilterPanel extends FilterEditorPanel<ManaCostFilter>
{
	public static ManaCostFilterPanel create(ManaCostFilter f)
	{
		ManaCostFilterPanel panel = new ManaCostFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	private ComboBoxPanel<Containment> contain;
	private JTextField cost;
	
	private ManaCostFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		cost = new JTextField();
		add(cost);
	}
	
	@Override
	public Filter filter()
	{
		ManaCostFilter filter = new ManaCostFilter();
		filter.contain = contain.getSelectedItem();
		filter.cost = ManaCost.valueOf(cost.getText());
		return filter;
	}

	@Override
	public void setContents(ManaCostFilter filter)
	{
		contain.setSelectedItem(filter.contain);
		cost.setText(filter.cost.toString());
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof ManaCostFilter)
			setContents((ManaCostFilter)filter);
		else
			throw new IllegalArgumentException("Illegal mana cost filter " + filter.type.name());
	}
}
