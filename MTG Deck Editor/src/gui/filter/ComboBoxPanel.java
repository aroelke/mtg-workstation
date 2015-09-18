package gui.filter;

import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * TODO: Comment this
 * @author Alec
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ComboBoxPanel<T> extends JPanel
{
	private JComboBox<T> options;
	
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
	
	public T getSelectedItem()
	{
		return options.getItemAt(options.getSelectedIndex());
	}
	
	public void setSelectedItem(Object item)
	{
		options.setSelectedItem(item);
	}
	
	public void addItemListener(ItemListener aListener)
	{
		options.addItemListener(aListener);
	}
}
