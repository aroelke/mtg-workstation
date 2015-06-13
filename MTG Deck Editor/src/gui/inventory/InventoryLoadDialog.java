package gui.inventory;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import database.Card;
import database.Inventory;

/**
 * This class represents a dialog that shows the progress for loading the
 * inventory and blocking the main frame until it is finished.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryLoadDialog extends JDialog
{
	/**
	 * Label showing the current stage of loading.
	 */
	private JLabel progressLabel;
	/**
	 * Progress bar showing overall progress of loading.
	 */
	private JProgressBar progressBar;
	/**
	 * Area showing past and current progress of loading.
	 */
	private JTextArea progressArea;
	
	public InventoryLoadDialog(JFrame owner)
	{
		super(owner, "Loading Inventory", Dialog.ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(350, 200));
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		// Content panel
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0};
		layout.columnWeights = new double[] {1.0};
		layout.rowHeights = new int[] {0, 0, 0};
		layout.rowWeights = new double[] {0.0, 0.0, 1.0};
		JPanel contentPanel = new JPanel(layout);
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPanel);
		
		// Stage progress label
		progressLabel = new JLabel("Loading inventory...");
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.anchor = GridBagConstraints.WEST;
		labelConstraints.fill = GridBagConstraints.BOTH;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.insets = new Insets(0, 0, 2, 0);
		contentPanel.add(progressLabel, labelConstraints);
		
		// Overall progress bar
		progressBar = new JProgressBar();
		GridBagConstraints barConstraints = new GridBagConstraints();
		barConstraints.fill = GridBagConstraints.BOTH;
		barConstraints.gridx = 0;
		barConstraints.gridy = 1;
		barConstraints.insets = new Insets(0, 0, 2, 0);
		contentPanel.add(progressBar, barConstraints);
		
		// History text area
		progressArea = new JTextArea();
		progressArea.setEditable(false);
		GridBagConstraints areaConstraints = new GridBagConstraints();
		areaConstraints.fill = GridBagConstraints.BOTH;
		areaConstraints.gridx = 0;
		areaConstraints.gridy = 2;
		contentPanel.add(new JScrollPane(progressArea), areaConstraints);
		
		// Set the mouse to the wait pointer while this is open
		// TODO: Figure out a way for this to work for the main frame (which may be difficult, as others have not)
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			
			@Override
			public void windowClosed(WindowEvent e)
			{
				setCursor(Cursor.getDefaultCursor());
			}
		});
		
		pack();
	}
	
	/**
	 * Set the value of the progress bar.
	 * 
	 * @param i New value of the progress bar; should be in [0, 100].
	 */
	public void setValue(int i)
	{
		progressBar.setValue(i);
	}
	
	/**
	 * Set whether or not the progress bar is indeterminate.  If it is, then it will
	 * not fill up.
	 * 
	 * @param indeterminate Whether or not the progress bar is indeterminate
	 */
	public void setIndeterminate(boolean indeterminate)
	{
		progressBar.setIndeterminate(indeterminate);
	}
	
	/**
	 * Set the stage of loading.
	 * 
	 * @param s String showing what stage loading is in
	 */
	public void setStage(String s)
	{
		progressLabel.setText(s);
		progressArea.append(s + "\n");
	}
	
	/**
	 * Make this dialog visible and then begin loading the inventory.  Block until it is
	 * complete, and then return the newly-created Inventory.
	 * 
	 * @return The Inventory that was created.
	 */
	public Inventory createInventory(File file)
	{
		InventoryLoadWorker worker = new InventoryLoadWorker(this, file);
		worker.execute();
		setVisible(true);
		progressArea.setText("");
		try
		{
			return worker.get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			return new Inventory(new ArrayList<Card>());
		}
	}
}
