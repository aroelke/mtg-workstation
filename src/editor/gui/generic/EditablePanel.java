package editor.gui.generic;

import java .awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

import editor.util.MouseListenerFactory;

/**
 * This class represents a panel with a line of text that can be edited by
 * double-clicking on it. It also contains a box with an "X" in it to the right
 * of the text that can be used to remove the panel.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EditablePanel extends JPanel
{
    /**
     * String signaling the event of the close butting being clicked.
     */
    public static final String CLOSE = "close";
    /**
     * String signaling the event of the text being changed.
     */
    public static final String EDIT = "edit";
    /**
     * String signaling the event of the text change being canceled.
     */
    public static final String CANCEL = "cancel";

    /**
     * This class represents a button with an "X" on it that might be used to
     * indicate the ability to close something. The button appears depressed
     * while the mouse is held down on it.
     */
    private class CloseButton extends JButton
    {
        /**
         * Size of the "X".
         */
        private static final int CLOSE_SIZE = 17;

        /**
         * Create a new CloseButton.
         */
        public CloseButton()
        {
            setPreferredSize(new Dimension(CLOSE_SIZE, CLOSE_SIZE));
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createRaisedBevelBorder());
            setBorderPainted(false);
            addMouseListener(MouseListenerFactory.createMotionListener(
                (e) -> setBorderPainted(true),
                (e) -> setBorderPainted(false)
            ));
            setRolloverEnabled(true);
            addMouseListener(MouseListenerFactory.createHoldListener(
                (e) -> setBorder(BorderFactory.createLoweredBevelBorder()),
                (e) -> setBorder(BorderFactory.createRaisedBevelBorder())
            ));
            addActionListener((e) -> {
                for (ActionListener listener : listeners)
                    listener.actionPerformed(new ActionEvent(EditablePanel.this, ActionEvent.ACTION_PERFORMED, CLOSE));
            });
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();

            g2.setColor(getModel().isRollover() ? Color.RED : Color.BLACK);
            g2.setStroke(new BasicStroke(1));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed())
                g2.translate(1, 1);
            g2.drawLine(getWidth()/3, getHeight()/3, getWidth()*2/3, getHeight()*2/3);
            g2.drawLine(getWidth()/3, getHeight()*2/3, getWidth()*2/3, getHeight()/3);
            g2.dispose();
        }
    }

    /**
     * Field containing the text while it is being edited.
     */
    private JTextField field;
    /**
     * Panel containing the static text and close button.
     */
    private JPanel front;
    /**
     * Label containing the static text.
     */
    private JLabel label;
    /**
     * Listeners that are listening for changes to the text or the close button
     * being pressed.
     */
    private Set<ActionListener> listeners;
    /**
     * Old text before a change was made, initially <code>null</code>. This allows
     * listeners to make changes based on the old text as well as the new text.
     */
    private String old;

    /**
     * Create a new EditablePanel. This panel is "transparent" to mouse events
     * in that it should not prevent its container's mouse events from happening
     * if the mouse is over it.
     * 
     * @param title initial text to display
     * @param parent parent component to forward mouse events to, or <code>null</code>
     * if the panel should not forward mouse events.
     */
    public EditablePanel(String title, Component parent)
    {
        super();
        setLayout(new OverlayLayout(this));
        setOpaque(false);
        old = null;

        front = new JPanel(new BorderLayout());
        front.setOpaque(false);
        front.add(label = new JLabel(title), BorderLayout.WEST);
        front.add(Box.createHorizontalStrut(2), BorderLayout.CENTER);
        front.add(new CloseButton(), BorderLayout.EAST);

        add(front);
        label.setFocusable(false);

        field = new JTextField(title);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setVisible(false);
        field.addActionListener((e) -> finish(true));
        field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL);
        field.getActionMap().put(CANCEL, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                finish(false);
            }
        });
        add(field);

        label.addMouseListener(MouseListenerFactory.composeListeners(
            MouseListenerFactory.createUniversalListener((e) -> {
                if (parent != null)
                    parent.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, parent));
            }),
            MouseListenerFactory.createDoubleClickListener((e) -> {
                front.setVisible(false);
                field.setText(label.getText());
                field.setVisible(true);
                field.requestFocusInWindow();
                field.selectAll();
            })
        ));

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e)
            {
                finish(false);
			}

        });

        listeners = new HashSet<>();
    }

    /**
     * Create a new EditablePanel that does not forward mouse events.
     * 
     * @param title initial text to display
     */
    public EditablePanel(String title)
    {
        this(title, null);
    }

    /**
     * Add a new listener for changes to this EditablePanel.
     * 
     * @param listener listener to add
     */
    public void addActionListener(ActionListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Finish editing the text to display.  An action event is fired with either
     * the #{@link #EDIT} command to signify the text changed or the {@link #CANCEL}
     * command to signal it has not.
     * 
     * @param commit <code>true</code> if the text to display should be updated
     * with the contents of the text field, and <code>false</code> if it should not
     * be.
     */
    private void finish(boolean commit)
    {
        front.setVisible(true);
        field.setVisible(false);
        field.transferFocusUpCycle();
        if (commit)
        {
            old = label.getText();
            label.setText(field.getText());
            for (ActionListener listener : listeners)
                listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, EDIT));
        }
        else
        {
            field.setText(label.getText());
            for (ActionListener listener : listeners)
                listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CANCEL));
        }
    }

    /**
     * @return the previous text before editing, or <code>null</code> if it hasn't
     * been edited yet.
     */
    public String getOldTitle()
    {
        return old;
    }

    /**
     * @return the current text being displayed by this EditablePanel.
     */
    public String getTitle()
    {
        return label.getText();
    }

    /**
     * Set the text to display.  This does update the return value of the old
     * title to the text that was displayed before this was called.
     * 
     * @param title new text to display
     * @see #getOldTitle()
     */
    public void setTitle(String title)
    {
        old = label.getText();
        label.setText(title);
    }
}