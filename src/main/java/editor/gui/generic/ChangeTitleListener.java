package editor.gui.generic;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
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
    private TitledBorder border;
    /**
     * Popup containing the text field that is overlaid over the border.
     */
    private JPopupMenu popup;
    /**
     * Text field containing the changes to the title.
     */
    private JTextField field;
    /**
     * Function to get the horizontal and vertical offsets of the title editor field
     * based on the contents of the text (most often it will depend only on if the
     * title is currently blank).
     */
    private Function<String, Integer> hgap, vgap;

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
        this(c, (TitledBorder)c.getBorder(), 0, 0, change);
    }

    /**
     * Create a new ChangeTitleListener that watches for a double-click on the title
     * of the border of <code>c</code> and performs the given action when the title
     * is changed this way, with the text field offset.
     * 
     * @param c component containing the border to watch
     * @param b border whose titled should be changed
     * @param h horizontal offset of the editor field
     * @param v vertical offset of the editor field
     * @param change action to perform when the title is changed
     */
    public ChangeTitleListener(JComponent c, TitledBorder b, int h, int v, Consumer<String> change)
    {
        this(c, b, (s) -> h, (s) -> v, change);
    }

    /**
     * Create a new ChangeTitleListener that watches for a double-click on the title
     * of the border of <code>c</code> and performs the given action when the title
     * is changed this way.
     * 
     * @param c component containing the border to watch
     * @param b border whose title should be changed
     * @param h horizontal offset of the editor field based on the current title
     * @param v vertical offset of the editor field based on the current title
     * @param change action to perform when the title is changed
     */
    public ChangeTitleListener(JComponent c, TitledBorder b, Function<String, Integer> h, Function<String, Integer> v, Consumer<String> change)
    {
        super();

        component = c;
        border = b;
        hgap = h;
        vgap = v;

        field = new JTextField();
        // Accept entry when enter is pressed
        field.addActionListener((e) -> {
            String value = field.getText();
            change.accept(value);
            popup.setVisible(false);
            popup.getInvoker().revalidate();
            popup.getInvoker().repaint();
        });
        // Throw away entry when ESC is pressed
        field.registerKeyboardAction(
            (e) -> popup.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_FOCUSED
        );
        // Implicitly throw away entry when something else is clicked

        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.add(field);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            FontMetrics metrics = component.getFontMetrics(border.getTitleFont());
            int width = metrics.stringWidth(border.getTitle().isEmpty() ? "Change title" : border.getTitle()) + 20;
            if (new Rectangle(hgap.apply(border.getTitle()), vgap.apply(border.getTitle()), width, metrics.getHeight()).contains(e.getPoint()))
            {
                field.setText(border.getTitle());
                popup.setPreferredSize(new Dimension(width, field.getPreferredSize().height));
                popup.show(component, hgap.apply(border.getTitle()), vgap.apply(border.getTitle()));
                field.selectAll();
                field.requestFocusInWindow();
            }
        }
    }
}