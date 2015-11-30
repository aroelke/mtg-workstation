package editor.filter.leaf.options;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 *
 * @param <T>
 */
public abstract class OptionsFilter<T> extends FilterLeaf<T>
{
	public Containment contain;
	public Set<T> selected;
	
	public OptionsFilter(FilterType t, Function<Card, T> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		selected = new HashSet<T>();
	}

	@Override
	public String content()
	{
		StringJoiner join = new StringJoiner(",", "{", "}");
		for (T option: selected)
			join.add(option.toString());
		return contain.toString() + join.toString();
	}
}
