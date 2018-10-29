package editor.gui.generic;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import editor.util.MouseListenerFactory;

@SuppressWarnings("serial")
public class EditablePanel extends JPanel
{
    private JLabel label;
    private JTextField field;

    public EditablePanel(String title, Component parent)
    {
        super();
        setLayout(new OverlayLayout(this));
        setOpaque(false);

        add(label = new JLabel(title));
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
                label.setVisible(false);
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
        label.setVisible(true);
        field.setVisible(false);
        field.transferFocusUpCycle();
    }
}