package gui.filter;

import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * This class represents a panel containing a JComboBox that is vertically centered
 * and minimum height to fit the data contained within.  Horizontally, the panel shrinks
 * to fit the combo box.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of data the combo box holds
 */
@SuppressWarnings("serial")
public class ComboBoxPanel<T> extends JPanel
{
	/**
	 * Combo box displayed by the panel.
	 */
	private JComboBox<T> options;
	
	/**
	 * Create a new ComboBoxPanel containing a combo box with the given options.
	 * 
	 * @param t Options the combo box should display
	 */
	public ComboBoxPanel(T[] t)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalGlue());
		options = new JComboBox<T>(t);
		options.setMaximumSize(options.getPreferredSize());
		add(options);
		add(Box.createVerticalGlue());
	}
	
	/**
	 * @return The item currently displayed by the combo box.
	 */
	public T getSelectedItem()
	{
		return options.getItemAt(options.getSelectedIndex());
	}
	
	/**
	 * @param item Item to set the combo box to.
	 */
	public void setSelectedItem(Object item)
	{
		options.setSelectedItem(item);
	}
	
	/**
	 * Add an event that should occur when the comob box's item changes.
	 * 
	 * @param aListener Listener that should listen for when the combo box's
	 * item is changed.
	 */
	public void addItemListener(ItemListener aListener)
	{
		options.addItemListener(aListener);
	}
}
