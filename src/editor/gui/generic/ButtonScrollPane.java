package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import editor.util.UnicodeSymbols;

/**
 * TODO: Comment this
 * TODO: Eventually allow this to have vertical versions as well
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ButtonScrollPane extends JPanel
{
	private static class ArrowButton extends JButton
	{
		public static final int EAST = 1;
		public static final int WEST = 3;

		public ArrowButton(int direction)
		{
			super(String.valueOf(switch (direction) {
				case EAST -> UnicodeSymbols.RIGHT_ARROW;
				case WEST -> UnicodeSymbols.LEFT_ARROW;
				default -> throw new IllegalArgumentException("Illegal direction " + direction);
			}));
		}
	}

	public ButtonScrollPane(Component view)
	{
		super(new BorderLayout());

		ArrowButton left = new ArrowButton(ArrowButton.WEST);
		add(left, BorderLayout.WEST);

		JScrollPane pane = new JScrollPane(view);
		pane.setBorder(null);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(pane, BorderLayout.CENTER);

		ArrowButton right = new ArrowButton(ArrowButton.EAST);
		add(right, BorderLayout.EAST);

		JScrollBar bar = pane.getHorizontalScrollBar();
		left.addActionListener((e) -> {
			bar.getActionMap().get("negativeUnitIncrement").actionPerformed(new ActionEvent(
			bar,
			ActionEvent.ACTION_PERFORMED,
			"",
			e.getWhen(),
			e.getModifiers()));
		});
		right.addActionListener((e) -> {
			bar.getActionMap().get("positiveUnitIncrement").actionPerformed(new ActionEvent(
			bar,
			ActionEvent.ACTION_PERFORMED,
			"",
			e.getWhen(),
			e.getModifiers()));
		});

		view.addComponentListener(new ComponentListener()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				boolean scrollable = view != null && view.getPreferredSize().width > pane.getSize().width;
				left.setEnabled(scrollable);
				right.setEnabled(scrollable);
			}

			@Override
			public void componentHidden(ComponentEvent e)
			{}

			@Override
			public void componentMoved(ComponentEvent e)
			{}

			@Override
			public void componentShown(ComponentEvent e)
			{}
		});
	}
}
