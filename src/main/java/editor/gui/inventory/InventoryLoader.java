package editor.gui.inventory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import editor.collection.Inventory;
import editor.database.FormatConstraints;
import editor.database.attributes.CombatStat;
import editor.database.attributes.Expansion;
import editor.database.attributes.Legality;
import editor.database.attributes.Loyalty;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.attributes.Rarity;
import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.card.FlipCard;
import editor.database.card.MeldCard;
import editor.database.card.SingleCard;
import editor.database.card.SplitCard;
import editor.database.card.TransformCard;
import editor.database.version.DatabaseVersion;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.filter.leaf.options.multi.SubtypeFilter;
import editor.filter.leaf.options.multi.SupertypeFilter;
import editor.gui.MainFrame;
import editor.gui.settings.SettingsDialog;

/**
 * Worker that loads the JSON inventory file into memory and displays progress in a
 * popup dialog.
 * 
 * @author Alec Roelke
 */
public class InventoryLoader extends SwingWorker<Inventory, String>
{
    private static final DatabaseVersion VER_5_0_0 = new DatabaseVersion(5, 0, 0);

    /**
     * Load the inventory into memory from disk. Display a dialog indicating showing progress
     * and allowing cancellation.
     * 
     * @param owner frame for setting the location of the dialog
     * @param file file to load the inventory from
     */
    public static Inventory loadInventory(Frame owner, File file)
    {
        JDialog dialog = new JDialog(owner, "Loading Inventory", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final int BORDER = 10;

        // Content panel
        Box contentPanel = new Box(BoxLayout.Y_AXIS);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        dialog.setContentPane(contentPanel);

        // Stage progress label
        JLabel progressLabel = new JLabel("Loading inventory...");
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(progressLabel);
        contentPanel.add(Box.createVerticalStrut(2));

        // Overall progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(progressBar);
        contentPanel.add(Box.createVerticalStrut(2));

        // History text area
        JTextArea progressArea = new JTextArea("", 6, 40);
        progressArea.setEditable(false);
        JScrollPane progressPane = new JScrollPane(progressArea);
        progressPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(progressPane);
        contentPanel.add(Box.createVerticalStrut(BORDER));

        InventoryLoader loader = new InventoryLoader(file, (c) -> {
            progressLabel.setText(c);
            progressArea.append(c + "\n");
        }, () -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        loader.addPropertyChangeListener((e) -> {
            if ("progress".equals(e.getPropertyName()))
            {
                int p = (Integer)e.getNewValue();
                progressBar.setIndeterminate(p < 0);
                progressBar.setValue(p);
            }
        });

        // Cancel button
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> loader.cancel(false));
        cancelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelPanel.add(cancelButton);
        contentPanel.add(cancelPanel);

        dialog.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                loader.cancel(false);
            }
        });
        dialog.getRootPane().registerKeyboardAction((e) -> {
            loader.cancel(false);
            dialog.setVisible(false);
            dialog.dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.pack();
        loader.execute();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        Inventory result = new Inventory();
        try
        {
            result = loader.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            JOptionPane.showMessageDialog(owner, "Error loading inventory: " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        catch (CancellationException e)
        {}
        if (SettingsDialog.settings().inventory.warn && !loader.warnings().isEmpty())
        {
            SwingUtilities.invokeLater(() -> {
                StringJoiner join = new StringJoiner("<li>", "<html>", "</ul></html>");
                join.add("Errors ocurred while loading the following card(s):<ul style=\"margin-top:0;margin-left:20pt\">");
                for (String failure : loader.warnings())
                    join.add(failure);
                JPanel warningPanel = new JPanel(new BorderLayout());
                JLabel warningLabel = new JLabel(join.toString());
                warningPanel.add(warningLabel, BorderLayout.CENTER);
                JCheckBox suppressBox = new JCheckBox("Don't show this warning in the future", !SettingsDialog.settings().inventory.warn);
                warningPanel.add(suppressBox, BorderLayout.SOUTH);
                JOptionPane.showMessageDialog(null, warningPanel, "Warning", JOptionPane.WARNING_MESSAGE);
                SettingsDialog.setShowInventoryWarnings(!suppressBox.isSelected());
            });
        }
        SettingsDialog.setInventoryWarnings(loader.warnings());
        return result;
    }

    /** File to load from. */
    private File file;
    /** List of errors that occur during loading. */
    private List<String> errors;
    /** Action to perform on each chunk during process(). */
    private Consumer<String> consumer;
    /** Function to perform when done loading. */
    private Runnable finished;

    /**
     * Create a new InventoryWorker.
     *
     * @param f #File to load
     * @param c function to perform on each update
     * @param d function to perform when done loading
     */
    private InventoryLoader(File f, Consumer<String> c, Runnable d)
    {
        super();
        file = f;
        consumer = c;
        errors = new ArrayList<>();
        finished = d;
    }

    /**
     * Convert a card that has a single face but incorrectly is loaded as a
     * multi-faced card into a card with a {@link CardLayout#NORMAL} layout.
     * 
     * @param card card to convert
     * @return a {@link Card} with the same information as the input but a
     * {@link CardLayout#NORMAL} layout.
     */
    private Card convertToNormal(Card card)
    {
        return new SingleCard(
            CardLayout.NORMAL,
            card.name().get(0),
            card.manaCost().get(0),
            card.colors(),
            card.colorIdentity(),
            card.supertypes(),
            card.types(),
            card.subtypes(),
            card.printedTypes().get(0),
            card.rarity(),
            card.expansion(),
            Optional.of(card.oracleText().get(0)),
            Optional.of(card.flavorText().get(0)),
            Optional.of(card.printedText().get(0)),
            card.artist().get(0),
            card.multiverseid().get(0),
            card.number().get(0),
            card.power().get(0),
            card.toughness().get(0),
            card.loyalty().get(0),
            new TreeMap<>(card.rulings()),
            card.legality(),
            card.commandFormats()
        );
    }

    /**
     * {@inheritDoc}
     * Import a list of all cards that exist in Magic: the Gathering from a JSON file downloaded from
     * {@link "http://www.mtgjson.com"}.  Also populate the lists of types and expansions (and their blocks).
     *
     * @return The inventory of cards that can be added to a deck.
     */
    @Override
    protected Inventory doInBackground() throws Exception
    {
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        publish("Opening " + file.getName() + "...");

        var cards = new ArrayList<Card>();
        var faces = new HashMap<Card, List<String>>();
        var melds = new HashMap<Card, List<String>>();
        var expansions = new HashSet<Expansion>();
        var blockNames = new HashSet<String>();

        // Read the inventory file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")))
        {
            publish("Parsing " + file.getName() + "...");

            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            DatabaseVersion version = root.has("meta") ?
                new DatabaseVersion(root.get("meta").getAsJsonObject().get("version").getAsString()) :
                new DatabaseVersion(0, 0, 0); // Anything less than 5.0.0 will do for pre-5.0.0 databases

            var entries = (version.compareTo(VER_5_0_0) < 0 ? root : root.get("data").getAsJsonObject()).entrySet();
            int numCards = 0;
            if (version.compareTo(VER_5_0_0) < 0)
            {
                for (var setNode : entries)
                    for (JsonElement card : setNode.getValue().getAsJsonObject().get("cards").getAsJsonArray())
                        if (card.getAsJsonObject().has("multiverseId"))
                            numCards++;
            }
            else
            {
                for (var setNode : entries)
                    for (JsonElement card : setNode.getValue().getAsJsonObject().get("cards").getAsJsonArray())
                        if (card.getAsJsonObject().get("identifiers").getAsJsonObject().has("multiverseId"))
                            numCards++;
            }

            // We don't use String.intern() here because the String pool that is maintained must include extra data that adds several MB
            // to the overall memory consumption of the inventory
            var costs = new HashMap<String, ManaCost>();
            var colorLists = new HashMap<String, List<ManaType>>();
            var allSupertypes = new HashMap<String, String>(); // map of supertype onto string reference
            var supertypeSets = new HashMap<String, Set<String>>();
            var allTypes = new HashMap<String, String>(); // map of type onto string reference
            var typeSets = new HashMap<String, Set<String>>();
            var allSubtypes = new HashMap<String, String>(); // map of subtype onto string reference
            var subtypeSets = new HashMap<String, Set<String>>();
            var printedTypes = new HashMap<String, String>(); // Map of type line onto string reference
            var artists = new HashMap<String, String>(); // Map of artist name onto string reference
            var formats = new HashMap<>(FormatConstraints.FORMAT_NAMES.stream().collect(Collectors.toMap(Function.identity(), Function.identity())));
            var numbers = new HashMap<String, String>(); // Map of number (string) onto string reference
            var stats = new HashMap<String, CombatStat>();
            var loyalties = new HashMap<String, Loyalty>();
            publish("Reading cards from " + file.getName() + "...");
            setProgress(0);
            for (var setNode : entries)
            {
                if (isCancelled())
                {
                    expansions.clear();
                    blockNames.clear();
                    allSupertypes.clear();
                    allTypes.clear();
                    allSubtypes.clear();
                    formats.clear();
                    cards.clear();
                    return new Inventory();
                }

                // Create the new Expansion
                JsonObject setProperties = setNode.getValue().getAsJsonObject();
                JsonArray setCards = setProperties.get("cards").getAsJsonArray();
                Expansion set = new Expansion(
                    setProperties.get("name").getAsString(),
                    Optional.ofNullable(setProperties.get("block")).map(JsonElement::getAsString).orElse("<No Block>"),
                    setProperties.get("code").getAsString(),
                    setCards.size(),
                    LocalDate.parse(setProperties.get("releaseDate").getAsString(), Expansion.DATE_FORMATTER)
                );
                expansions.add(set);
                blockNames.add(set.block);
                publish("Loading cards from " + set + "...");

                for (JsonElement cardElement : setCards)
                {
                    // Create the new card for the expansion
                    JsonObject card = cardElement.getAsJsonObject();

                    // Card's multiverseid.  Skip cards that aren't in gatherer
                    int multiverseid = Optional.ofNullable(version.compareTo(VER_5_0_0) < 0 ? card.get("multiverseId") : card.get("identifiers").getAsJsonObject().get("multiverseId")).map(JsonElement::getAsInt).orElse(-1);
                    if (multiverseid < 0)
                        continue;

                    // Card's name
                    String name = card.get(card.has("faceName") ? "faceName" : "name").getAsString();

                    // If the card is a token, skip it
                    CardLayout layout;
                    try
                    {
                        layout = CardLayout.valueOf(card.get("layout").getAsString().toUpperCase().replaceAll("[^A-Z]", "_"));
                    }
                    catch (IllegalArgumentException e)
                    {
                        errors.add(name + " (" + set + "): " + e.getMessage());
                        continue;
                    }

                    // Mana Cost
                    ManaCost cost;
                    String costStr = card.has("manaCost") ? card.get("manaCost").getAsString() : "";
                    if (costs.containsKey(costStr))
                        cost = costs.get(costStr);
                    else
                    {
                        cost = ManaCost.parseManaCost(costStr);
                        costs.put(costStr, cost);
                    }

                    // Colors and color identity
                    List<ManaType> colors = new ArrayList<>();
                    List<ManaType> identity = new ArrayList<>();
                    String colorsStr = card.get("colors").getAsJsonArray().toString();
                    String identityStr = card.get("colorIdentity").getAsJsonArray().toString();
                    if (colorLists.containsKey(colorsStr))
                        colors = colorLists.get(colorsStr);
                    else
                    {
                        for (JsonElement e : card.get("colors").getAsJsonArray())
                            colors.add(ManaType.parseManaType(e.getAsString()));
                        colors = Collections.unmodifiableList(colors);
                        colorLists.put(colorsStr, colors);
                    }
                    if (colorLists.containsKey(identityStr))
                        identity = colorLists.get(identityStr);
                    else
                    {
                        for (JsonElement e : card.get("colorIdentity").getAsJsonArray())
                            identity.add(ManaType.parseManaType(e.getAsString()));
                        identity = Collections.unmodifiableList(identity);
                        colorLists.put(identityStr, identity);
                    }

                    // Supertypes
                    Set<String> supertypes;
                    String supertypeStr = card.get("supertypes").getAsJsonArray().toString();
                    if (supertypeSets.containsKey(supertypeStr))
                        supertypes = supertypeSets.get(supertypeStr);
                    else
                    {
                        supertypes = new HashSet<>();
                        for (JsonElement e : card.get("supertypes").getAsJsonArray())
                        {
                            if (!allSupertypes.containsKey(e.getAsString()))
                                allSupertypes.put(e.getAsString(), e.getAsString());
                            supertypes.add(allSupertypes.get(e.getAsString()));
                        }
                        supertypeSets.put(supertypeStr, supertypes);
                    }

                    // Types
                    Set<String> types;
                    String typeStr = card.get("types").getAsJsonArray().toString();
                    if (typeSets.containsKey(typeStr))
                        types = typeSets.get(typeStr);
                    else
                    {
                        types = new HashSet<>();
                        for (JsonElement e : card.get("types").getAsJsonArray())
                        {
                            if (!allTypes.containsKey(e.getAsString()))
                                allTypes.put(e.getAsString(), e.getAsString());
                            types.add(allTypes.get(e.getAsString()));
                        }
                        typeSets.put(typeStr, types);
                    }

                    // Subtypes
                    Set<String> subtypes;
                    String subtypeStr = card.get("subtypes").getAsJsonArray().toString();
                    if (subtypeSets.containsKey(subtypeStr))
                        subtypes = subtypeSets.get(subtypeStr);
                    else
                    {
                        subtypes = new HashSet<>();
                        for (JsonElement e : card.get("subtypes").getAsJsonArray())
                        {
                            if (!allSubtypes.containsKey(e.getAsString()))
                                allSubtypes.put(e.getAsString(), e.getAsString());
                            subtypes.add(allSubtypes.get(e.getAsString()));
                        }
                        subtypeSets.put(subtypeStr, subtypes);
                    }

                    // Printed type line
                    String printedType = card.has("originalType") ? card.get("originalType").getAsString() : "";
                    if (printedTypes.containsKey(printedType))
                        printedType = printedTypes.get(printedType);
                    else
                        printedTypes.put(printedType, printedType);

                    // Artist
                    String artist = card.has("artist") ? card.get("artist").getAsString() : "";
                    if (artists.containsKey(artist))
                        artist = artists.get(artist);
                    else
                        artists.put(artist, artist);

                    String number = card.get("number").getAsString();
                    if (numbers.containsKey(number))
                        number = numbers.get(number);
                    else
                        numbers.put(number, number);

                    // Power, toughness, and loyalty
                    CombatStat power, toughness;
                    String powerStr = card.has("power") ? card.get("power").getAsString() : "";
                    String toughStr = card.has("toughness") ? card.get("toughness").getAsString() : "";
                    if (stats.containsKey(powerStr))
                        power = stats.get(powerStr);
                    else
                    {
                        power = new CombatStat(powerStr);
                        stats.put(powerStr, power);
                    }
                    if (stats.containsKey(toughStr))
                        toughness = stats.get(toughStr);
                    else
                    {
                        toughness = new CombatStat(toughStr);
                        stats.put(toughStr, toughness);
                    }
                    Loyalty loyalty;
                    String loyaltyStr = card.has("loyalty") ? card.get("loyalty").isJsonNull() ? "X" : card.get("loyalty").getAsString() : "";
                    if (loyalties.containsKey(loyaltyStr))
                        loyalty = loyalties.get(loyaltyStr);
                    else
                    {
                        loyalty = new Loyalty(loyaltyStr);
                        loyalties.put(loyaltyStr, loyalty);
                    }

                    // Rulings
                    var rulings = new TreeMap<Date, List<String>>();
                    if (card.has("rulings"))
                    {
                        for (JsonElement l : card.get("rulings").getAsJsonArray())
                        {
                            JsonObject o = l.getAsJsonObject();
                            String ruling = o.get("text").getAsString();
                            try
                            {
                                Date date = format.parse(o.get("date").getAsString());
                                if (!rulings.containsKey(date))
                                    rulings.put(date, new ArrayList<>());
                                rulings.get(date).add(ruling);
                            }
                            catch (ParseException x)
                            {
                                errors.add(name + " (" + set + "): " + x.getMessage());
                            }
                        }
                    }

                    // Format legality
                    var legality = new HashMap<String, Legality>();
                    for (var entry : card.get("legalities").getAsJsonObject().entrySet())
                    {
                        if (!formats.containsKey(entry.getKey()))
                            formats.put(entry.getKey(), entry.getKey());
                        legality.put(formats.get(entry.getKey()), Legality.parseLegality(entry.getValue().getAsString()));
                    }

                    // Formats the card can be commander in
                    var commandFormats = !card.has("leadershipSkills") ? Collections.<String>emptyList() :
                        card.get("leadershipSkills").getAsJsonObject().entrySet().stream()
                            .filter((e) -> e.getValue().getAsBoolean())
                            .map(Map.Entry::getKey)
                            .sorted()
                            .collect(Collectors.toList());

                    Card c = new SingleCard(
                        layout,
                        name,
                        cost,
                        colors,
                        identity,
                        supertypes,
                        types,
                        subtypes,
                        printedType,
                        Rarity.parseRarity(card.get("rarity").getAsString()),
                        set,
                        Optional.ofNullable(card.get("text")).map(JsonElement::getAsString),
                        Optional.ofNullable(card.get("flavorText")).map(JsonElement::getAsString),
                        Optional.ofNullable(card.get("originalText")).map(JsonElement::getAsString),
                        artist,
                        multiverseid,
                        number,
                        power,
                        toughness,
                        loyalty,
                        rulings,
                        legality,
                        commandFormats
                    );

                    // Collect unexpected card values
                    if (c.artist().stream().anyMatch(String::isEmpty))
                        errors.add(c.unifiedName() + " (" + c.expansion() + "): Missing artist!");

                    // Add to map of faces if the card has multiple faces
                    if (layout.isMultiFaced)
                    {
                        if (version.compareTo(VER_5_0_0) < 0)
                        {
                            var names = new ArrayList<String>();
                            for (JsonElement e : card.get("names").getAsJsonArray())
                                names.add(e.getAsString());
                            faces.put(c, names);
                        }
                        else if (layout != CardLayout.MELD)
                            faces.put(c, Arrays.asList(card.get("name").getAsString().split(Card.FACE_SEPARATOR)));
                        else
                            melds.put(c, Arrays.asList(card.get("name").getAsString().split(Card.FACE_SEPARATOR)));
                    }

                    cards.add(c);
                    setProgress(cards.size()*100/numCards);
                }
            }

            publish("Processing multi-faced cards...");
            var facesList = new ArrayList<>(faces.keySet());
            while (!facesList.isEmpty())
            {
                boolean error = false;

                Card face = facesList.remove(0);
                var otherFaces = new ArrayList<Card>();
                if (version.compareTo(VER_5_0_0) < 0 || face.layout() != CardLayout.MELD)
                {
                    var faceNames = faces.get(face);
                    for (Card c : facesList)
                        if (faceNames.contains(c.unifiedName()) && c.expansion().equals(face.expansion()))
                            otherFaces.add(c);
                    facesList.removeAll(otherFaces);
                    otherFaces.add(face);
                    otherFaces.sort(Comparator.comparingInt((a) -> faceNames.indexOf(a.unifiedName())));
                }
                cards.removeAll(otherFaces);

                switch (face.layout())
                {
                case SPLIT: case AFTERMATH: case ADVENTURE:
                    if (otherFaces.size() < 2)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Can't find other face(s) for split card.");
                        error = true;
                    }
                    else
                    {
                        for (Card f : otherFaces)
                        {
                            if (f.layout() != face.layout())
                            {
                                errors.add(face.toString() + " (" + face.expansion() + "): Can't join non-split faces into a split card.");
                                error = true;
                            }
                        }
                    }
                    if (!error)
                        cards.add(new SplitCard(otherFaces));
                    else
                        for (Card f : otherFaces)
                            cards.add(convertToNormal(f));
                    break;
                case FLIP:
                    if (otherFaces.size() < 2)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Can't find other side of flip card.");
                        error = true;
                    }
                    else if (otherFaces.size() > 2)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Too many sides for flip card.");
                        error = true;
                    }
                    else if (otherFaces.get(0).layout() != CardLayout.FLIP || otherFaces.get(1).layout() != CardLayout.FLIP)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Can't join non-flip faces into a flip card.");
                        error = true;
                    }
                    if (!error)
                        cards.add(new FlipCard(otherFaces.get(0), otherFaces.get(1)));
                    else
                        for (Card f : otherFaces)
                            cards.add(convertToNormal(f));
                    break;
                case TRANSFORM:
                    if (otherFaces.size() < 2)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Can't find other face of double-faced card.");
                        error = true;
                    }
                    else if (otherFaces.size() > 2)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Too many faces for double-faced card.");
                        error = true;
                    }
                    else if (otherFaces.get(0).layout() != CardLayout.TRANSFORM || otherFaces.get(1).layout() != CardLayout.TRANSFORM)
                    {
                        errors.add(face.toString() + " (" + face.expansion() + "): Can't join single-faced cards into double-faced cards.");
                        error = true;
                    }
                    if (!error)
                        cards.add(new TransformCard(otherFaces.get(0), otherFaces.get(1)));
                    else
                        for (Card f : otherFaces)
                            cards.add(convertToNormal(f));
                    break;
                case MELD:
                    if (version.compareTo(VER_5_0_0) < 0)
                    {
                        if (otherFaces.size() < 3)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't find some faces of meld card.");
                            error = true;
                        }
                        else if (otherFaces.size() > 3)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Too many faces for meld card.");
                            error = true;
                        }
                        else if (otherFaces.get(0).layout() != CardLayout.MELD || otherFaces.get(1).layout() != CardLayout.MELD || otherFaces.get(2).layout() != CardLayout.MELD)
                        {
                            errors.add(face.toString() + " (" + face.expansion() + "): Can't join single-faced cards into meld cards.");
                            error = true;
                        }
                        if (!error)
                        {
                            cards.add(new MeldCard(otherFaces.get(0), otherFaces.get(2), otherFaces.get(1)));
                            cards.add(new MeldCard(otherFaces.get(2), otherFaces.get(0), otherFaces.get(1)));
                        }
                        else
                            for (Card f : otherFaces)
                                cards.add(convertToNormal(f));
                    }
                    else
                        errors.add(face + " (" + face.expansion() + "): Wrong processing of meld card.");
                    break;
                default:
                    break;
                }
            }

            if (version.compareTo(VER_5_0_0) >= 0)
            {
                var meldBacks = melds.entrySet().stream().filter((e) -> e.getValue().size() == 1).map((e) -> e.getKey()).collect(Collectors.toList());
                var meldFronts = new HashMap<>(melds.entrySet().stream().filter((e) -> e.getValue().size() > 1).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
                for (Card back : meldBacks)
                {
                    var fronts = meldFronts.entrySet().stream().filter((e) -> e.getKey().expansion().equals(back.expansion()) && e.getValue().contains(back.unifiedName())).map((e) -> e.getKey()).collect(Collectors.toList());
                    if (fronts.size() < 2)
                        errors.add(back + " (" + back.expansion() + "): Can't find some faces of meld card.");
                    else if (fronts.size() == 2)
                    {
                        cards.add(new MeldCard(fronts.get(0), fronts.get(1), back));
                        cards.add(new MeldCard(fronts.get(1), fronts.get(0), back));
                        cards.remove(back);
                        cards.removeAll(fronts);
                        for (Card front : fronts)
                            meldFronts.remove(front);
                    }
                    else
                        errors.add(back + "( " + back.expansion() + "): Too many faces for meld card.");
                }
                for (Card front : meldFronts.keySet())
                    errors.add(front + "( " + front.expansion() + "): Couldn't pair with a back face.");
            }

            publish("Removing duplicate entries...");
            var unique = new HashMap<Integer, Card>();
            for (Card c : cards)
                if (!unique.containsKey(c.multiverseid().get(0)))
                    unique.put(c.multiverseid().get(0), c);
            cards = new ArrayList<>(unique.values());

            // Store the lists of expansion and block names and types and sort them alphabetically
            Expansion.expansions = expansions.stream().sorted().toArray(Expansion[]::new);
            Expansion.blocks = blockNames.stream().sorted().toArray(String[]::new);
            SupertypeFilter.supertypeList = allSupertypes.values().stream().sorted().toArray(String[]::new);
            CardTypeFilter.typeList = allTypes.values().stream().sorted().toArray(String[]::new);
            SubtypeFilter.subtypeList = allSubtypes.values().stream().sorted().toArray(String[]::new);

            var missingFormats = formats.values().stream().filter((f) -> !FormatConstraints.FORMAT_NAMES.contains(f)).sorted().collect(Collectors.toList());
            if (!missingFormats.isEmpty())
                errors.add("Could not find definitions for the following formats: " + missingFormats.stream().collect(Collectors.joining(", ")));
        }

        Inventory inventory = new Inventory(cards);

        if (Files.exists(Path.of(SettingsDialog.settings().inventory.tags)))
        {
            @SuppressWarnings("unchecked")
            var rawTags = (Map<Integer, Set<String>>)MainFrame.SERIALIZER.fromJson(String.join("\n", Files.readAllLines(Path.of(SettingsDialog.settings().inventory.tags))), new TypeToken<Map<Long, Set<String>>>() {}.getType());
            Card.tags.clear();
            Card.tags.putAll(rawTags.entrySet().stream().collect(Collectors.toMap((e) -> inventory.find(e.getKey()), Map.Entry::getValue)));
        }

        return inventory;
    }

    /**
     * {@inheritDoc}
     * Change the label in the dialog to match the stage this worker is in.
     */
    @Override
    protected void process(List<String> chunks)
    {
        for (String chunk : chunks)
            consumer.accept(chunk);
    }

    @Override
    protected void done()
    {
        finished.run();
    }

    /**
     * @return A list of warnings that occured while loading the inventory.
     */
    public List<String> warnings()
    {
        return errors;
    }
}