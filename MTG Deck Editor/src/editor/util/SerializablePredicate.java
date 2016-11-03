package editor.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

public interface SerializablePredicate<T> extends Predicate<T>, Serializable
{
	default SerializablePredicate<T> and(Predicate<? super T> other)
	{
		return (SerializablePredicate<T>)((t) -> test(t) && other.test(t));
	}
	
	static <T> SerializablePredicate<T> isEqual(Object targetRef)
	{
		return (SerializablePredicate<T>)((t) -> Objects.equals(t, targetRef));
	}
	
	default SerializablePredicate<T> negate()
	{
		return (SerializablePredicate<T>)((t) -> !test(t));
	}
	
	default SerializablePredicate<T> or(Predicate<? super T> other)
	{
		return (SerializablePredicate<T>)((t) -> test(t) || other.test(t));
	}
}
