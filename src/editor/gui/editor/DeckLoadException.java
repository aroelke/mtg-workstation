package editor.gui.editor;

import java.io.File;

/**
 * This class represents an exception that might occur during loading a deck.
 */
@SuppressWarnings("serial")
public class DeckLoadException extends Exception
{
    /**
     * File that caused the exception.
     */
    public final File file;

    /**
     * Create a new exception with the specified file and message.
     * 
     * @param f File that was attempting to be loaded
     * @param message message to display
     */
    public DeckLoadException(File f, String message)
    {
        super(message);
        file = f;
    }

    /**
     * Create a new exception with the specified file caused by the specified
     * #Throwable.
     * 
     * @param f File that was attempting to be loaded
     * @param cause cause of this exception
     */
    public DeckLoadException(File f, Throwable cause)
    {
        super(cause.getMessage(), cause);
        file = f;
    }

    /**
     * Create a new exception with the specified file.
     * 
     * @param f File that was attempting to be loaded.
     */
    public DeckLoadException(File f)
    {
        this(f, "");
    }
}