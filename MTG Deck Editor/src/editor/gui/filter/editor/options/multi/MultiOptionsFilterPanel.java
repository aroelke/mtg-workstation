package editor.gui.filter.editor.options.multi;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import editor.database.Card;
import editor.gui.filter.FilterType;
import editor.gui.filter.editor.options.OptionsFilterPanel;
import editor.util.Containment;

/**
 * This class represents a FilterPanel that filters Cards by a characteristic that has multiple
 * values among several options.
 * 
 * @author Alec
 *
 * @param <T> Type of the characteristic that will be filtered
 */
@SuppressWarnings("serial")
public class MultiOptionsFilterPanel<T> extends OptionsFilterPanel<T>
{
	/**
	 * Function representing the characteristic to be filtered.
	 */
	private Function<Card, List<T>> param;
	
	/**
	 * Create a new MultiOptionsFilterPanel.
	 * 
	 * @param type Type of filter the new MultiOptionsFilterPanel edits
	 * @param o List of options to be chosen from
	 * @param f Card characteristic to filter
	 */
	public MultiOptionsFilterPanel(FilterType type, T[] o, Function<Card, List<T>> f)
	{
		super(type, Containment.values(), o);
		param = f;
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns true if the Card's values
	 * for are characteristic is among those selected with the appropriate containment
	 * type, and <code>false</code> otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> contain.getSelectedItem().test(param.apply(c), getSelectedValues());
	}
}
