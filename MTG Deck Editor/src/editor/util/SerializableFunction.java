package editor.util;

import java.io.Serializable;
import java.util.function.Function;

@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable
{
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
	
	static <T> SerializableFunction<T,T> identity()
	{
		return (SerializableFunction<T, T>)((t) -> t);
	}
}
