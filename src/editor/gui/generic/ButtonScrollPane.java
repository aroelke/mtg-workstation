package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.UIManager;

import editor.util.MouseListenerFactory;

/**
 * TODO: Comment this
 * TODO: Eventually allow this to have vertical versions as well
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ButtonScrollPane extends JPanel
{
	private static final int INITIAL_DELAY = 750;
	private static final int TICK_DELAY = 75;

	private static class ArrowButton extends JButton
	{
		public static final int EAST = 1;
		public static final int WEST = 3;

		private int direction;

		public ArrowButton(int d)
		{
			super();
			direction = d;
			setFocusable(false);
		}

		@Override
		public Dimension getPreferredSize()
		{
			Dimension original = super.getPreferredSize();
			return new Dimension(12, original.height);
		}

		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			int h = getHeight();
			int w = getWidth();
			int[] x;
			switch (direction)
			{
			case EAST:
				x = new int[] {w/3, 2*w/3, w/3};
				break;
			case WEST:
				x = new int[] {2*w/3, w/3, 2*w/3};
				break;
			default:
				throw new IllegalArgumentException("Illegal direction " + direction);
			};
			int[] y = new int[] {2*h/3, h/2, h/3};
			g.setColor(UIManager.getColor(isEnabled() ? "Button.foreground" : "Button.disabledForeground"));
			g.fillPolygon(x, y, 3);
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

		final JScrollBar bar = pane.getHorizontalScrollBar();

		ActionListener moveRight = (e) -> bar.getActionMap().get("positiveUnitIncrement").actionPerformed(
			new ActionEvent(bar, ActionEvent.ACTION_PERFORMED, "", e.getWhen(), e.getModifiers())
		);
		Timer rightTimer = new Timer(TICK_DELAY, moveRight);
		rightTimer.setInitialDelay(INITIAL_DELAY);
		right.addMouseListener(MouseListenerFactory.createHoldListener((e) -> {
			moveRight.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString(), e.getWhen(), e.getModifiersEx()));
			rightTimer.start();
		}, (e) -> rightTimer.stop()));

		ActionListener moveLeft = (e) -> bar.getActionMap().get("negativeUnitIncrement").actionPerformed(
			new ActionEvent(bar, ActionEvent.ACTION_PERFORMED, "", e.getWhen(), e.getModifiers())
		);
		Timer leftTimer = new Timer(TICK_DELAY, moveLeft);
		leftTimer.setInitialDelay(INITIAL_DELAY);
		left.addMouseListener(MouseListenerFactory.createHoldListener((e) -> {
			moveLeft.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString(), e.getWhen(), e.getModifiersEx()));
			leftTimer.start();
		}, (e) -> leftTimer.stop()));

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
