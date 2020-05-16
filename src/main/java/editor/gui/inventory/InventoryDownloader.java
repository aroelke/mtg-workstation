package editor.gui.inventory;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public abstract class InventoryDownloader
{
    public static boolean downloadInventory(Frame owner, URL site, File file) throws IOException
    {
        File zip = new File(file.getPath() + ".zip");
        File tmp = new File(zip.getPath() + ".tmp");

        JDialog dialog = new JDialog(owner, "Update", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setPreferredSize(new Dimension(350, 115));
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        UnzipWorker unzipper = new UnzipWorker(zip, file);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 2));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.setContentPane(contentPanel);

        // Stage progress label
        JLabel progressLabel = new JLabel("Downloading inventory...");
        contentPanel.add(progressLabel, BorderLayout.NORTH);

        // Overall progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        contentPanel.add(progressBar, BorderLayout.CENTER);

        DownloadWorker downloader = new DownloadWorker(site, tmp, () -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        if (downloader.size() >= 0)
            progressBar.setMaximum(downloader.size());
        downloader.setUpdateFunction((downloaded) -> {
            StringBuilder progress = new StringBuilder();
            progress.append("Downloading inventory ..." + formatDownload(downloaded));
            if (downloader.size() < 0)
                progressBar.setVisible(false);
            else
            {
                progressBar.setIndeterminate(false);
                progressBar.setValue(downloaded);
                progress.append("B/" + formatDownload(downloader.size()));
            }
            progress.append("B downloaded.");
            progressLabel.setText(progress.toString());
        });

        // Cancel button
        JPanel cancelPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> {
            downloader.cancel(true);
            unzipper.cancel(true);
        });
        cancelPanel.add(cancelButton);
        contentPanel.add(cancelPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        downloader.execute();
        dialog.setVisible(true);
        try
        {
            downloader.get();
            Files.move(tmp.toPath(), zip.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (InterruptedException | ExecutionException e)
        {
            JOptionPane.showMessageDialog(null, "Error downloading " + zip.getName() + ": " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
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

        progressLabel.setText("Unzipping archive...");
        unzipper.execute();
        try
        {
            unzipper.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            JOptionPane.showMessageDialog(null, "Error decompressing " + zip.getName() + ": " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (CancellationException e)
        {
            return false;
        }
        finally
        {
            zip.delete();
        }
        return true;
    }

    private static String formatDownload(int n)
    {
        if (n < 0)
            return "";
        else if (n <= 1024)
            return String.format("%d", n);
        else if (n <= 1048576)
            return String.format("%.1fk", n/1024.0);
        else
            return String.format("%.2fM", n/1048576.0);
    }

    /**
     * This class represents a worker which downloads the inventory from a website
     * in the background.  It is tied to a dialog which blocks input until the
     * download is complete.
     *
     * @author Alec Roelke
     */
    private static class DownloadWorker extends SwingWorker<Void, Integer>
    {
        /** File to store the inventory file in. */
        private File file;
        /** Connection to the inventory site. */
        private URLConnection connection;
        /** Number of bytes to download from the inventory site. */
        private int size;
        /** Function for updating the GUI with the number of bytes downloaded. */
        private Consumer<Integer> updater;
        private Runnable finished;

        /**
         * Create a new InventoryDownloadWorker.  A new one must be created each time
         * a file is to be downloaded.
         *
         * @param s URL to download the file from
         * @param f File to store it locally in
         */
        public DownloadWorker(URL s, File f, Runnable d) throws IOException
        {
            super();
            file = f;
            connection = s.openConnection();
            size = connection.getContentLength();
            updater = (i) -> {};
            finished = d;
        }

        public int size()
        {
            return size;
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
                try (BufferedInputStream in = new BufferedInputStream((connection.getInputStream())))
                {
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
                    {
                        byte[] data = new byte[1024];
                        int size = 0;
                        int x;
                        while ((x = in.read(data)) > 0)
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
            finished.run();
        }

        public void setUpdateFunction(Consumer<Integer> u)
        {
            updater = u;
        }

        /**
         * {@inheritDoc}
         * Tell the dialog how many bytes were downloaded, sometimes in kB or MB
         * if it is too large.
         */
        @Override
        protected void process(List<Integer> chunks)
        {
            updater.accept(chunks.get(chunks.size() - 1));
        }
    }

    /**
     * This class represents a worker which unzips a zipped archive of the inventory.
     *
     * @author Alec Roelke
     */
    private static class UnzipWorker extends SwingWorker<Void, Void>
    {
        /**
         * Zip file containing the inventory.
         */
        private File zipfile;
        /**
         * File to write the unzipped inventory to.
         */
        private File outfile;

        /**
         * Create a new InventoryUnzipWorker to unzip a file.
         *
         * @param z file to unzip
         * @param o file to store the result to
         */
        public UnzipWorker(File z, File o)
        {
            zipfile = z;
            outfile = o;
        }

        /**
         * {@inheritDoc}
         * Open the zip file, decompress it, and store the result back to disk.
         */
        @Override
        protected Void doInBackground() throws Exception
        {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipfile)))
            {
                zis.getNextEntry();
                try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile)))
                {
                    byte[] data = new byte[1024];
                    int x;
                    while ((x = zis.read(data)) > 0)
                        out.write(data, 0, x);
                }
            }
            return null;
        }
    }
}