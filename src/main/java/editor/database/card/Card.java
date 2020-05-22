package editor.database.card;

import java.awt.datatransfer.DataFlavor;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import editor.database.attributes.CombatStat;
import editor.database.attributes.Expansion;
import editor.database.attributes.Legality;
import editor.database.attributes.Loyalty;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.attributes.Rarity;
import editor.database.symbol.FunctionalSymbol;
import editor.database.symbol.Symbol;
import editor.gui.generic.ComponentUtils;
import editor.util.Lazy;
import editor.util.UnicodeSymbols;

/**
 * This interface represents an abstract Card with various characteristics.  Each card can be uniquely
 * identified by the set it is in, its name, and its image name (which is its name followed by a
 * number if there is more than one version of the same card in the same set).  All of its values are constant.
 *
 * @author Alec Roelke
 */
public abstract class Card
{
    /**
     * DataFlavor representing cards being transferred.
     */
    public static final DataFlavor cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Card[].class.getName() + "\"", "Card Array");
    /**
     * Separator string between characteristics of a multi-face card.
     */
    public static final String FACE_SEPARATOR = "//";
    /**
     * Separator for card text when displaying multiple cards in a single text box.
     */
    public static final String TEXT_SEPARATOR = "-----";
    /**
     * String representing this Card's name in its text box.
     */
    public static final String THIS = "~";

    /**
     * User-defined card tags.
     */
    public static final Map<Card, Set<String>> tags = new HashMap<>();

    /**
     * Set of all user-defined card tags among all cards.
     * @see #tags
     */
    public static Set<String> tags()
    {
        return tags.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    /**
     * Whether or not this Card can be a commander.
     */
    private Lazy<Boolean> canBeCommander;
    /**
     * Expansion this Card belongs to.
     */
    private final Expansion expansion;
    /**
     * Number of faces this Card has.
     */
    private final int faces;
    /**
     * Whether or not to ignore the card count restriction for this Card.
     */
    private Lazy<Boolean> ignoreCountRestriction;
    /**
     * Layout of this Card.
     *
     * @see CardLayout
     */
    private final CardLayout layout;
    /**
     * List of formats this Card is legal in.
     */
    private Lazy<List<String>> legalIn;
    /**
     * If this Card is legendary, the name of the character or item depicted by it.  Otherwise,
     * its normalized name.
     */
    private Lazy<List<String>> legendName;
    /**
     * List of flavor texts of the faces of this Card, converted to lower case and with special
     * characters removed.
     */
    private Lazy<List<String>> normalizedFlavor;
    /**
     * All lower-case, normalized name of this Card with special characters removed.
     */
    private Lazy<List<String>> normalizedName;
    /**
     * List of oracle texts of the faces of this Card, converted to lower case and with special
     * characters removed.
     */
    private Lazy<List<String>> normalizedOracle;
    /**
     * List of printed texts of the faces of this Card, converted to lower case and with special
     * characers removed.
     */
    private Lazy<List<String>> normalizedPrinted;
    /**
     * Type lines of all faces of this Card, separated by {@link #FACE_SEPARATOR}.
     */
    private Lazy<String> unifiedTypeLine;

    /**
     * Create a new Card.  Most of the parameters are assigned lazily; that is, only
     * the first time their values are requested.
     *
     * @param expansion expension the new Card belongs to
     * @param layout    layout of the new Card
     * @param faces     number of faces the new Card has
     */
    public Card(Expansion expansion, CardLayout layout, int faces)
    {
        this.expansion = expansion;
        this.layout = layout;
        this.faces = faces;

        normalizedName = new Lazy<>(() -> Collections.unmodifiableList(name().stream().map(UnicodeSymbols::normalize).collect(Collectors.toList())));
        legendName = new Lazy<>(() -> {
            var legendNames = new ArrayList<String>();
            for (String fullName : normalizedName())
            {
                if (!supertypes().contains("Legendary"))
                    legendNames.add(fullName);
                else
                {
                    int comma = fullName.indexOf(',');
                    if (comma > 0)
                        legendNames.add(fullName.substring(0, comma).trim());
                    else
                    {
                        int the = fullName.indexOf("the ");
                        if (the == 0)
                            legendNames.add(fullName);
                        else if (the > 0)
                            legendNames.add(fullName.substring(0, the).trim());
                        else
                        {
                            int of = fullName.indexOf("of ");
                            if (of > 0)
                                legendNames.add(fullName.substring(0, of).trim());
                            else
                                legendNames.add(fullName);
                        }
                    }
                }
            }
            return Collections.unmodifiableList(legendNames);
        });
        unifiedTypeLine = new Lazy<>(() -> {
            StringJoiner join = new StringJoiner(" " + FACE_SEPARATOR + " ");
            for (String line : typeLine())
                join.add(line);
            return join.toString();
        });
        normalizedOracle = new Lazy<>(() -> {
            var texts = new ArrayList<String>(faces);
            for (int i = 0; i < faces; i++)
            {
                String normal = UnicodeSymbols.normalize(oracleText().get(i).toLowerCase());
                normal = normal.replace(legendName().get(i), Card.THIS).replace(normalizedName().get(i), Card.THIS);
                texts.add(normal);
            }
            return Collections.unmodifiableList(texts);
        });
        normalizedFlavor = new Lazy<>(() -> Collections.unmodifiableList(flavorText().stream().map(UnicodeSymbols::normalize).collect(Collectors.toList())));
        normalizedPrinted = new Lazy<>(() -> Collections.unmodifiableList(printedText().stream().map(UnicodeSymbols::normalize).collect(Collectors.toList())));
        legalIn = new Lazy<>(() -> Collections.unmodifiableList(legality().keySet().stream().filter(this::legalIn).collect(Collectors.toList())));
        canBeCommander = new Lazy<>(() -> supertypeContains("legendary") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("can be your commander")));
        ignoreCountRestriction = new Lazy<>(() -> supertypeContains("basic") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("a deck can have any number")));
    }

    /**
     * Get all of this Card's supertypes, card types, and subtypes.
     *
     * @return A list whose elements each is a set of Strings which contains all the supertypes,
     * types, and subtypes of the corresponding face.
     */
    public abstract List<Set<String>> allTypes();

    /**
     * Get the artist of each face of this Card.
     *
     * @return a list containing the artist of each face of this Card.
     */
    public abstract List<String> artist();

    /**
     * Check if this Card can be commander of a commander deck.
     *
     * @return true if this Card can be a commander in the Commander format, and false otherwise.
     */
    public boolean canBeCommander()
    {
        return canBeCommander.get();
    }

    /**
     * Get this card's converted mana cost(s).
     *
     * @return a list containing the converted mana costs of the faces of this Card.
     */
    public abstract List<Double> cmc();

    /**
     * Get this Card's color identity, which is comprised of its its colors and colors of any
     * mana symbols that appear in its rules text that is not reminder text, and in abilities that
     * are given it by basic land types.
     *
     * @return a list containing the colors in this Card's color identity.
     */
    public abstract List<ManaType> colorIdentity();

    /**
     * Get all of the colors across this Card's faces.
     *
     * @return a list containing the colors of this Card.
     */
    public abstract List<ManaType> colors();

    /**
     * Get the colors of one of the faces of this Card.
     *
     * @param face index of the face to get the colors of
     * @return a list containing the colors of the given face.
     */
    public abstract List<ManaType> colors(int face);

    /**
     * Compare this Card's unified name lexicographically with another's.
     *
     * @param other Card to compare names with
     * @return a positive number if this Card's name comes after the other's, 0 if
     * they are the same, or a negative number if it comes before.
     */
    public int compareName(Card other)
    {
        return Collator.getInstance(Locale.US).compare(unifiedName(), other.unifiedName());
    }

    @Override
    public boolean equals(Object other)
    {
        return other != null && (other == this || other instanceof Card && multiverseid().equals(((Card)other).multiverseid()));
    }

    /**
     * Get this Card's expansion.
     *
     * @return the expansion this Card belongs to.
     */
    public Expansion expansion()
    {
        return expansion;
    }

    /**
     * Get the number of faces this Card has.
     *
     * @return the number of faces
     */
    public int faces()
    {
        return faces;
    }

    /**
     * Get the flavor texts of the faces of this Card.
     *
     * @return a list containing the flavor text of each face of this Card.
     */
    public abstract List<String> flavorText();

    /**
     * Add the Oracle text of all of this Card's faces to the given document, separated
     * by a separator on its own line.
     *
     * @param document document to add text to
     * @param printed  whether to use printed or Oracle data for a card
     */
    public void formatDocument(StyledDocument document, boolean printed)
    {
        Style textStyle = document.getStyle("text");
        try
        {
            for (int f = 0; f < faces; f++)
            {
                formatDocument(document, printed, f);
                if (f < faces - 1)
                    document.insertString(document.getLength(), "\n" + TEXT_SEPARATOR + "\n", textStyle);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Add text and icons to the given document so it contains a nice-looking version of this
     * Card's Oracle text.  The document is expected to have styles "text" and "reminder."
     *
     * @param document document to format
     * @param printed  whether to use printed or Oracle data for a card
     * @param f        face to add to the document
     */
    public void formatDocument(StyledDocument document, boolean printed, int f)
    {
        Style textStyle = document.getStyle("text");
        Style reminderStyle = document.getStyle("reminder");
        Style chaosStyle = document.addStyle("CHAOS", null);
        StyleConstants.setIcon(chaosStyle, FunctionalSymbol.CHAOS.getIcon(ComponentUtils.TEXT_SIZE));
        try
        {
            document.insertString(document.getLength(), name().get(f) + " ", textStyle);
            if (!manaCost().get(f).isEmpty())
            {
                for (Symbol symbol : manaCost().get(f))
                {
                    Style style = document.addStyle(symbol.toString(), null);
                    StyleConstants.setIcon(style, symbol.getIcon(ComponentUtils.TEXT_SIZE));
                    document.insertString(document.getLength(), symbol.toString(), style);
                }
                document.insertString(document.getLength(), " ", textStyle);
            }
            if (cmc().get(f) == cmc().get(f).doubleValue())
                document.insertString(document.getLength(), "(" + (int)cmc().get(f).doubleValue() + ")\n", textStyle);
            else
                document.insertString(document.getLength(), "(" + cmc().get(f) + ")\n", textStyle);
            if (!manaCost().get(f).colors().equals(colors(f)))
            {
                for (ManaType color : colors(f))
                {
                    Style indicatorStyle = document.addStyle("indicator", document.getStyle("text"));
                    StyleConstants.setForeground(indicatorStyle, color.color);
                    document.insertString(document.getLength(), String.valueOf(UnicodeSymbols.BULLET), indicatorStyle);
                }
                if (!colors().isEmpty())
                    document.insertString(document.getLength(), " ", textStyle);
            }
            if (printed)
                document.insertString(document.getLength(), printedTypes().get(f) + '\n', textStyle);
            else
                document.insertString(document.getLength(), typeLine().get(f) + '\n', textStyle);
            document.insertString(document.getLength(), expansion.name + ' ' + rarity() + '\n', textStyle);

            String abilities = (printed ? printedText() : oracleText()).get(f);
            if (!abilities.isEmpty())
            {
                int start = 0;
                Style style = textStyle;
                for (int i = 0; i < abilities.length(); i++)
                {
                    if (i == 0 || abilities.charAt(i) == '\n')
                    {
                        int nextLine = abilities.substring(i + 1).indexOf('\n');
                        int dash = (nextLine > -1 ? abilities.substring(i, nextLine + i) : abilities.substring(i)).indexOf(UnicodeSymbols.EM_DASH);
                        if (dash > -1)
                        {
                            dash += i;
                            if (i > 0)
                                document.insertString(document.getLength(), abilities.substring(start, i), style);
                            start = i;
                            // Assume that the dash used for ability words is surrounded by spaces, but not
                            // for keywords with cost parameters when the cost is nonmana cost
                            if (abilities.charAt(dash - 1) == ' ' && abilities.charAt(dash + 1) == ' ')
                                style = reminderStyle;
                        }
                    }
                    switch (abilities.charAt(i))
                    {
                    case '{':
                        document.insertString(document.getLength(), abilities.substring(start, i), style);
                        start = i + 1;
                        break;
                    case '}':
                        var symbol = Symbol.tryParseSymbol(abilities.substring(start, i));
                        if (symbol.isEmpty())
                        {
                            System.err.println("Unexpected symbol {" + abilities.substring(start, i) + "} in oracle text for " + unifiedName() + ".");
                            document.insertString(document.getLength(), abilities.substring(start, i), textStyle);
                        }
                        else
                        {
                            Style symbolStyle = document.addStyle(symbol.get().toString(), null);
                            StyleConstants.setIcon(symbolStyle, symbol.get().getIcon(ComponentUtils.TEXT_SIZE));
                            document.insertString(document.getLength(), symbol.get().toString(), symbolStyle);
                        }
                        start = i + 1;
                        break;
                    case '(':
                        document.insertString(document.getLength(), abilities.substring(start, i), style);
                        style = reminderStyle;
                        start = i;
                        break;
                    case ')':
                        document.insertString(document.getLength(), abilities.substring(start, i + 1), style);
                        style = textStyle;
                        start = i + 1;
                        break;
                    case 'C':
                        if (i < abilities.length() - 5 && abilities.substring(i, i + 5).equals("CHAOS"))
                        {
                            document.insertString(document.getLength(), abilities.substring(start, i), style);
                            document.insertString(document.getLength(), "CHAOS", chaosStyle);
                            start = i += 5;
                        }
                        break;
                    case UnicodeSymbols.EM_DASH:
                        document.insertString(document.getLength(), abilities.substring(start, i), style);
                        style = textStyle;
                        start = i;
                        break;
                    default:
                        break;
                    }
                    if (i == abilities.length() - 1 && abilities.charAt(i) != '}' && abilities.charAt(i) != ')')
                        document.insertString(document.getLength(), abilities.substring(start, i + 1), style);
                }
                document.insertString(document.getLength(), "\n", textStyle);
            }
            String flavor = flavorText().get(f);
            if (!flavor.isEmpty())
            {
                int start = 0;
                for (int i = 0; i < flavor.length(); i++)
                {
                    switch (flavor.charAt(i))
                    {
                    case '{':
                        document.insertString(document.getLength(), flavor.substring(start, i), reminderStyle);
                        start = i + 1;
                        break;
                    case '}':
                        var symbol = Symbol.tryParseSymbol(flavor.substring(start, i));
                        if (symbol.isEmpty())
                        {
                            System.err.println("Unexpected symbol {" + flavor.substring(start, i) + "} in flavor text for " + unifiedName() + ".");
                            document.insertString(document.getLength(), flavor.substring(start, i), reminderStyle);
                        }
                        else
                        {
                            Style symbolStyle = document.addStyle(symbol.get().toString(), null);
                            StyleConstants.setIcon(symbolStyle, symbol.get().getIcon(ComponentUtils.TEXT_SIZE));
                            document.insertString(document.getLength(), " ", symbolStyle);
                        }
                        start = i + 1;
                        break;
                    default:
                        break;
                    }
                    if (i == flavor.length() - 1 && flavor.charAt(i) != '}')
                        document.insertString(document.getLength(), flavor.substring(start, i + 1), reminderStyle);
                }
                document.insertString(document.getLength(), "\n", reminderStyle);
            }

            if (!Double.isNaN(power().get(f).value) && !Double.isNaN(toughness().get(f).value))
                document.insertString(document.getLength(), power().get(f) + "/" + toughness().get(f) + "\n", textStyle);
            else if (loyalty().get(f).exists())
                document.insertString(document.getLength(), loyalty().get(f) + "\n", textStyle);

            document.insertString(document.getLength(), artist().get(f) + " " + number().get(f) + "/" + expansion.count, textStyle);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name(), multiverseid());
    }

    /**
     * Check if this Card ignores the restriction on card counts in decks.
     *
     * @return true if there can be any number of copies of this Card in a deck, and false
     * otherwise.
     */
    public boolean ignoreCountRestriction()
    {
        return ignoreCountRestriction.get();
    }

    /**
     * Get the image name of each face of this Card.
     *
     * @return A list containing the name of each image corresponding to a face of this Card.
     */
    public abstract List<String> imageNames();

    /**
     * @return true if this Card is a land, and false otherwise.
     */
    public abstract boolean isLand();

    /**
     * Get this Card's layout.
     *
     * @return this Card's layout.
     */
    public CardLayout layout()
    {
        return layout;
    }

    /**
     * Get the formats this Card is legal in.
     *
     * @return a list of names of formats that this Card is legal in.
     */
    public List<String> legalIn()
    {
        return legalIn.get();
    }

    /**
     * Check if this Card is legal in the given format
     *
     * @param format format to look up
     * @return true if the given format exists and this Card is legal in it, and false
     * otherwise.
     */
    public boolean legalIn(String format)
    {
        if (format.equalsIgnoreCase("prismatic") && legalIn("classic") && legality().get(format) != Legality.BANNED)
            return true;
        else if (format.equalsIgnoreCase("classic") || format.equalsIgnoreCase("freeform"))
            return true;
        else if (format.contains("Block"))
        {
            format = format.substring(0, format.indexOf("Block")).trim();
            return expansion.block.equalsIgnoreCase(format)
                    || format.equalsIgnoreCase("urza") && expansion.block.equalsIgnoreCase("urza's")
                    || format.equalsIgnoreCase("lorwyn-shadowmoor") && (expansion.block.equalsIgnoreCase("lorwyn") || expansion.block.equalsIgnoreCase("shadowmoor"))
                    || format.equalsIgnoreCase("shards of alara") && expansion.block.equalsIgnoreCase("alara")
                    || format.equalsIgnoreCase("tarkir") && expansion.block.equalsIgnoreCase("khans of tarkir");
        }
        else
            return legality().containsKey(format) && legality().get(format) != Legality.BANNED;
    }

    /**
     * Get the set of formats and this Card's legalities in each one.
     *
     * @return a map whose keys are format names and whose values are the legalities
     * of this Card in those formats.
     */
    public abstract Map<String, Legality> legality();

    /**
     * Check the legality of this Card in the given format.
     *
     * @param format format to look up
     * @return The legality ({@link Legality#LEGAL}, {@link Legality#RESTRICTED},
     * {@link Legality#BANNED}) of this Card in the given format.
     */
    public Legality legalityIn(String format)
    {
        if (legalIn(format))
        {
            if (format.equalsIgnoreCase("prismatic"))
                format = "classic";
            return legality().getOrDefault(format, Legality.LEGAL);
        }
        else
            return Legality.BANNED;
    }

    /**
     * Get a version of {@link #normalizedName()} with the title removed if this
     * Card is legendary, or no changes if it isn't.
     *
     * @return A list containing the name of the character depicted by each face
     * of this Card if it is legendary.
     */
    public List<String> legendName()
    {
        return legendName.get();
    }

    /**
     * Get this Card's loyalty.  Any nonpositive number represents a nonexistent
     * loyalty.
     *
     * @return a list containing the loyalty of each face of this Card.
     */
    public abstract List<Loyalty> loyalty();

    /**
     * @return <code>true</code> if this Card has loyalty and it is variable (X),
     * and <code>false</code> otherwise.
     */
    public boolean loyaltyVariable()
    {
        return loyalty().stream().anyMatch(Loyalty::variable);
    }

    /**
     * Get this Card's mana cost(s).
     *
     * @return a list containing the mana costs of the faces of this Card.
     */
    public abstract List<ManaCost> manaCost();

    /**
     * @return the IDs of each face of this card as they are used by
     * <a href="http://gatherer.wizards.com">Gatherer</a>.
     */
    public abstract List<Long> multiverseid();

    /**
     * Get the name of each of this Card's faces.
     *
     * @return a List containing the name of each face of this Card
     */
    public abstract List<String> name();

    /**
     * Get the flavor texts of this Card's faces in lower case and with special characters replaced
     * with versions that appear on a standard QWERTY keyboard.
     *
     * @return a list containing the "normalized" flavor texts of this Card's faces.
     */
    public List<String> normalizedFlavor()
    {
        return normalizedFlavor.get();
    }

    /**
     * Get a version of {@link #name()} converted to lower case and special characters
     * converted to versions that appear on a standard QWERTY keyboard.
     *
     * @return a list containing the "normalized" names of this Card's faces
     */
    public List<String> normalizedName()
    {
        return normalizedName.get();
    }

    /**
     * Get the Oracle texts of this Card's faces in lower case and with special characters replaced
     * with versions that appear on a standard QWERTY keyboard.
     *
     * @return a list containing the "normalized" Oracle texts of this Card's faces.
     */
    public List<String> normalizedOracle()
    {
        return normalizedOracle.get();
    }

    /**
     * Get the printed texts of this Card's faces in lower case and with special characters replaced
     * with versions that appear on a standard QWERTY keyboard.
     *
     * @return a list containing the "normalized" printed texts of this Card's faces
     */
    public List<String> normalizedPrinted()
    {
        return normalizedPrinted.get();
    }

    /**
     * Get the collector number of each face of this Card.  This is a string since some cards don't
     * have numbers or are things like "1a"
     *
     * @return a list containing the collector's number of each face of this Card.
     */
    public abstract List<String> number();

    /**
     * Get the Oracle texts of the faces of this Card.
     *
     * @return the Oracle text of each of this Card's faces in a list.
     */
    public abstract List<String> oracleText();

    /**
     * Get this Card's power.  If it's not a creature, it's {@link Double#NaN} and will
     * return <code>false</code> for {@link CombatStat#exists()}.
     *
     * @return a list containing the power of each face of this Card.
     */
    public abstract List<CombatStat> power();

    /**
     * Check if this Card's power is variable, or has a * in it.
     *
     * @return true if this Card has a face that is a creature with variable power, and false
     * otherwise.
     */
    public boolean powerVariable()
    {
        return power().stream().anyMatch(CombatStat::variable);
    }

    /**
     * Get the printed texts of the faces of this Card.
     *
     * @return the printed text of each of this Card's faces in a list.
     */
    public abstract List<String> printedText();

    /**
     * Get the printed types of this Card.
     *
     * @return the printed types of each of this Card's faces in a list.
     */
    public abstract List<String> printedTypes();

    /**
     * Get this Card's rarity.
     *
     * @return This card's rarity.
     */
    public abstract Rarity rarity();

    /**
     * Get any rulings for this Card.
     *
     * @return a map whose keys are dates and whose values are the rulings of this
     * Card on those dates.
     */
    public abstract Map<Date, List<String>> rulings();

    /**
     * Get this Card's subtypes.
     *
     * @return a set containing the subtypes among all faces of this Card.
     */
    public abstract Set<String> subtypes();

    /**
     * Check whether or not this Card has a supertype.
     *
     * @param s supertype to search for
     * @return true if the given String is among this Card's supertypes, case insensitive, and
     * false otherwise.
     */
    public boolean supertypeContains(String s)
    {
        if (Pattern.compile("\\s").matcher(s).find())
            throw new IllegalArgumentException("Supertypes don't contain white space");
        return supertypes().stream().anyMatch((t) -> t.equalsIgnoreCase(s));
    }

    /**
     * Get this Card's supertypes.
     *
     * @return a set containing the supertypes among all the faces of this Card.
     */
    public abstract Set<String> supertypes();

    @Override
    public String toString()
    {
        return unifiedName();
    }

    /**
     * Get this Card's toughness.  If it's not a creature, it's {@link Double#NaN} and will
     * return <code>false</code> for {@link CombatStat#exists()}.
     *
     * @return a list containing the toughness of each face of this Card.
     */
    public abstract List<CombatStat> toughness();

    /**
     * Check if this Card's toughness is variable, or has a * in it.
     *
     * @return true if this Card has a face that is a creature with variable toughness, and
     * false otherwise.
     */
    public boolean toughnessVariable()
    {
        return toughness().stream().anyMatch(CombatStat::variable);
    }

    /**
     * Check whether or not this Card has a card type.
     *
     * @param s type to search for
     * @return true if the given String is among this Card's types, case insensitive, and false
     * otherwise.
     */
    public boolean typeContains(String s)
    {
        if (s.matches("\\s"))
            throw new IllegalArgumentException("Types don't contain white space");
        return types().stream().anyMatch(s::equalsIgnoreCase);
    }

    /**
     * Get this Card's type line formatted as it might appear on a physical card
     * ("[{@link #supertypes()}] [{@link #types()}] {@value UnicodeSymbols#EM_DASH} [{@link #supertypes()}]").
     *
     * @return A list of Strings containing the full, formatted type line of each face.
     */
    public abstract List<String> typeLine();

    /**
     * Get this Card's card types.
     *
     * @return a set containing the types among all the faces of this Card.
     */
    public abstract Set<String> types();

    /**
     * Get a String consisting of the names of each of the faces of this Card
     * concatenated by {@link #FACE_SEPARATOR}.
     *
     * @return the unified name of this Card
     */
    public String unifiedName()
    {
        return name().stream().collect(Collectors.joining(" " + FACE_SEPARATOR + " "));
    }

    /**
     * Get the type lines of this Card's faces separated by {@link #FACE_SEPARATOR}.
     *
     * @return A String consisting of the type lines of all faces of this Card.
     */
    public String unifiedTypeLine()
    {
        return unifiedTypeLine.get();
    }
}
