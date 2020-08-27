package editor.util;

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
    /** Whether or not the value has been computed yet. */
    private boolean flag;
    /** Reference to either the computed value or its generator (only one is needed at a time). */
    private Object ref;

    /**
     * Create a new Lazy supplier.
     *
     * @param val Function supplying the value to lazily evaluate.
     */
    public Lazy(Supplier<T> val)
    {
        flag = false;
        ref = val;
    }

    /**
     * If the value has not been computed, compute it and then return it.  Otherwise,
     * just return it.
     *
     * @return The value computed by the function
     */
    @Override
    @SuppressWarnings("unchecked")
    public T get()
    {
        if (!flag)
        {
            flag = true;
            ref = ((Supplier<T>)ref).get();
        }
        return (T)ref;
    }
}
