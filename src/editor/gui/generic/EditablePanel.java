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
 * TODO
 */
@SuppressWarnings("serial")
public class EditablePanel extends JPanel
{
    public static final String CLOSE = "close";
    public static final String EDIT = "edit";
    public static final String CANCEL = "cancel";

    private class CloseButton extends JButton
    {
        private static final int CLOSE_SIZE = 17;

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

    private JTextField field;
    private JPanel front;
    private JLabel label;
    private Set<ActionListener> listeners;
    private String old;

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

    public EditablePanel(String title)
    {
        this(title, null);
    }

    public void addActionListener(ActionListener listener)
    {
        listeners.add(listener);
    }

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

    public String getOldTitle()
    {
        return old;
    }

    public String getTitle()
    {
        return label.getText();
    }

    public void setTitle(String title)
    {
        old = label.getText();
        label.setText(title);
    }
}