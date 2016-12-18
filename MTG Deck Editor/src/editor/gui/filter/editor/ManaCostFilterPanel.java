package editor.gui.filter.editor;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import editor.database.characteristics.ManaCost;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.gui.generic.ComboBoxPanel;
import editor.util.Containment;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by mana cost.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ManaCostFilterPanel extends FilterEditorPanel<ManaCostFilter>
{
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
	public ManaCostFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		cost = new JTextField();
		cost.getDocument().addDocumentListener(new DocumentListener()
		{
			private void update(DocumentEvent e)
			{
				if (ManaCost.tryParseManaCost(cost.getText()) == null)
					cost.setBackground(Color.PINK);
				else
					cost.setBackground(Color.WHITE);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				update(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				update(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				update(e);
			}
		});
		add(cost);
	}
	
	/**
	 * Create a new ManaCostFilterPanel using the given ManaCostFilter
	 * to set the contents of its fields.
	 * 
	 * @param f filter to use for initialization
	 */
	public ManaCostFilterPanel(ManaCostFilter f)
	{
		this();
		setContents(f);
	}
	
	@Override
	public Filter filter()
	{
		ManaCostFilter filter = new ManaCostFilter();
		filter.contain = contain.getSelectedItem();
		filter.cost = ManaCost.parseManaCost(cost.getText());
		if (filter.cost == null)
			filter.cost = new ManaCost();
		return filter;
	}

	@Override
	public void setContents(ManaCostFilter filter)
	{
		contain.setSelectedItem(filter.contain);
		cost.setText(filter.cost.toString());
	}

	@Override
	public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
	{
		if (filter instanceof ManaCostFilter)
			setContents((ManaCostFilter)filter);
		else
			throw new IllegalArgumentException("Illegal mana cost filter " + filter.type());
	}
}
