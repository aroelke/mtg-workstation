package util;

/**
 * This class represents an operation that takes three arguments and returns no result.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface TriConsumer<T, U, V>
{
	/**
	 * Perform the operation on the given arguments.
	 * 
	 * @param t First argument of the operation
	 * @param u Second argument of the operation
	 * @param v Third argument of the operation
	 */
	public void accept(T t, U u, V v);
	
	/**
	 * Creates a TriConsumer that performs this TriConsumer's operation followed by the one
	 * specified by <code>after</code>.
	 * 
	 * @param after Operation to perform after this one
	 * @return A TriConsumer composed of this one and <code>after</code>.
	 */
	default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after)
	{
		return (t, u, v) -> {
			accept(t, u, v);
			after.accept(t, u, v);
		};
	}
}
