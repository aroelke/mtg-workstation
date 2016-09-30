package editor.util;

import java.util.Collection;
import java.util.function.BiPredicate;

/**
 * This enumeration represents a way that elements from one set can be contained in another.
 * 
 * TODO: Change this to a map like Comparison did.
 * 
 * @author Alec Roelke
 */
public enum Containment implements BiPredicate<Collection<?>, Collection<?>>
{
	CONTAINS_ANY_OF((a, b) -> {
		if (b.isEmpty())
			return true;
		for (Object o: b)
			if (a.contains(o))
				return true;
		return false;
	}),
	CONTAINS_NONE_OF((a, b) -> {
		for (Object o: b)
			if (a.contains(o))
				return false;
		return true;
	}),
	CONTAINS_ALL_OF(Collection::containsAll),
	CONTAINS_NOT_ALL_OF((a, b) -> CONTAINS_ANY_OF.test(a,  b) && !a.containsAll(b)),
	CONTAINS_EXACTLY((a, b) -> a.containsAll(b) && b.containsAll(a)),
	CONTAINS_NOT_EXACTLY((a, b) -> {
		for (Object o: a)
			if (!b.contains(o))
				return true;
		for (Object o: b)
			if (!a.contains(o))
				return true;
		return false;
	});
	
	/**
	 * @return Array containing Containments that have meaning when the list has only one
	 * value.
	 */
	public static Containment[] singletonValues()
	{
		return new Containment[] {CONTAINS_ANY_OF, CONTAINS_NONE_OF};
	}
	
	/**
	 * Get a Containment based on the specified String.
	 * 
	 * @param s String to parse
	 * @return Containment matching the specified String.
	 */
	public static Containment get(String s)
	{
		for (Containment e: Containment.values())
			if (s.equalsIgnoreCase(e.toString()))
				return e;
		throw new IllegalArgumentException("Illegal containment string \"" + s + "\"");
	}
	
	/**
	 * Function to perform when calling this Containment.
	 */
	private BiPredicate<Collection<?>, Collection<?>> func;
	
	/**
	 * Create a new Containment.
	 * 
	 * @param f Function to perform when calling the new Containment.
	 */
	private Containment(BiPredicate<Collection<?>, Collection<?>> f)
	{
		func = f;
	}
	
	/**
	 * @return A String representation of this Containment, which is its name
	 * in lower case with _ replaced with a space.
	 */
	@Override
	public String toString()
	{
		return super.toString().replace("_", " ").toLowerCase();
	}

	/**
	 * Test if the first collection contains the elements of the second with
	 * this Containment's function.
	 * 
	 * @param a First collection to test
	 * @param b Second collection to test
	 * @return <code>true</code> if this Containment's function returns true when
	 * performed on the two operands and <code>false</code> otherwise.
	 */
	@Override
	public boolean test(Collection<?> a, Collection<?> b)
	{
		return func.test(a, b);
	}
}
