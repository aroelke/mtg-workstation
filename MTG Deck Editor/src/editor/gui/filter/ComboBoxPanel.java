package editor.gui.filter;

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
 * @param <E> Type of data the combo box holds
 */
@SuppressWarnings("serial")
public class ComboBoxPanel<E> extends JPanel
{
	/**
	 * Combo box displayed by the panel.
	 */
	private JComboBox<E> options;
	
	/**
	 * Create a new ComboBoxPanel containing a combo box with the given options.
	 * 
	 * @param items Options the combo box should display
	 */
	public ComboBoxPanel(E[] items)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalGlue());
		options = new JComboBox<E>(items);
		options.setMaximumSize(options.getPreferredSize());
		add(options);
		add(Box.createVerticalGlue());
	}
	
	/**
	 * Create a new ComboBoxPanel containing an empty combo box.
	 */
	public ComboBoxPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalGlue());
		options = new JComboBox<E>();
		options.setMaximumSize(options.getPreferredSize());
		add(options);
		add(Box.createVerticalGlue());
	}
	
	/**
	 * @return The item currently displayed by the combo box.
	 */
	public E getSelectedItem()
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
