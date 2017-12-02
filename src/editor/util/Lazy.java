package editor.util;

import java.util.function.Supplier;

/**
 * This class "lazily" supplies a value by not evaluating it until the first time
 * it is requested.  Then that function's result is cached so it doesn't have to
 * be reevaluated again.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of object that is being lazily evaluated
 */
public class Lazy<T> implements Supplier<T>
{

	/**
	 * Cached value.
	 */
	private T value;
	
	/**
	 * Supplier for the cached value.
	 */
	private Supplier<T> supplier;
	
	/**
	 * Create a new Lazy supplier.
	 * 
	 * 
	 * @param val Function supplying the value to lazily evaluate.
	 */
	public Lazy(Supplier<T> val)
	{
		value = null;
		supplier = val;
	}
	
	/**
	 * If the value has not been computed, compute it and then return it.  Otherwise,
	 * just return it.
	 * 
	 * @return The value computed by the function
	 */
	@Override
	public T get()
	{
		if (value == null)
			value = supplier.get();
		return value;
	}
}
