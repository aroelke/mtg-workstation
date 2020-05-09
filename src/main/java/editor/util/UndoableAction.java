package editor.util;

import java.util.function.Supplier;

/**
 * This class represents an action that can be undone, which is done by simply
 * using two actions that are semantically supposed to be opposites.  Each
 * action can optionally return a value.
 * 
 * @param <R> information to be returned by the action
 * @param <U> information to be returned by the undone action
 * @author Alec Roelke
 */
public class UndoableAction<R, U>
{
    /**
     * "Forward" action that represents what was done.
     */
    private final Supplier<R> forward;
    /**
     * "Reverse" action that represents how to undo the forward action.
     */
    private final Supplier<U> reverse;

    /**
     * Create a new UndoableAction.
     * 
     * @param f "forward" action representing what was done
     * @param r "reverse" action representing how to undo it
     */
    public UndoableAction(final Supplier<R> f, final Supplier<U> r)
    {
        forward = f;
        reverse = r;
    }

    /**
     * Perform the action (or redo it if it has been undone).
     * 
     * @return a value containing information about the result of performing
     * the action.
     */
    public R redo()
    {
        return forward.get();
    }

    /**
     * Undo the action.
     * 
     * @return a value containing information about the result undoing the action.
     */
    public U undo()
    {
        return reverse.get();
    }
}