package editor.util;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return switch (this) {
            case CONTAINS_ANY_OF -> b.isEmpty() || b.stream().anyMatch(a::contains);
            case CONTAINS_NONE_OF -> b.stream().noneMatch(a::contains);
            case CONTAINS_ALL_OF -> a.containsAll(b);
            case CONTAINS_NOT_ALL_OF -> CONTAINS_ANY_OF.test(a, b) && !a.containsAll(b);
            case CONTAINS_EXACTLY -> a.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).equals(
                                     b.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
            case CONTAINS_NOT_EXACTLY -> a.stream().anyMatch((o) -> !b.contains(o)) || b.stream().anyMatch((o) -> !a.contains(o));
        };
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
