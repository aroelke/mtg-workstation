package editor.gui.generic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.UIManager;

/**
 * This class contains static methods that perform useful operations on Swing components.
 *
 * @author Alec Roelke
 */
public interface ComponentUtils
{
    /**
	 * Size of the text in oracle text and rulings tabs.
	 */
	int TEXT_SIZE = UIManager.getFont("Label.font").getSize();

    /**
     * Set the font of the given component, and then repeat this process for all of its
     * children if it is a container.
     *
     * @param component component to set the font of
     * @param f         font to set the component and its children to
     */
    static void changeFontRecursive(Component component, Font f)
    {
        component.setFont(f);
        if (component instanceof Container container)
            for (Component child : container.getComponents())
                changeFontRecursive(child, f);
    }

    /**
     * Create a fixed-width component with a fixed-height vertical separator in the middle.
     * 
     * @param width width of the component
     * @param height height of the separator
     * @return the component with the separator.
     */
    static JComponent createHorizontalSeparator(int width, int height)
    {
        Box panel = new Box(BoxLayout.X_AXIS);
        panel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        panel.add(Box.createHorizontalStrut((width - separator.getPreferredSize().width)/2));
        panel.add(separator);
        panel.add(Box.createHorizontalGlue());
        panel.setPreferredSize(new Dimension(width, height));
        if (height > 0)
            panel.setMaximumSize(new Dimension(width, height));
        return panel;
    }

    /**
     * Create a fixed-width component with a vertical separator in the middle that
     * allows the parent layout manager to size it.
     * 
     * @param width width of the component
     * @return the component with the separator.
     */
    static JComponent createHorizontalSeparator(int width)
    {
        return createHorizontalSeparator(width, 0);
    }
}
