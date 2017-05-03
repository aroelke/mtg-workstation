package editor.gui.inventory;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * This class represents a dialog that shows progress for downloading the
 * inventory file.  It has a label which shows how many bytes have been
 * downloaded and a progress bar that indicates things are happening.
 * 
 * TODO: Download the compressed version and uncompress it rather than downloading plain text.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryDownloadDialog extends JDialog
{
	/**
	 * This class represents a worker which downloads the inventory from a website
	 * in the background.  It is tied to a dialog which blocks input until the
	 * download is complete.
	 * 
	 * @author Alec Roelke
	 */
	private class InventoryDownloadWorker extends SwingWorker<Void, Integer>
	{
		/**
		 * Number of bytes to download.
		 */
		private String bytes;
		/**
		 * File to store the inventory file in.
		 */
		private File file;
		/**
		 * URL to download the inventory file from.
		 */
		private URL site;
		
		/**
		 * Create a new InventoryDownloadWorker.  A new one must be created each time
		 * a file is to be downloaded.
		 * 
		 * @param s URL to download the file from
		 * @param f File to store it locally in
		 */
		public InventoryDownloadWorker(URL s, File f)
		{
			super();
			site = s;
			file = f;
		}
		
		/**
		 * {@inheritDoc}
		 * Connect to the site to download the file from, and the download the file,
		 * periodically reporting how many bytes have been downloaded.
		 */
		@Override
		protected Void doInBackground() throws Exception
		{
			try
			{
				// TODO: Add ETA
				URLConnection conn = site.openConnection();
				int toDownload = conn.getContentLength();
				if (toDownload < 0)
					bytes = "";
				else if (toDownload <= 1024)
					bytes = String.format("%d", toDownload);
				else if (toDownload <= 1048576)
					bytes = String.format("%.1fk", toDownload/1024.0);
				else
					bytes = String.format("%.2fM", toDownload/1048576.0);
				SwingUtilities.invokeLater(() -> progressBar.setMaximum(toDownload));
				try (BufferedInputStream in = new BufferedInputStream((conn.getInputStream())))
				{
					try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
					{
						byte[] data = new byte[1024];
						int size = 0;
						int x;
						while ((x = in.read(data, 0, 1024)) >= 0)
						{
							size += x;
							out.write(data, 0, x);
							publish(size);
						}
					}
				}
			}
			finally
			{}
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 * Close the parent dialog and return control back to its parent.
		 */
		@Override
		protected void done()
		{
			setVisible(false);
			dispose();
		}
		
		/**
		 * {@inheritDoc}
		 * Tell the dialog how many bytes were downloaded, sometimes in kB or MB
		 * if it is too large.
		 */
		@Override
		protected void process(List<Integer> chunks)
		{
			int downloaded = chunks.get(chunks.size() - 1);
			String downloadedStr;
			if (downloaded <= 1024)
				downloadedStr = String.format("%d", downloaded);
			else if (downloaded <= 1048576)
				downloadedStr = String.format("%.1fk", downloaded/1024.0);
			else
				downloadedStr = String.format("%.2fM", downloaded/1048576.0);
			if (bytes.isEmpty())
			{
				progressBar.setVisible(false);
				progressLabel.setText("Downloading inventory..." + downloadedStr + "B downloaded.");
			}
			else
			{
				progressBar.setIndeterminate(false);
				progressBar.setValue(downloaded);
				progressLabel.setText("Downloading inventory..." + downloadedStr + "B/" + bytes + "B downloaded.");
			}
		}
	}
	
	/**
	 * Bar to look pretty and indicate things are happening.
	 */
	private JProgressBar progressBar;
	/**
	 * Label to show download statistics.
	 */
	private JLabel progressLabel;
	
	/**
	 * Worker that downloads the inventory.
	 */
	private InventoryDownloadWorker worker;
	
	/**
	 * Create a new InventoryDownloadDialog.
	 * 
	 * @param owner owner frame of the dialog.
	 */
	public InventoryDownloadDialog(JFrame owner)
	{
		super(owner, "Update", Dialog.ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(350, 115));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		worker = null;
		
		// Content panel
		JPanel contentPanel = new JPanel(new BorderLayout(0, 2));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
	 * Show this InventoryDownloadDialog and then start a worker that downloads the file.
	 * When it is complete, return the result.
	 * 
	 * @param site site to download from
	 * @param file #File to store to
	 * @return true if the download successfully completed, and false otherwise.
	 */
	public boolean downloadInventory(URL site, File file)
	{
		File tmp = new File(file.getPath() + ".tmp");
		worker = new InventoryDownloadWorker(site, tmp);
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
			JOptionPane.showMessageDialog(null, "Error downloading " + file.getName() + ": " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
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
