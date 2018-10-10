package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.export.CardListFormat;
import editor.database.card.Card;
import editor.gui.MainFrame;
import editor.util.ProgressInputStream;

/**
 * This class controls the serialization and deserialization of a #Deck.  It can
 * serialize a deck, a sideboard, and a changelog, deserialize them, and import
 * a deck from an external file type.  Once a deck has been loaded, another one
 * cannot be loaded by the same instance of this class.
 * 
 * @author Alec Roelke
 */
public class DeckSerializer
{
    /**
     * Format to display dates for changes made to a deck.
     */
    public static final SimpleDateFormat CHANGELOG_DATE = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
    /**
     * Latest version of save file.
     * 
     * Change log:
     * 1. Added save version number
     * 2. Switched changelog from read/writeObject to read/writeUTF
     * 3. Allow multiple sideboards
     */
    private static final long SAVE_VERSION = 3;

    /**
     * This class is a worker for loading a deck.
     *
     * @author Alec Roelke
     */
    private class LoadWorker extends SwingWorker<Void, Integer>
    {
        /**
         * Dialog containing the progress bar.
         */
        private JDialog dialog;
        /**
         * File to load the deck from.
         */
        private File file;
        /**
         * Progress bar to display progress to.
         */
        private JProgressBar progressBar;

        /**
         * Create a new LoadWorker.
         *
         * @param f file to load the deck from
         * @param v file to load contains save version number (only false if importing from before versions were implemented)
         * @param b progress bar showing progress
         * @param d dialog containing the progress bar
         */
        public LoadWorker(File f, JProgressBar b, JDialog d)
        {
            file = f;
            progressBar = b;
            dialog = d;

            progressBar.setMaximum((int)file.length());
        }

        /**
         * {@inheritDoc}
         * Load the deck, updating the progress bar all the while.
         */
        @Override
        protected Void doInBackground() throws Exception
        {
            long version;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file)))
            {
                version = ois.readLong();
            }
            // Assume that high bits in the first 64 bits are used by the serialization of a Deck
            // object and that SAVE_VERSION will never be that high.
            if (version > SAVE_VERSION)
                version = 0;

            try (ProgressInputStream pis = new ProgressInputStream(new FileInputStream(file)))
            {
                pis.addPropertyChangeListener((e) -> process(Collections.singletonList(((Long)e.getNewValue()).intValue())));
                try (ObjectInputStream ois = new ObjectInputStream(pis))
                {
                    if (version > 0)
                        ois.readLong(); // Throw out first 64 bits that have already been read
                    deck = readDeck(ois);
                    if (version <= 2)
                        sideboard.put("Sideboard", readDeck(ois));
                    else
                    {
                        int boards = ois.readInt();
                        for (int i = 0; i < boards; i++)
                        {
                            String name = ois.readUTF();
                            sideboard.put(name, readDeck(ois));
                        }
                    }
                    if (version < 2)
                        changelog = (String)ois.readObject();
                    else
                        changelog = ois.readUTF();
                }
            }
            return null;
        }

        @Override
        protected void done()
        {
            dialog.dispose();
        }

        @Override
        protected void process(List<Integer> chunks)
        {
            progressBar.setValue(chunks.get(chunks.size() - 1));
        }
    }

    /**
     * Changelog of the loaded deck.
     */
    private String changelog;

    /**
     * The loaded deck.
     */
    private Deck deck;

    /**
     * File to load the deck from or that the deck has been loaded from.
     */
    private File file;

    /**
     * Whether or not the deck was imported from an external file type.
     */
    private boolean imported;

    /**
     * Sideboard for the loaded deck.
     */
    private Map<String, Deck> sideboard;

    /**
     * Create a new, empty DeckSerializer.  Use this to load a deck.
     */
    public DeckSerializer()
    {
        reset();
    }

    /**
     * Create a new DeckSerializer with the given deck, sideboard, and changelog
     * already loaded.  This cannot be used to load a deck, so use it to save one.
     */
    public DeckSerializer(Deck d, Map<String, Deck> s, String c)
    {
        this();
        changelog = c;
        deck = d;
        sideboard = new LinkedHashMap<>(s);
    }

    /**
     * @return <code>true</code> if the file that was used to open the deck can
     * be saved to, which is if it is defined and is of the native format for
     * this editor, and <code>false</code> otherwise.
     */
    public boolean canSaveFile()
    {
        return file != null && !imported;
    }

    /**
     * @return the changelog for the loaded deck.
     */
    public String changelog()
    {
        return changelog;
    }

    /**
     * @return the loaded deck.
     */
    public Deck deck()
    {
        return deck;
    }

    /**
     * @return the File corresponding to the saved or loaded deck.
     */
    public File file()
    {
        if (file == null)
            throw new NoSuchElementException("file not saved or loaded");
        return file;
    }

    /**
     * Import a list of cards from a nonstandard file.  If an error occurs during import,
     * this serializer is reset to an empty state.
     *
     * @param format format of the file
     * @param file   file to import from
     * @throws DeckLoadException if the deck could not be imported
     * @see CardListFormat
     */
    public void importList(CardListFormat format, File file) throws DeckLoadException
    {
        if (!deck.isEmpty())
            throw new DeckLoadException(file, "deck already loaded");
        try
        {
            deck.addAll(format.parse(String.join(System.lineSeparator(), Files.readAllLines(file.toPath()))));
            imported = true;
        }
        catch (Exception e)
        {
            reset();
            throw new DeckLoadException(file, e);
        }
    }

    /**
     * Load a deck from a native file type.  If an error occurs during loading the deck, this serializer
     * is reset to an empty state.
     * 
     * @param f File to load from
     * @param parent parent window used to display errors
     * @throws CancellationException if loading the deck was canceled
     * @throws DeckLoadException if there is already a loaded deck
     */
    public void load(File f, Window parent) throws CancellationException, DeckLoadException
    {
        if (!deck.isEmpty())
            throw new DeckLoadException(file, "deck already loaded");

        JDialog progressDialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);
        JProgressBar progressBar = new JProgressBar();
        LoadWorker worker = new LoadWorker(f, progressBar, progressDialog);

        JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
        progressDialog.setContentPane(progressPanel);
        progressPanel.add(new JLabel("Opening " + f.getName() + "..."), BorderLayout.NORTH);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> worker.cancel(false));
        cancelPanel.add(cancelButton);
        progressPanel.add(cancelPanel, BorderLayout.SOUTH);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(parent);

        worker.execute();
        progressDialog.setVisible(true);
        try
        {
            worker.get();
        }
        catch (CancellationException e)
        {
            reset();
            throw e;
        }
        catch (Exception e)
        {
            reset();
            throw new DeckLoadException(file, e);
        }
        file = f;
    }

    /**
     * Read a deck from an object stream.
     * 
     * @param in input stream to read from
     * @return the Deck that was read
     */
    private Deck readDeck(ObjectInput in) throws IOException, ClassNotFoundException
    {
        Deck d = new Deck();
        int n = in.readInt();
        for (int i = 0; i < n; i++)
        {
            Card card = MainFrame.inventory().get(in.readUTF());
            int count = in.readInt();
            LocalDate added = (LocalDate)in.readObject();
            d.add(card, count, added);
        }
        n = in.readInt();
        for (int i = 0; i < n; i++)
        {
            CategorySpec spec = new CategorySpec();
            spec.readExternal(in);
            d.addCategory(spec, in.readInt());
        }
        return d;
    }

    /**
     * Clear the contents of this DeckSerializer so it can be reused.
     */
    private void reset()
    {
        changelog = "";
        deck = new Deck();
        file = null;
        sideboard = new LinkedHashMap<>();
        imported = false;
    }

    /**
     * Save the deck to the given file.
     *
     * @param f file to save to
     * @throws IOException if the file could not be saved
     */
    public void save(File f) throws IOException
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f, false)))
        {
            oos.writeLong(SAVE_VERSION);
            writeDeck(deck, oos);
            oos.writeInt(sideboard.size());
            for (Map.Entry<String, Deck> sb : sideboard.entrySet())
            {
                oos.writeUTF(sb.getKey());
                writeDeck(sb.getValue(), oos);
            }
            oos.writeUTF(changelog);
            file = f;
        }
    }

    /**
     * @return the sideboards of the loaded deck.
     */
    public Map<String, Deck> sideboards()
    {
        return sideboard;
    }

    /**
     * Write a deck to an output stream.
     * 
     * @param deck deck to write
     * @param out stream to write to
     */
    private void writeDeck(Deck deck, ObjectOutput out) throws IOException
    {
        out.writeInt(deck.size());
        for (Card card : deck)
        {
            out.writeUTF(card.id());
            out.writeInt(deck.getData(card).count());
            out.writeObject(deck.getData(card).dateAdded());
        }
        out.writeInt(deck.numCategories());
        for (CategorySpec spec : deck.categories())
        {
            spec.writeExternal(out);
            out.writeInt(deck.getCategoryRank(spec.getName()));
        }
    }
}