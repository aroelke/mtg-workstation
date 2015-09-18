package gui.filter.editor.options.multi;

import gui.filter.ComboBoxPanel;
import gui.filter.FilterType;
import gui.filter.editor.options.OptionsFilterPanel;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JList;
import javax.swing.JScrollPane;

import util.Containment;
import database.Card;

/**
 * This class represents a FilterPanel that filters Cards by a characteristic that has multiple
 * values among several options.
 * 
 * @author Alec
 *
 * @param <T> Type of the characteristic that will be filtered
 */
@SuppressWarnings("serial")
public class MultiOptionsFilterPanel<T extends CharSequence> extends OptionsFilterPanel
{
	/**
	 * Function representing the characteristic to be filtered.
	 */
	private Function<Card, List<T>> param;
	/**
	 * Combo box indicating containment.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Box showing the options to choose from.
	 */
	protected JList<T> optionsBox;
	/**
	 * Options that can be chosen from.
	 */
	private T[] options;
	
	/**
	 * Create a new MultiOptionsFilterPanel.
	 * 
	 * @param type Type of filter the new MultiOptionsFilterPanel edits
	 * @param o List of options to be chosen from
	 * @param f Card characteristic to filter
	 */
	public MultiOptionsFilterPanel(FilterType type, T[] o, Function<Card, List<T>> f)
	{
		super(type);
		setLayout(new BorderLayout());
		
		param = f;
		options = o;
		
		// Set containment combo box
		add(contain = new ComboBoxPanel<Containment>(Containment.values()), BorderLayout.WEST);
		
		// List pane showing the available options
		optionsBox = new JList<T>(options);
		optionsBox.setVisibleRowCount(Math.min(MAX_ROWS, options.length));
		add(new JScrollPane(optionsBox), BorderLayout.CENTER);
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns true if the Card's values
	 * for are characteristic is among those selected with the appropriate containment
	 * type, and <code>false</code> otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> contain.getSelectedItem().test(param.apply(c), optionsBox.getSelectedValuesList());
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
		return contain.getSelectedItem().toString() + "{" + String.join(",", optionsBox.getSelectedValuesList()) + "}";
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
