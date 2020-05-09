package editor.gui.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.Timer;

import editor.util.MouseListenerFactory;

/**
 * This class is a button that can repeat its action when held down after a customizable delay.
 * If the mouse is moved off the button while held down, actions will still continue until it
 * is released.  Default delays set by constructors can be overrided by using the appropriate
 * methods.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class RepeatButton extends JButton
{
    /**
     * Default delay before starting repetition.
     */
    private int defaultInitial;
    /**
     * Default delay between repetitions.
     */
    private int defaultTick;

    /**
     * Create a new RepeatButton with specified default delays.
     * 
     * @param initial default initial delay
     * @param tick default delay between repetitions
     */
    public RepeatButton(int initial, int tick)
    {
        super();
        defaultInitial = initial;
        defaultTick = tick;
    }

    /**
     * Create a new RepeatButton with the same default delay before and between repeating
     * actions.
     * 
     * @param delay default delay
     */
    public RepeatButton(int delay)
    {
        this(delay, delay);
    }

    /**
     * Create a new RepeatButton with no delay before and between repeating actions.
     */
    public RepeatButton()
    {
        this(0);
    }

    /**
     * Add an action that will be performed upon clicking the button, after an initial delay,
     * and repeated after another delay until the mouse is released.
     * 
     * @param initial delay after first pressing the mouse button to start repeating
     * @param tick delay between repetitions
     * @param l action to perform when pressing the button
     */
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

    /**
     * Add an action that will be performed upon clicking the button and repeated with a
     * specified delay as long as the button is held down.
     * 
     * @param delay delay between repetitions
     * @param l action to perform
     */
    public void addRepeatListener(int delay, ActionListener l)
    {
        addRepeatListener(delay, delay, l);
    }

    /**
     * Add an action that will be performed upon clicking the button, after an initial delay,
     * and repeated after another delay until the mouse is released. The delays are the defaults
     * set by RepeatButton's constructor.
     */
    public void addRepeatListener(ActionListener l)
    {
        addRepeatListener(defaultInitial, defaultTick, l);
    }
}