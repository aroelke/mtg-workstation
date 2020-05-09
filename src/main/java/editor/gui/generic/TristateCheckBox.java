package editor.gui.generic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;

/**
 * This class represents a check box with three states: selected, unselected,
 * and intermediate. A state of intermediate should be used to indicate when the
 * box represents an aspect of several items and not all of those items have the
 * that aspect.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TristateCheckBox extends JCheckBox
{
    /**
     * The three states a TristateCheckBox can have.
     */
    public static enum State { SELECTED, INDETERMINATE, UNSELECTED }

    /**
     * Icon serving as the base for a TristateCheckBox.  A square is drawn
     * over it if the box is in indeterminate state.
     */
    private static final Icon BASE_ICON = UIManager.getIcon("CheckBox.icon");

    /**
     * Amount of the image the square should fill.
     */
    private static final double ICON_FILL = 2.0/3.0;

    /**
     * When the check box is clicked, toggle its state.  If the state is
     * indeterminate, change it to being unselected.
     */
    private class ClickListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (state)
            {
            case SELECTED: case INDETERMINATE:
                setState(State.UNSELECTED);
                break;
            case UNSELECTED:
                state = State.SELECTED;
                setState(State.SELECTED);
                break;
            }
        }
    }

    /**
     * Icon for a TristateCheckBox.  Draws a normal check box icon, but
     * overlays it with a square if the box is in indeterminate state.
     */
    private class IndeterminateIcon implements Icon
    {
        /**
         * Color of the square to draw.
         */
        private Color boxColor;

        /**
         * Create a new IndeterminateIcon that draws a square of the specified
         * color.
         * 
         * @param color color of the square
         */
        public IndeterminateIcon(Color color)
        {
            boxColor = color;
        }

        @Override
        public int getIconHeight()
        {
            return BASE_ICON.getIconHeight();
        }

        @Override
        public int getIconWidth()
        {
            return BASE_ICON.getIconWidth();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            BASE_ICON.paintIcon(c, g, x, y);
            if (state == State.INDETERMINATE)
            {
                g.setColor(boxColor);
                g.fillRect((int)Math.floor(x + getIconWidth()*(1 - ICON_FILL)/2), (int)Math.floor(y + getIconHeight()*(1 - ICON_FILL)/2),
                           (int)Math.ceil(getIconWidth()*ICON_FILL), (int)Math.ceil(getIconHeight()*ICON_FILL));
            }
        }
    }

    /**
     * State of this TristateCheckBox.
     */
    private State state;

    /**
     * Create a new TristateCheckBox with the specified text and state.
     * 
     * @param text text to show next to the check box
     * @param s initial state of the check box
     */
    public TristateCheckBox(String text, State s)
    {
        super(text, s != State.UNSELECTED);
        setState(s);
        setIcon(new IndeterminateIcon(UIManager.getColor("CheckBox.foreground")));
        setRolloverIcon(new IndeterminateIcon(UIManager.getColor("CheckBox.foreground")));
        addActionListener(new ClickListener());
    }

    /**
     * Create a TristateCheckBox with the specified text in unselected state.
     * 
     * @param text text to show next to the check box
     */
    public TristateCheckBox(String text)
    {
        this(text, State.UNSELECTED);
    }

    /**
     * Create a TristateCheckBox with no text in unselected state.
     */
    public TristateCheckBox()
    {
        this("");
    }

    /**
     * @return The state of this TristateCheckBox.
     */
    public State getState()
    {
        return state;
    }

    /**
     * Check whether or not this TristateCheckBox is indeterminate.
     * 
     * @return <code>true</code> if this TristateCheckBox is indeterminate,
     * and <code>false</code> otherwise.
     */
    public boolean isPartial()
    {
        return state == State.INDETERMINATE;
    }

    /**
     * @inheritDoc
     * 
     * Also update the state to reflect whether or not the box is selected.
     * A state of indeterminate cannot be reached this way; to set it, use
     * #{@link #setState(State)}.
     */
    @Override
    public void setSelected(boolean b)
    {
        setState(b ? State.SELECTED : State.UNSELECTED);
    }

    /**
     * Set the state of this TristateCheckBox.  Also updates the selected
     * state.  A state of indeterminate is considered unselected.
     */
    public void setState(State s)
    {
        state = s;
        super.setSelected(state == State.SELECTED);
    }
}