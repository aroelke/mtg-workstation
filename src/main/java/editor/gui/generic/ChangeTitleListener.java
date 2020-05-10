package editor.gui.generic;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * This class is a listener for double clicks on the title of a component with a titled
 * border. When the title is double-clicked, a text field is overlaid over it that allows
 * the user to change the title of the border, or perform any other action.
 * 
 * @author Alec Roelke
 */
public class ChangeTitleListener extends MouseAdapter
{
    /**
     * Component that has the border to watch.
     */
    private JComponent component;
    /**
     * Border whose title is to be watched and edited.
     */
    private TitledBorder titledBorder;
    /**
     * Popup containing the text field that is overlaid over the border.
     */
    private JPopupMenu editPopup;
    /**
     * Text field containing the changes to the title.
     */
    private JTextField editTextField;

    /**
     * Create a new ChangeTitleListener that watches for a double-click on the title
     * of the border of <code>c</code> and performs the given action when the title
     * is changed this way.
     * 
     * @param c component containing the border to watch
     * @param change action to perform when the title is changed
     */
    public ChangeTitleListener(JComponent c, Consumer<String> change)
    {
        super();

        component = c;
        if (component.getBorder() instanceof TitledBorder)
            titledBorder = (TitledBorder)component.getBorder();
        else
            throw new IllegalArgumentException("component must have a titled border");

        editTextField = new JTextField();
        // Accept entry when enter is pressed
        editTextField.addActionListener((e) -> {
            String value = editTextField.getText();
            change.accept(value);
            editPopup.setVisible(false);
            editPopup.getInvoker().revalidate();
            editPopup.getInvoker().repaint();
        });
        // Throw away entry when ESC is pressed
        editTextField.registerKeyboardAction(
            (e) -> editPopup.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_FOCUSED
        );
        // Implicitly throw away entry when something else is clicked

        editPopup = new JPopupMenu();
        editPopup.setBorder(new EmptyBorder(0, 0, 0, 0));
        editPopup.add(editTextField);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            FontMetrics fm = component.getFontMetrics(titledBorder.getTitleFont());
            int titleWidth = fm.stringWidth(titledBorder.getTitle()) + 20;
            if (new Rectangle(0, 0, titleWidth, fm.getHeight()).contains(e.getPoint()))
            {
                editTextField.setText(titledBorder.getTitle());
                Dimension d = editTextField.getPreferredSize();
                d.width = titleWidth;
                editPopup.setPreferredSize(d);
                editPopup.show(component, 0, 0);
                editTextField.selectAll();
                editTextField.requestFocusInWindow();
            }
        }
    }
}