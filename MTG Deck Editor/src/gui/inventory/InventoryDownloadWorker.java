package gui.inventory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * This class represents a worker which downloads the inventory from a website
 * in the background.  It is tied to a dialog which blocks input until the
 * download is complete.
 * 
 * @author Alec Roelke
 */
public class InventoryDownloadWorker extends SwingWorker<Void, Double>
{
	/**
	 * Parent dialog to show progress.
	 */
	private InventoryDownloadDialog parent;
	/**
	 * URL to download the inventory file from.
	 */
	private URL site;
	/**
	 * File to store the inventory file in.
	 */
	private File file;
	/**
	 * Number of bytes to download.
	 */
	private double bytes;
	
	/**
	 * Create a new InventoryDownloadWorker.  A new one must be created each time
	 * a file is to be downloaded.
	 * 
	 * @param p Parent dialog to show this worker's progress
	 * @param s URL to download the file from
	 * @param f File to store it locally in
	 */
	public InventoryDownloadWorker(InventoryDownloadDialog p, URL s, File f)
	{
		super();
		parent = p;
		site = s;
		file = f;
		bytes = 0;
	}
	
	/**
	 * Tell the dialog how many bytes were downloaded, sometimes in kB or MB
	 * if it is too large.
	 */
	@Override
	protected void process(List<Double> chunks)
	{
		parent.setDownloaded(chunks.get(chunks.size() - 1), bytes);
	}
	
	/**
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
			bytes = conn.getContentLengthLong();
			try (BufferedInputStream in = new BufferedInputStream((conn.getInputStream())))
			{
				try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
				{
					byte[] data = new byte[1024];
					double size = 0;
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
	 * When done, close the parent dialog and return control back to its parent.
	 */
	@Override
	protected void done()
	{
		parent.setVisible(false);
		parent.dispose();
	}
}
