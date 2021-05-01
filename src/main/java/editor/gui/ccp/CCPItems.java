package editor.gui.ccp;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

/**
 * This is a convenience class for creating a set of cut, copy, and paste menu items
 * with commonly-used keyboard accelerators (Ctrl+X, Ctrl+C, Ctrl+V, respectively)
 * optionally already installed. The menu text and keyboard accelerators are not
 * customizable.
 * 
 * @param cut cut menu item
 * @param copy copy menu item
 * @param paste paste menu item
 * 
 * @author Alec Roelke
 */
public record CCPItems(JMenuItem cut, JMenuItem copy, JMenuItem paste)
{
    /**
     * Create a new set of cut, copy, and paste menu items that allow the reference to the source
     * component performing the action to change what it points to over time.
     * 
     * @param source function for getting the component performing the actions
     * @param accelerate add keyboard accelerators
     */
    public CCPItems(Supplier<? extends Component> source, boolean accelerate)
    {
        this(new JMenuItem("Cut"), new JMenuItem("Copy"), new JMenuItem("Paste"));

        cut.addActionListener((e) -> TransferHandler.getCutAction().actionPerformed(new ActionEvent(source.get(), ACTION_PERFORMED, null)));
        copy.addActionListener((e) -> TransferHandler.getCopyAction().actionPerformed(new ActionEvent(source.get(), ACTION_PERFORMED, null)));
        paste.addActionListener((e) -> TransferHandler.getPasteAction().actionPerformed(new ActionEvent(source.get(), ACTION_PERFORMED, null)));

        if (accelerate)
        {
            cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
            copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
            paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        }
    }

    /**
     * Create a new set of cut, copy, and paste menu items with a static source reference
     * (i.e. if the reference passed here changes later, these items won't see the change).
     * 
     * @param source component performing the actions
     * @param accelerate add keyboard accelerators
     */
    public CCPItems(Component source, boolean accelerate)
    {
        this(() -> source, accelerate);
    }
}