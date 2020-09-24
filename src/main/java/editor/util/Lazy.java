package editor.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class "lazily" supplies a value by not evaluating it until the first time
 * it is requested.  Then that function's result is cached so it doesn't have to
 * be reevaluated again.
 *
 * @param <T> Type of object that is being lazily evaluated
 * @author Alec Roelke
 */
public class Lazy<T> implements Supplier<T>
{
    /** Supplier of the value which will be called once when the value is first accessed. */
    private transient Supplier<T> supplier;
    /** The cached value of the computation. */
    private T value;

    /**
     * Create a new Lazy supplier.
     *
     * @param val Function supplying the value to lazily evaluate.
     */
    public Lazy(Supplier<T> val)
    {
        supplier = Objects.requireNonNull(val);
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
        {
            synchronized (this)
            {
                if (value == null)
                {
                    value = supplier.get();
                    supplier = null;
                }
            }
        }
        return value;
    }
}
