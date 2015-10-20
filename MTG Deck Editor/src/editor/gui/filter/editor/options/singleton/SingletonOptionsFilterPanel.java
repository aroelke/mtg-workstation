package editor.gui.filter.editor.options.singleton;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import editor.database.Card;
import editor.gui.filter.FilterType;
import editor.gui.filter.editor.options.OptionsFilterPanel;
import editor.util.Containment;

/**
 * This class represents a FilterPanel that filters Cards by a characteristic that has one value
 * among several options.
 * 
 * @author Alec
 *
 * @param <T> Type of the characteristic that will be filtered
 */
@SuppressWarnings("serial")
public class SingletonOptionsFilterPanel<T> extends OptionsFilterPanel<T>
{
	/**
	 * Function representing the characteristic to be filtered.
	 */
	private Function<Card, T> param;
	
	/**
	 * Create a new SingletonOptionsFilterPanel.
	 * 
	 * @param type Type of filter the new SingletonOptionsFilterPanel edits
	 * @param o List of options to be chosen from
	 * @param f Card characteristic to filter
	 */
	public SingletonOptionsFilterPanel(FilterType type, T[] o, Function<Card, T> f)
	{
		super(type, Containment.singletonValues(), o);
		param = f;
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns true if the Card's value
	 * for the characteristic is among those selected, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> contain.getSelectedItem().test(optionsBox.getSelectedValuesList(), Arrays.asList(param.apply(c)));
	}
}
