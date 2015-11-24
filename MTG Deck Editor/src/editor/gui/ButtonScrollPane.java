package editor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * TODO: Comment this
 * TODO: Eventually allow this to have vertical versions as well
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ButtonScrollPane extends JPanel
{
	private static final int INITIAL_DELAY = 500;
	private static final int TICK_DELAY = 50;
	
	private JScrollBar bar;
	
	public ButtonScrollPane(Component view)
	{
		super(new BorderLayout());
		
		BasicArrowButton left = new BasicArrowButton(BasicArrowButton.WEST);
		add(left, BorderLayout.WEST);
		
		JScrollPane pane = new JScrollPane(view);
		pane.setBorder(null);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(pane, BorderLayout.CENTER);
		
		BasicArrowButton right = new BasicArrowButton(BasicArrowButton.EAST);
		add(right, BorderLayout.EAST);
		
		bar = pane.getHorizontalScrollBar();
		bar.setUnitIncrement(10);
		Timer leftTimer = new Timer(TICK_DELAY, e -> {
			bar.getActionMap().get("negativeUnitIncrement").actionPerformed(new ActionEvent(bar,
					ActionEvent.ACTION_PERFORMED,
					"",
					e.getWhen(),
					e.getModifiers()));
		});
		leftTimer.setInitialDelay(INITIAL_DELAY);
		left.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (left.isEnabled())
				{
					leftTimer.restart();
					bar.getActionMap().get("negativeUnitIncrement").actionPerformed(new ActionEvent(bar,
							ActionEvent.ACTION_PERFORMED,
							"",
							e.getWhen(),
							e.getModifiers()));
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				leftTimer.stop();
			}
		});
		Timer rightTimer = new Timer(TICK_DELAY, e -> {
			bar.getActionMap().get("positiveUnitIncrement").actionPerformed(new ActionEvent(
					bar,
					ActionEvent.ACTION_PERFORMED,
					"",
					e.getWhen(),
					e.getModifiers()));
		});
		rightTimer.setInitialDelay(INITIAL_DELAY);
		right.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (right.isEnabled())
				{
					rightTimer.restart();
					bar.getActionMap().get("positiveUnitIncrement").actionPerformed(new ActionEvent(bar,
							ActionEvent.ACTION_PERFORMED,
							"",
							e.getWhen(),
							e.getModifiers()));
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				rightTimer.stop();
			}
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
	
	public void setUnitIncrement(int unitIncrement)
	{
		bar.setUnitIncrement(unitIncrement);
	}
}
