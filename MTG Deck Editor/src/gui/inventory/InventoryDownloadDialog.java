package gui.inventory;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
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
 * TODO: Enable cancellation of the download
 * TODO: Merge the dialog and worker classes
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
	 * Worker that downloads the inventory.
	 */
	private InventoryDownloadWorker worker;
	
	/**
	 * Create a new InventoryDownloadDialog.
	 * 
	 * @param owner Owner frame of the dialog.
	 */
	public InventoryDownloadDialog(JFrame owner)
	{
		super(owner, "Update", Dialog.ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(350, 115));
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		worker = null;
		
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
		
		// Cancel button
		JPanel cancelPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {
			if (worker != null)
				worker.cancel(true);
		});
		cancelPanel.add(cancelButton);
		contentPanel.add(cancelPanel, BorderLayout.SOUTH);
		
		pack();
	}
	
	/**
	 * Display the current download progress.
	 * 
	 * @param text String indicating how many bytes have been downloaded.
	 */
	public void setDownloaded(double downloaded, double toDownload)
	{
		progressBar.setIndeterminate(false);
		progressBar.setValue((int)(100*downloaded/toDownload));
		String downloadedStr, toDownloadStr;
		if (downloaded <= 1024)
			downloadedStr = String.format("%d", downloaded);
		else if (downloaded <= 1048576)
			downloadedStr = String.format("%.1fk", downloaded/1024);
		else
			downloadedStr = String.format("%.2fM", downloaded/1048576);
		if (toDownload <= 1024)
			toDownloadStr = String.format("%d", toDownload);
		else if (toDownload <= 1048576)
			toDownloadStr = String.format("%.1fk", toDownload/1024);
		else
			toDownloadStr = String.format("%.2fM", toDownload/1048576);
		progressLabel.setText("Downloading inventory..." + downloadedStr + "B/" + toDownloadStr + "B downloaded.");
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
		File tmp = new File(file.getPath() + ".tmp");
		worker = new InventoryDownloadWorker(this, site, tmp);
		worker.execute();
		setVisible(true);
		try
		{
			worker.get();
			Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch (InterruptedException | ExecutionException e)
		{
			JOptionPane.showMessageDialog(null, "Error downloading " + file.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			tmp.delete();
			return false;
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Could not replace temporary file: " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (CancellationException e)
		{
			tmp.delete();
			return false;
		}
	}
}
