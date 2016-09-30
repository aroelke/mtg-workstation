package editor.util;

import java.util.Collection;
import java.util.function.BiPredicate;

/**
 * This enumeration represents a way that elements from one set can be contained in another.
 * 
 * @author Alec Roelke
 */
public enum Containment implements BiPredicate<Collection<?>, Collection<?>>
{
	CONTAINS_ANY_OF(),
	CONTAINS_NONE_OF(),
	CONTAINS_ALL_OF(),
	CONTAINS_NOT_ALL_OF(),
	CONTAINS_EXACTLY(),
	CONTAINS_NOT_EXACTLY();
	
	/**
	 * @return Array containing Containments that have meaning when the list has only one
	 * value.
	 */
	public static Containment[] singletonValues()
	{
		return new Containment[] {CONTAINS_ANY_OF, CONTAINS_NONE_OF};
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
	 * @param contain String representation of the desired Containment
	 * @return The Containment corresponding to the given String.
	 */
	public static Containment fromString(String contain)
	{
		return Containment.valueOf(contain.toUpperCase().replace(' ', '_'));
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
		switch (this)
		{
		case CONTAINS_ANY_OF:
			if (b.isEmpty())
				return true;
			for (Object o: b)
				if (a.contains(o))
					return true;
			return false;
		case CONTAINS_NONE_OF:
			for (Object o: b)
				if (a.contains(o))
					return false;
			return true;
		case CONTAINS_ALL_OF:
			return a.containsAll(b);
		case CONTAINS_NOT_ALL_OF:
			return CONTAINS_ANY_OF.test(a, b) && !a.containsAll(b);
		case CONTAINS_EXACTLY:
			return a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
		case CONTAINS_NOT_EXACTLY:
			for (Object o: a)
				if (!b.contains(o))
					return true;
			for (Object o: b)
				if (!a.contains(o))
					return true;
			return false;
		default:
			throw new IllegalArgumentException("Illegal Containment " + this);
		}
	}
}
