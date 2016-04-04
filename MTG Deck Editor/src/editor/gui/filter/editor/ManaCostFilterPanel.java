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
 * This class represents a panel corresponding to a filter that groups
 * cards by mana cost.
 * 
 * TODO: Figure out error-checking for this
 * TODO: Make this display the mana symbols filtered for instead of just their text
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ManaCostFilterPanel extends FilterEditorPanel<ManaCostFilter>
{
	/**
	 * Create a new ManaCostFilterPanel using the given ManaCostFilter
	 * to set the contents of its fields.
	 * 
	 * @param f Filter to use for initialization
	 * @return The created ManaCostFilterPanel
	 */
	public static ManaCostFilterPanel create(ManaCostFilter f)
	{
		ManaCostFilterPanel panel = new ManaCostFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	/**
	 * Combo box indicating containment.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Text field for entering the text version of the mana cost.
	 */
	private JTextField cost;
	
	/**
	 * Create a new ManaCostFilterPanel.
	 */
	private ManaCostFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		cost = new JTextField();
		add(cost);
	}
	
	/**
	 * @return The ManaCostFilter that corresponds to the entries in
	 * this ManaCostFilterPanel's fields.
	 */
	@Override
	public Filter filter()
	{
		ManaCostFilter filter = new ManaCostFilter();
		filter.contain = contain.getSelectedItem();
		filter.cost = ManaCost.valueOf(cost.getText());
		return filter;
	}

	/**
	 * Set the contents of this ManaCostFilter's fields according
	 * to the contents of the given ManaCostFilter.
	 * 
	 * @param filter Filter to use for setting fields
	 */
	@Override
	public void setContents(ManaCostFilter filter)
	{
		contain.setSelectedItem(filter.contain);
		cost.setText(filter.cost.toString());
	}

	/**
	 * Set the contents of this ManaCostFilter's fields according
	 * to the contents of the given FilterLeaf.
	 * 
	 * @param filter Filter to use for setting fields
	 * @throws IllegalArgumentException if the given filter is not
	 * a ManaCostFilter.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof ManaCostFilter)
			setContents((ManaCostFilter)filter);
		else
			throw new IllegalArgumentException("Illegal mana cost filter " + filter.type.name());
	}
}
