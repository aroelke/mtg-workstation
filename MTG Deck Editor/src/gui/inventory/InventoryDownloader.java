package gui.inventory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.SwingWorker;

public class InventoryDownloader extends SwingWorker<Void, Double>
{
	private URL site;
	private File file;
	private double bytes;
	
	public InventoryDownloader(URL s, File f)
	{
		site = s;
		file = f;
		bytes = 0;
	}
	
	@Override
	protected void process(List<Double> chunks)
	{
		
	}
	
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
	
	@Override
	protected void done()
	{
		
	}
}
