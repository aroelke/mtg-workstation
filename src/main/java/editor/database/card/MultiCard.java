package editor.database.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import editor.database.attributes.CombatStat;
import editor.database.attributes.Legality;
import editor.database.attributes.Loyalty;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.attributes.Rarity;
import editor.util.Lazy;

/**
 * This class represents an abstract card with multiple faces.
 *
 * @author Alec Roelke
 */
public abstract class MultiCard extends Card
{
    /**
     * List containing the set of types for each of this MultiCard's faces.
     */
    private Lazy<List<Set<String>>> allTypes;
    /**
     * List containing the artist of each of this MultiCard's faces.
     */
    private Lazy<List<String>> artist;
    /**
     * Tuple of the color identity of this MultiCard.
     */
    private Lazy<List<ManaType>> colorIdentity;
    /**
     * Tuple of all of the colors of this MultiCard.
     */
    private Lazy<List<ManaType>> colors;
    /**
     * List of formats this MultiCard can be commander in.
     */
    private Lazy<List<String>> commandFormats;
    /**
     * List of Cards that represent faces.  They should all have exactly one face.
     */
    private List<? extends Card> faces;
    /**
     * List containing the flavor text of each of this MultiCard's faces.
     */
    private Lazy<List<String>> flavorText;
    /**
     * List containing the image name of each of this MultiCard's faces.
     */
    private Lazy<List<String>> imageNames;
    /**
     * Whether or not this MultiCard is a land card, which it only is if its front
     * (first) face is a land.
     */
    private boolean isLand;
    /**
     * Tuple containing the loyalty of each of this MultiCard's faces.
     */
    private Lazy<List<Loyalty>> loyalty;
    /**
     * List of mana costs of the faces of this MultiCard.
     */
    private Lazy<List<ManaCost>> manaCost;
    /**
     * List of the names of the faces of this MultiCard.
     */
    private Lazy<List<String>> name;
    /**
     * List containing the collector's number of each of this MultiCard's faces.
     */
    private Lazy<List<String>> number;
    /**
     * List containing this MultiCard's faces' multiverseids.
     */
    private Lazy<List<Integer>> multiverseid;
    /**
     * List containing this MultiCard's faces' Scryfall illustration ids.
     */
    private Lazy<List<String>> scryfallid;
    /**
     * List containing the oracle text of each of this MultiCard's faces.
     */
    private Lazy<List<String>> oracleText;
    /**
     * Tuple containing the power of each of this MultiCard's faces.
     */
    private Lazy<List<CombatStat>> power;
    /**
     * List containing the printed text of each of this MultiCard's faces.
     */
    private Lazy<List<String>> printedText;
    /**
     * List containing the printed type lines of each of this MultiCard's faces.
     */
    private Lazy<List<String>> printedTypes;
    /**
     * Map containing the rulings of this MultiCard and the dates they were made on.
     */
    private Lazy<Map<Date, List<String>>> rulings;
    /**
     * Set of this MultiCard's subtypes including all of its faces.
     */
    private Lazy<Set<String>> subtypes;
    /**
     * Set of this MultiCard's supertypes including all of its faces.
     */
    private Lazy<Set<String>> supertypes;
    /**
     * Tuple containing the toughness of each of this MultiCard's faces.
     */
    private Lazy<List<CombatStat>> toughness;
    /**
     * List containing the type line of each of this MultiCard's faces.
     */
    private Lazy<List<String>> typeLine;
    /**
     * Set of this MultiCard's types including all of its faces.
     */
    private Lazy<Set<String>> types;

    /**
     * Create a new MultiCard out of the given cards.
     *
     * @param layout layout of the new MultiCard, which should be one that has muliple faces
     * @param f cards to use as faces
     */
    public MultiCard(CardLayout layout, Card... f)
    {
        this(layout, Arrays.asList(f));
    }

    /**
     * Create a new MultiCard out of the given list of Cards. Each one should only have one face.
     *
     * @param layout layout of the new MultiCard, which should be one that has multiple faces
     * @param f      cards to use as faces
     */
    public MultiCard(CardLayout layout, List<? extends Card> f)
    {
        super(f.get(0).expansion(), layout, f.size());

        faces = f;
        for (Card face : faces)
            if (face.faces() > 1)
                throw new IllegalArgumentException("Only normal, single-faced cards can be joined into a multi-faced card");

        name = new Lazy<>(() -> collect(Card::name));
        manaCost = new Lazy<>(() -> collect(Card::manaCost));
        colors = new Lazy<>(() -> {
            var sorted = new ArrayList<>(faces.stream().flatMap((c) -> c.colors().stream()).collect(Collectors.toSet()));
            ManaType.sort(sorted);
            return Collections.unmodifiableList(sorted);
        });
        colorIdentity = new Lazy<>(() -> {
            var sorted = new ArrayList<>(faces.stream().flatMap((c) -> c.colors().stream()).collect(Collectors.toSet()));
            ManaType.sort(sorted);
            return Collections.unmodifiableList(sorted);
        });
        supertypes = new Lazy<>(() -> faces.stream().flatMap((c) -> c.supertypes().stream()).collect(Collectors.toSet()));
        types = new Lazy<>(() -> faces.stream().flatMap((c) -> c.types().stream()).collect(Collectors.toSet()));
        subtypes = new Lazy<>(() -> faces.stream().flatMap((c) -> c.subtypes().stream()).collect(Collectors.toSet()));
        allTypes = new Lazy<>(() -> {
            var a = new ArrayList<Set<String>>();
            for (Card face : faces)
            {
                Set<String> faceTypes = new HashSet<>();
                faceTypes.addAll(face.supertypes());
                faceTypes.addAll(face.types());
                faceTypes.addAll(face.subtypes());
                a.add(Collections.unmodifiableSet(faceTypes));
            }
            return Collections.unmodifiableList(a);
        });
        typeLine = new Lazy<>(() -> collect(Card::typeLine));
        printedTypes = new Lazy<>(() -> collect(Card::printedTypes));
        oracleText = new Lazy<>(() -> collect(Card::oracleText));
        flavorText = new Lazy<>(() -> collect(Card::flavorText));
        printedText = new Lazy<>(() -> collect(Card::printedText));
        artist = new Lazy<>(() -> collect(Card::artist));
        number = new Lazy<>(() -> collect(Card::number));
        power = new Lazy<>(() -> collect(Card::power));
        toughness = new Lazy<>(() -> collect(Card::toughness));
        loyalty = new Lazy<>(() -> collect(Card::loyalty));
        rulings = new Lazy<>(() -> Collections.unmodifiableMap(faces.stream().map(Card::rulings).reduce(new TreeMap<>(), (a, b) -> {
            for (Date k : b.keySet())
                a.getOrDefault(k, new ArrayList<>()).addAll(b.get(k));
            return a;
        })));
        imageNames = new Lazy<>(() -> collect(Card::imageNames));
        multiverseid = new Lazy<>(() -> collect(Card::multiverseid));
        scryfallid = new Lazy<>(() -> collect(Card::scryfallid));
        isLand = faces.get(0).isLand() || (layout == CardLayout.MODAL_DFC && faces.get(1).isLand());
        commandFormats = new Lazy<>(() -> faces.stream().flatMap((c) -> c.commandFormats().stream()).distinct().sorted().collect(Collectors.toList()));
    }

    @Override
    public List<Set<String>> allTypes()
    {
        return allTypes.get();
    }

    @Override
    public List<String> artist()
    {
        return artist.get();
    }

    /**
     * Collect the values of the give characteristic from each face into a list.
     *
     * @param characteristic characteristic to collect
     * @return the value of the characteristic for each face of this MultiCard collected into a list.
     */
    private <T> List<T> collect(Function<Card, List<T>> characteristic)
    {
        return faces.stream().map((f) -> characteristic.apply(f).get(0)).collect(Collectors.toList());
    }

    @Override
    public List<ManaType> colorIdentity()
    {
        return colorIdentity.get();
    }

    @Override
    public List<ManaType> colors()
    {
        return colors.get();
    }

    @Override
    public List<ManaType> colors(int face) throws IndexOutOfBoundsException
    {
        return faces.get(face).colors();
    }

    @Override
    public List<String> commandFormats()
    {
        return commandFormats.get();
    }

    @Override
    public List<String> flavorText()
    {
        return flavorText.get();
    }

    @Override
    public List<String> imageNames()
    {
        return imageNames.get();
    }

    @Override
    public boolean isLand()
    {
        return isLand;
    }

    @Override
    public Map<String, Legality> legality()
    {
        return faces.get(0).legality();
    }

    @Override
    public List<Loyalty> loyalty()
    {
        return loyalty.get();
    }

    @Override
    public List<ManaCost> manaCost()
    {
        return manaCost.get();
    }

    @Override
    public double minManaValue()
    {
        return faces.stream().mapToDouble(Card::manaValue).min().orElseThrow(() -> new IllegalStateException("multi card with no faces"));
    }

    @Override
    public double maxManaValue()
    {
        return faces.stream().mapToDouble(Card::manaValue).max().orElseThrow(() -> new IllegalStateException("multi card with no faces"));
    }

    @Override
    public double avgManaValue()
    {
        return faces.stream().mapToDouble(Card::manaValue).average().orElseThrow(() -> new IllegalStateException("multi card with no faces"));
    }

    @Override
    public List<Integer> multiverseid()
    {
        return multiverseid.get();
    }

    @Override
    public List<String> scryfallid()
    {
        return scryfallid.get();
    }

    @Override
    public List<String> name()
    {
        return name.get();
    }

    @Override
    public List<String> number()
    {
        return number.get();
    }

    @Override
    public List<String> oracleText()
    {
        return oracleText.get();
    }

    @Override
    public List<CombatStat> power()
    {
        return power.get();
    }

    @Override
    public List<String> printedText()
    {
        return printedText.get();
    }

    @Override
    public List<String> printedTypes()
    {
        return printedTypes.get();
    }

    @Override
    public Rarity rarity()
    {
        return faces.get(0).rarity();
    }

    @Override
    public Map<Date, List<String>> rulings()
    {
        return rulings.get();
    }

    @Override
    public Set<String> subtypes()
    {
        return subtypes.get();
    }

    @Override
    public Set<String> supertypes()
    {
        return supertypes.get();
    }

    @Override
    public List<CombatStat> toughness()
    {
        return toughness.get();
    }

    @Override
    public List<String> typeLine()
    {
        return typeLine.get();
    }

    @Override
    public Set<String> types()
    {
        return types.get();
    }

    @Override
    public void formatDocument(StyledDocument document, boolean printed)
    {
        Style textStyle = document.getStyle("text");
        try
        {
            for (int f = 0; f < faces.size(); f++)
            {
                formatDocument(document, printed, f);
                if (f < faces.size() - 1)
                    document.insertString(document.getLength(), "\n" + TEXT_SEPARATOR + "\n", textStyle);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void formatDocument(StyledDocument document, boolean printed, int f)
    {
        faces.get(f).formatDocument(document, printed);
    }
}
