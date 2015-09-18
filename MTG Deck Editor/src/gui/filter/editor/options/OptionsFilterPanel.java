package gui.filter.editor.options;

import gui.filter.ComboBoxPanel;
import gui.filter.FilterType;
import gui.filter.editor.FilterEditorPanel;

import java.awt.BorderLayout;
import java.util.stream.Collectors;

import javax.swing.JList;
import javax.swing.JScrollPane;

import util.Containment;

/**
 * This class represents a FilterPanel that presents a set of options
 * to the user to choose from to fill out the filter.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class OptionsFilterPanel<T> extends FilterEditorPanel
{
	/**
	 * Maximum number of rows to show in the list pane.
	 */
	public static final int MAX_ROWS = 7;
	
	/**
	 * Combo box indicating containment.
	 */
	protected ComboBoxPanel<Containment> contain;
	/**
	 * Box showing the options to choose from.
	 */
	protected JList<T> optionsBox;
	/**
	 * Options that can be chosen from.
	 */
	private T[] options;
	
	/**
	 * Create a new OptionsFilterPanel.
	 * 
	 * @param type Type of filter the new OptionsFilterPanel edits
	 */
	public OptionsFilterPanel(FilterType type, Containment[] c, T[] o)
	{
		super(type);
		setLayout(new BorderLayout());
		
		options = o;
		
		// Set containment combo box
		add(contain = new ComboBoxPanel<Containment>(c), BorderLayout.WEST);
		
		// List pane showing the available options
		optionsBox = new JList<T>(options);
		optionsBox.setVisibleRowCount(Math.min(MAX_ROWS, options.length));
		add(new JScrollPane(optionsBox), BorderLayout.CENTER);
	}
	
	/**
	 * @return <code>true</code> if this SingletonOptionsFilterPanel has nothing in it selected,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return optionsBox.getSelectedIndices().length == 0;
	}
	
	/**
	 * @return A String representation of this MultiOptionsFilterPanel's contents, which
	 * is its containment type followed by the selected options surrounded by braces.
	 */
	@Override
	protected String repr()
	{
		return contain.getSelectedItem().toString() + "{" + String.join(",", optionsBox.getSelectedValuesList().stream().map(String::valueOf).collect(Collectors.toList())) + "}";
	}
	
	/**
	 * Automatically selects values in the list from the given String.
	 * 
	 * @param content String to parse for values.
	 */
	@Override
	public void setContents(String content)
	{
		int index = content.indexOf('{');
		contain.setSelectedItem(Containment.get(content.substring(0, index)));
		String[] selectedOptions = content.substring(index + 1, content.length() - 1).split(",");
		for (String o: selectedOptions)
		{
			for (int i = 0; i < options.length; i++)
				if (o.equalsIgnoreCase(options[i].toString()))
					optionsBox.addSelectionInterval(i, i);
		}
		optionsBox.ensureIndexIsVisible(optionsBox.getSelectedIndex());
	}
}
