package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * This class is a scroll pane that is controlled with buttons on the sides rather than
 * a scroll bar on the edge. The buttons are always visible, even if the contents don't
 * fill the pane, but are disabled until scrolling is necessary.
 * 
 * TODO: Eventually allow this to have vertical versions as well
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ButtonScrollPane extends JPanel
{
	/**
	 * Initial delay when holding down an arrow button before auto-scrolling.
	 */
	private static final int INITIAL_DELAY = 750;
	/**
	 * Delay between scroll actions when holding down an arrow button.
	 */
	private static final int TICK_DELAY = 75;

	/**
	 * A button with an arrow icon pointing in the direction it will scroll the pane.
	 * 
	 * @author Alec Roelke
	 */
	private static class ArrowButton extends RepeatButton
	{
		/**
		 * East-facing (right) arrow.
		 */
		public static final int EAST = 1;
		/**
		 * West-facing (left) arrow.
		 */
		public static final int WEST = 3;

		/**
		 * Direction the button will face.
		 */
		private int direction;

		/**
		 * Create a new arrow button facing the specified direction.
		 * 
		 * @param d direction the arrow button will face (EAST or WEST)
		 */
		public ArrowButton(int d)
		{
			super(INITIAL_DELAY, TICK_DELAY);
			direction = d;
			setFocusable(false);
		}

		/**
		 * @inheritDoc
		 * Arrow buttons have a constant size on one of their sides so they are
		 * thin.
		 */
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

	/**
	 * Create a new ButtonScrollPane containing the given component. Add things to that
	 * component, not to this pane.
	 * 
	 * @param view component in this ButtonScrollPane's view
	 */
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
		left.addRepeatListener((e) -> bar.getActionMap().get("negativeUnitIncrement").actionPerformed(
			new ActionEvent(bar, ActionEvent.ACTION_PERFORMED, "", e.getWhen(), e.getModifiers())
		));
		right.addRepeatListener((e) -> bar.getActionMap().get("positiveUnitIncrement").actionPerformed(
			new ActionEvent(bar, ActionEvent.ACTION_PERFORMED, "", e.getWhen(), e.getModifiers())
		));

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
