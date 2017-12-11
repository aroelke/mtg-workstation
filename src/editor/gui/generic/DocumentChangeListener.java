package editor.gui.generic;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This is a {@link DocumentListener} that does the same thing for all modifications
 * of a document.
 *
 * @author Alec Roelke
 */
public abstract class DocumentChangeListener implements DocumentListener
{
    /**
     * When the document is modified in any way, perform an action.  The action
     * is normally the same no matter what the update is.
     *
     * @param e event describing the change
     */
    public abstract void update(DocumentEvent e);

    @Override
    public final void changedUpdate(DocumentEvent e)
    {
        update(e);
    }

    @Override
    public final void insertUpdate(DocumentEvent e)
    {
        update(e);
    }

    @Override
    public final void removeUpdate(DocumentEvent e)
    {
        update(e);
    }

}
