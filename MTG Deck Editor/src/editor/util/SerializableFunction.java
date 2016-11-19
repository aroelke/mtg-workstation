package editor.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * This interface is a version of #Function that is #Serializable.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of object to accept
 * @param <R> Type of object to return
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable
{
	/**
	 * Create a function that returns its input argument.
	 * 
	 * @return a function that always returns its input argument
	 */
	static <T> SerializableFunction<T,T> identity()
	{
		return (SerializableFunction<T, T>)((t) -> t);
	}
	
	@Override
	default <V> SerializableFunction<T, V> andThen(Function<? super R,? extends V> after)
	{
		return (SerializableFunction<T, V>)((t) -> after.apply(apply(t)));
	}
	
	@Override
	default <V> SerializableFunction<V,R> compose(Function<? super V,? extends T> before)
	{
		return (SerializableFunction<V, R>)((v) -> apply(before.apply(v)));
	}
}
