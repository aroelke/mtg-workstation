package editor.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This interface is a version of #Predicate that is #Serializable.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of object to test
 */
@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable
{
	/**
	 * Returns a serializable predicate that tests if two arguments are equal according to #Objects.equals(Object, Object)
	 * 
	 * @param <T> the type of the objects to compare
	 * @param targetRef the object reference with which to compare for equality (can be null)
	 * @return a predicate that tests if the target reference is equal to another
	 */
	static <T> SerializablePredicate<T> isEqual(Object targetRef)
	{
		return (SerializablePredicate<T>)((t) -> Objects.equals(t, targetRef));
	}
	
	@Override
	default SerializablePredicate<T> and(Predicate<? super T> other)
	{
		return (SerializablePredicate<T>)((t) -> test(t) && other.test(t));
	}
	
	@Override
	default SerializablePredicate<T> negate()
	{
		return (SerializablePredicate<T>)((t) -> !test(t));
	}
	
	@Override
	default SerializablePredicate<T> or(Predicate<? super T> other)
	{
		return (SerializablePredicate<T>)((t) -> test(t) || other.test(t));
	}
}
