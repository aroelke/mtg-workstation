package editor.util;

import java.util.function.Supplier;

public class UndoableAction<U, R>
{
    private final Supplier<R> forward;
    private final Supplier<U> reverse;

    public UndoableAction(final Supplier<R> f, final Supplier<U> r)
    {
        forward = f;
        reverse = r;
    }

    public U undo()
    {
        return reverse.get();
    }

    public R redo()
    {
        return forward.get();
    }
}