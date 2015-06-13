package gui.inventory;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/**
 * This class represents a dialog that shows progress for downloading the
 * inventory file.  It has a label which shows how many bytes have been
 * downloaded and a progress bar that indicates things are happening.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryDownloadDialog extends JDialog
{
	/**
	 * Label to show download statistics.
	 */
	private JLabel progressLabel;
	/**
	 * Bar to look pretty and indicate things are happening.
	 */
	private JProgressBar progressBar;
	
	/**
	 * Create a new InventoryDownloadDialog.
	 * 
	 * @param owner Owner frame of the dialog.
	 */
	public InventoryDownloadDialog(JFrame owner)
	{
		super(owner, "Update", Dialog.ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(350, 80));
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		// Content panel
		JPanel contentPanel = new JPanel(new BorderLayout(0, 2));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPanel);
		
		// Stage progress label
		contentPanel.add(progressLabel = new JLabel("Downloading inventory..."), BorderLayout.NORTH);
		
		// Overall progress bar
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		contentPanel.add(progressBar, BorderLayout.CENTER);
		
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
	 * Display the current download progress.
	 * 
	 * @param text String indicating how many bytes have been downloaded.
	 */
	public void setDownloaded(String text)
	{
		progressLabel.setText("Downloading inventory..." + text + " bytes downloaded.");
	}
	
	/**
	 * Show this InventoryDownloadDialog and then start a worker that downloads the file.
	 * When it is complete, return the result.
	 * 
	 * @param site Site to download from
	 * @param file File to store to
	 * @return <code>true</code> if the download successfully completed, and <code>false</code>
	 * otherwise.
	 */
	public boolean downloadInventory(URL site, File file)
	{
		InventoryDownloadWorker worker = new InventoryDownloadWorker(this, site, file);
		worker.execute();
		setVisible(true);
		try
		{
			worker.get();
			return true;
		}
		catch (InterruptedException | ExecutionException e)
		{
			JOptionPane.showMessageDialog(null, "Error downloading " + file.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
