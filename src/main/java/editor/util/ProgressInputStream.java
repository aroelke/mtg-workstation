package editor.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

/**
 * This class represents an input stream that reports how many bytes it has
 * read.
 *
 * @author Alec Roelke
 */
public class ProgressInputStream extends FilterInputStream
{
    /**
     * Support for property changes.  Reports how many bytes have been read
     * whenever bytes are read.
     */
    private PropertyChangeSupport propertySupport;
    /**
     * Number of bytes that have been read.
     */
    private long totalRead;

    /**
     * Create a ProgressInputStream tracking the given #InputStream and reporting
     * its progress.
     *
     * @param in stream to track
     */
    public ProgressInputStream(InputStream in)
    {
        super(in);
        propertySupport = new PropertyChangeSupport(this);
        totalRead = 0;
    }

    /**
     * Create a ProgressInputStream tracking the given #InputStream and reporting
     * its progress using the given function.
     * 
     * @param in stream to track
     * @param listener how to report progress reading data; the first argument
     * is the amount of data read before calling and the second argument is
     * the new amount (the difference being the number of bytes read)
     */
    public ProgressInputStream(InputStream in, BiConsumer<Long, Long> listener)
    {
        this(in);
        addPropertyChangeListener((e) -> {
            if (e.getPropertyName().equals("bytesRead"))
                listener.accept((Long)e.getOldValue(), (Long)e.getNewValue());
        });
    }

    /**
     * Add a listener for changes to the number of bytes read.  This will be reported
     * in the property "bytesRead."
     *
     * @param listener listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a listener for changes to the number of bytes read.
     *
     * @param listener listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.removePropertyChangeListener(listener);
    }

    @Override
    public void mark(int readlimit)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    @Override
    public int read() throws IOException
    {
        int r = super.read();
        update(1);
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int r = super.read(b, off, len);
        update(r);
        return r;
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * If any bytes have been read, report the new total.
     *
     * @param read bytes that were read
     */
    private void update(int read)
    {
        if (read > 0)
        {
            long old = totalRead;
            totalRead += read;
            propertySupport.firePropertyChange("bytesRead", old, totalRead);
        }
    }
}
