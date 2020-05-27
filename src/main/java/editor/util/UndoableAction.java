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
public interface UndoableAction<R, U>
{
    /**
     * Create a new undoable action from the given functions.
     * 
     * @param <R> return type of the forward action
     * @param <U> return type of the reverse action
     * @param forward forward action to perform
     * @param reverse reverse action to perform
     * @return The new UndoableAction.
     */
    public static <R, U> UndoableAction<R, U> createAction(Supplier<R> forward, Supplier<U> reverse)
    {
        return new UndoableAction<>()
        {
            public R redo() { return forward.get(); }
            public U undo() { return reverse.get(); }
        };
    }

    /**
     * Perform the action (or redo it if it has been undone).
     * 
     * @return a value containing information about the result of performing
     * the action.
     */
    R redo();

    /**
     * Undo the action.
     * 
     * @return a value containing information about the result undoing the action.
     */
    U undo();
}