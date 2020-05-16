package editor.util;

/**
 * This class represents a function of one argument and no return value that can
 * throw an exception.
 * 
 * @param <T> type of argument the function will consume
 * @param <E> type of exception that could be thrown
 */
@FunctionalInterface
public interface ExceptionConsumer<T, E extends Exception>
{
    /**
     * Consume the argument and perform actions on it.
     * 
     * @param t argument to consume
     * @throws E if the function throws an exception
     */
    public void accept(T t) throws E;
}