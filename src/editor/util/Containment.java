package editor.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiPredicate;

/**
 * This enumeration represents a way that elements from one #Collection can be contained in another.
 *
 * @author Alec Roelke
 */
public enum Containment implements BiPredicate<Collection<?>, Collection<?>>
{
    /**
     * The first collection contains all of the elements in the second collection.
     * Opposite of {@link #CONTAINS_NOT_ALL_OF}.
     */
    CONTAINS_ALL_OF,
    /**
     * The first collection contains any element in the second collection.  A collection always contains
     * an empty collection.
     * Opposite of {@link #CONTAINS_NONE_OF}.
     */
    CONTAINS_ANY_OF,
    /**
     * The first collection contains exactly the same elements as the second collection.
     * Opposite of {@link #CONTAINS_NOT_EXACTLY}.
     */
    CONTAINS_EXACTLY,
    /**
     * The first collection contains no elements from the second collection.
     * Opposite of {@link #CONTAINS_ANY_OF}.
     */
    CONTAINS_NONE_OF,
    /**
     * The first collection contains some, but not all, of the elements in the second collection.
     * Opposite if {@link #CONTAINS_ALL_OF}.
     */
    CONTAINS_NOT_ALL_OF,
    /**
     * The first collection contains some or none of the elements as the second collection, but is not
     * exactly the same.  Opposite of {@link #CONTAINS_EXACTLY}.
     */
    CONTAINS_NOT_EXACTLY;

    /**
     * @param contain String representation of the desired Containment
     * @return The Containment corresponding to the given String.
     */
    public static Containment parseContainment(String contain)
    {
        return Containment.valueOf(contain.toUpperCase().replace(' ', '_'));
    }

    @Override
    public boolean test(Collection<?> a, Collection<?> b)
    {
        switch (this)
        {
        case CONTAINS_ANY_OF:
            if (b.isEmpty())
                return true;
            for (Object o : b)
                if (a.contains(o))
                    return true;
            return false;
        case CONTAINS_NONE_OF:
            for (Object o : b)
                if (a.contains(o))
                    return false;
            return true;
        case CONTAINS_ALL_OF:
            return a.containsAll(b);
        case CONTAINS_NOT_ALL_OF:
            return CONTAINS_ANY_OF.test(a, b) && !a.containsAll(b);
        case CONTAINS_EXACTLY:
            var aMap = new HashMap<Object, Integer>();
            for (Object o : a)
                aMap.compute(o, (k, v) -> v == null ? 1 : v + 1);
            var bMap = new HashMap<Object, Integer>();
            for (Object o : b)
                bMap.compute(o, (k, v) -> v == null ? 1 : v + 1);
            return aMap.equals(bMap);
        case CONTAINS_NOT_EXACTLY:
            for (Object o : a)
                if (!b.contains(o))
                    return true;
            for (Object o : b)
                if (!a.contains(o))
                    return true;
            return false;
        default:
            throw new IllegalArgumentException("Illegal Containment " + this);
        }
    }

    /**
     * {@inheritDoc}
     * The String representation of a Containment is its name in lower case with
     * _ replaced with a space.
     */
    @Override
    public String toString()
    {
        return super.toString().replace("_", " ").toLowerCase();
    }
}
