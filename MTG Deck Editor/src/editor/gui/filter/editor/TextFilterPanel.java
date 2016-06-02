package editor.gui.filter.editor;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.TextFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

/**
 * This class represents a panel that corresponds to a filter that
 * groups cards according to a text characteristic.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TextFilterPanel extends FilterEditorPanel<TextFilter>
{
	/**
	 * Type of filter that this TextFilterPanel edits.
	 */
	private FilterType type;
	/**
	 * Combo box for choosing set containment.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Field for editing the text of the filter.
	 */
	private JTextField text;
	/**
	 * Check box specifying whether the text is a regular expression
	 * or not.
	 */
	private JCheckBox regex;
	
	/**
	 * Create a new TextFilterPanel.
	 */
	private TextFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		text = new JTextField();
		add(text);
		
		regex = new JCheckBox("regex");
		regex.addActionListener((e) -> contain.setVisible(!regex.isSelected()));
		add(regex);
	}
	
	/**
	 * Create a new TextFilterPanel and initialize its fields according
	 * to the contents of the given TextFilter.
	 * 
	 * @param f Filter to use for initialization
	 */
	public TextFilterPanel(TextFilter f)
	{
		this();
		setContents(f);
	}
	
	/**
	 * @return The TextFilter corresponding to the values of this
	 * TextFilterPanel's fields.
	 */
	@Override
	public Filter filter()
	{
		TextFilter filter = (TextFilter)type.createFilter();
		filter.contain = contain.getSelectedItem();
		filter.text = text.getText();
		filter.regex = regex.isSelected();
		return filter;
	}

	/**
	 * Set the values of this TextFilterPanel's fields based on
	 * the contents of the given TextFilter.
	 * 
	 * @param filter Filter to use for field values
	 */
	@Override
	public void setContents(TextFilter filter)
	{
		type = filter.type;
		contain.setSelectedItem(filter.contain);
		text.setText(filter.text);
		regex.setSelected(filter.regex);
		contain.setVisible(!filter.regex);
	}

	/**
	 * Set the values of this TextFilterPanel's fields based on
	 * the contents of the given FilterLeaf.
	 * 
	 * @param filter Filter to use for field values
	 * @throws IllegalArgumentException if the given filter is not a
	 * TextFilter.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof TextFilter)
			setContents((TextFilter)filter);
		else
			throw new IllegalArgumentException("Illegal text filter " + filter.type.name());
	}
}
