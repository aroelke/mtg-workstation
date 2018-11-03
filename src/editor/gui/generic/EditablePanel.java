package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import editor.util.MouseListenerFactory;
import editor.util.UnicodeSymbols;

@SuppressWarnings("serial")
public class EditablePanel extends JPanel
{
    public static final int CLOSE_SIZE = 17;

    private class CloseButton extends JButton
    {
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
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();

            g2.setColor(getModel().isRollover() ? Color.RED : Color.BLACK);
            String cross = String.valueOf(UnicodeSymbols.MULTIPLY);
            Rectangle bounds = g2.getFont().createGlyphVector(g2.getFontRenderContext(), cross).getPixelBounds(null, 0, 0);
            int off = getModel().isPressed() ? 1 : 0;
            g2.drawString(cross, (int)(getWidth() - bounds.getWidth())/2 + off, getHeight()/2 + (int)bounds.getHeight()/2 + off - 1);
            g2.dispose();
        }
    }

    private JTextField field;
    private JPanel front;
    private JLabel label;

    public EditablePanel(String title, Component parent)
    {
        super();
        setLayout(new OverlayLayout(this));
        setOpaque(false);

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
        add(field);

        label.addMouseListener(MouseListenerFactory.composeListeners(
            MouseListenerFactory.createUniversalListener((e) -> {
                if (parent != null)
                    parent.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, parent));
            }),
            MouseListenerFactory.createDoubleClickListener((e) -> {
                front.setVisible(false);
                field.setVisible(true);
                field.requestFocusInWindow();
                field.selectAll();
            })
        ));
    }

    public EditablePanel(String title)
    {
        this(title, null);
    }

    private void finish(boolean commit)
    {
        label.setText(field.getText());
        front.setVisible(true);
        field.setVisible(false);
        field.transferFocusUpCycle();
    }
}