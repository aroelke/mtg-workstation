package editor.gui.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.Timer;

import editor.util.MouseListenerFactory;

@SuppressWarnings("serial")
public class RepeatButton extends JButton
{
    private int defaultInitial;
    private int defaultTick;

    public RepeatButton(int initial, int tick)
    {
        super();
        defaultInitial = initial;
        defaultTick = tick;
    }

    public RepeatButton(int delay)
    {
        this(delay, delay);
    }

    public RepeatButton()
    {
        this(0);
    }

    public void addRepeatListener(int initial, int tick, ActionListener l)
    {
        Timer timer = new Timer(tick, l);
        timer.setRepeats(true);
        timer.setInitialDelay(initial);
        addMouseListener(MouseListenerFactory.createHoldListener((e) -> {
            l.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString(), e.getWhen(), e.getModifiersEx()));
            timer.start();
        }, (e) -> timer.stop()));
    }

    public void addRepeatListener(int delay, ActionListener l)
    {
        addRepeatListener(delay, delay, l);
    }

    public void addRepeatListener(ActionListener l)
    {
        addRepeatListener(defaultInitial, defaultTick, l);
    }
}