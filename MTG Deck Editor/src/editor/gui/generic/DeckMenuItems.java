package editor.gui.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.AbstractList;
import java.util.function.IntConsumer;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class DeckMenuItems extends AbstractList<JMenuItem>
{
	public static final int ADD_SINGLE = 0;
	public static final int FILL_PLAYSET = 1;
	public static final int ADD_N = 2;
	public static final int REMOVE_SINGLE = 3;
	public static final int REMOVE_ALL = 4;
	public static final int REMOVE_N = 5;
	
	private JMenuItem[] items;
	
	public DeckMenuItems(Component parent, IntConsumer addN, Runnable fillPlayset, IntConsumer removeN)
	{
		items = new JMenuItem[6];
		
		// Add single copy item
		items[0] = new JMenuItem("Add Single Copy");
		items[0].addActionListener((e) -> addN.accept(1));
		
		// Fill playset item
		items[1] = new JMenuItem("Fill Playset");
		items[1].addActionListener((e) -> fillPlayset.run());
		
		// Add variable item
		items[2] = new JMenuItem("Add Copies...");
		items[2].addActionListener((e) -> {
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
			contentPanel.add(spinner, BorderLayout.SOUTH);
			if (JOptionPane.showConfirmDialog(parent, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				addN.accept((Integer)spinner.getValue());
		});
		
		// Remove single copy item
		items[3] = new JMenuItem("Remove Single Copy");
		items[3].addActionListener((e) -> removeN.accept(1));
		
		// Remove all item
		items[4] = new JMenuItem("Remove All Copies");
		items[4].addActionListener((e) -> removeN.accept(Integer.MAX_VALUE));
		
		// Remove variable item
		items[5] = new JMenuItem("Remove Copies...");
		items[5].addActionListener((e) -> {
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(new JLabel("Copies to remove:"), BorderLayout.WEST);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
			contentPanel.add(spinner, BorderLayout.SOUTH);
			if (JOptionPane.showConfirmDialog(parent, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				removeN.accept((Integer)spinner.getValue());
		});
	}

	@Override
	public JMenuItem get(int index)
	{
		return items[index];
	}

	@Override
	public int size()
	{
		return items.length;
	}
}
