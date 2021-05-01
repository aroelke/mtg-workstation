package editor.database.card;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import editor.util.UnicodeSymbols;

/**
 * This class represents a single-faced Magic: the Gathering card.  All of the
 * methods that return sets of attributes will only contain one value.
 *
 * @author Alec Roelke
 */
public class SingleCard extends Card
{
    /**
     * Set containing all of the types of this SingleCard.
     */
    public final Set<String> allTypes;
    /**
     * This SingleCard's artist.
     */
    public final String artist;
    /**
     * This SingleCard's color identity.
     *
     * @see Card#colorIdentity()
     */
    private final List<ManaType> colorIdentity;
    /**
     * This SingleCard's colors.
     */
    public final List<ManaType> colors;
    /**
     * List of formats this SingleCard can be commander in.
     */
    public final List<String> commandFormats;
    /**
     * This SingleCard's flavor text.
     */
    public final String flavor;
    /**
     * This SingleCard's image name.
     */
    public final String imageName;
    /**
     * Whether or not this SingleCard is a land card.
     */
    public final boolean isLand;
    /**
     * Formats and legality for this Card.
     */
    private final Map<String, Legality> legality;
    /**
     * This SingleCard's loyalty.
     */
    public final Loyalty loyalty;
    /**
     * Mana cost of this SingleCard.
     */
    public final ManaCost mana;
    /**
     * multiverseid of this SingleCard.
     */
    public final int multiverseid;
    /**
     * Scryfall illustration ID of this SingleCard.
     */
    public final String scryfallid;
    /**
     * Name of this SingleCard.
     */
    public final String name;
    /**
     * This SingleCard's collector's number.
     */
    public final String number;
    /**
     * This SingleCard's power.
     *
     * @see Card#power()
     */
    public final CombatStat power;
    /**
     * The text actually printed on the card.
     */
    public final String printed;
    /**
     * The type line actually printed on the card.
     */
    public final String printedTypes;
    /**
     * This Card's rarity.
     */
    private final Rarity rarity;
    /**
     * Rulings for this Card.
     */
    private final Map<Date, List<String>> rulings;
    /**
     * This SingleCard's subtypes.
     */
    public final Set<String> subtypes;
    /**
     * This SingleCard's supertypes.
     */
    public final Set<String> supertypes;
    /**
     * This SingleCard's rules text.
     */
    public final String text;
    /**
     * This SingleCard's toughness.
     *
     * @see Card#toughness()
     */
    public final CombatStat toughness;
    /**
     * This SingleCard's type line.
     *
     * @see Card#typeLine()
     */
    public final String typeLine;
    /**
     * This SingleCard's types.
     */
    public final Set<String> types;

    /**
     * Create a new Card with a single face.
     *
     * @param layout the new Card's layout
     * @param name the new Card's name
     * @param mana the new Card's mana cost
     * @param colors the new Card's colors
     * @param colorIdentity the new Card's color identity
     * @param supertype the new Card's supertypes
     * @param type the new Card's types
     * @param subtype the new Card's subtypes
     * @param printedTypes the new Card's printed type line
     * @param rarity the new Card's rarity
     * @param set the Expansion the new Card belongs to
     * @param text the new Card's rules text
     * @param flavor the new Card's flavor text
     * @param printed the new Card's printed text
     * @param artist the new Card's artist
     * @param multiverseid the new Card's multiverse ID
     * @param scryfallid the new Card's Scryfall illustration ID
     * @param number the new Card's collector's number
     * @param power the new Card's power
     * @param toughness the new Card's toughness
     * @param loyalty the new Card's loyalty (0 or less for nonexistent)
     * @param legality the new Card's legality
     * @param command the new Card's command formats
     */
    public SingleCard(CardLayout layout,
                      String name,
                      ManaCost mana,
                      List<ManaType> colors,
                      List<ManaType> colorIdentity,
                      Set<String> supertype,
                      Set<String> type,
                      Set<String> subtype,
                      String printedTypes,
                      Rarity rarity,
                      Expansion set,
                      String text,
                      String flavor,
                      String printed,
                      String artist,
                      int multiverseid,
                      String scryfallid,
                      String number,
                      CombatStat power,
                      CombatStat toughness,
                      Loyalty loyalty,
                      TreeMap<Date, List<String>> rulings,
                      Map<String, Legality> legality,
                      List<String> command)
    {
        super(set, layout, 1);

        this.name = name;
        this.mana = mana;
        this.colors = Collections.unmodifiableList(colors);
        this.colorIdentity = Collections.unmodifiableList(colorIdentity);
        this.supertypes = Collections.unmodifiableSet(supertype);
        this.types = Collections.unmodifiableSet(type);
        this.subtypes = Collections.unmodifiableSet(subtype);
        this.printedTypes = printedTypes;
        this.text = text;
        this.flavor = flavor;
        this.printed = printed;
        this.artist = artist;
        this.number = number;
        this.multiverseid = multiverseid;
        this.scryfallid = scryfallid;
        this.power = power;
        this.toughness = toughness;
        this.loyalty = loyalty;
        this.imageName = name.toLowerCase();
        this.rarity = rarity;
        this.rulings = Collections.unmodifiableMap(rulings);
        this.legality = Collections.unmodifiableMap(legality);
        this.commandFormats = Collections.unmodifiableList(command);

        isLand = typeContains("land");

        // Create the type line for this Card
        StringBuilder str = new StringBuilder();
        if (supertypes.size() > 0)
            str.append(String.join(" ", supertypes)).append(" ");
        str.append(String.join(" ", types));
        if (subtypes.size() > 0)
            str.append(" " + UnicodeSymbols.EM_DASH + " ").append(String.join(" ", subtypes));
        typeLine = str.toString();

        var faceTypes = new HashSet<String>();
        faceTypes.addAll(supertypes);
        faceTypes.addAll(types);
        faceTypes.addAll(subtypes);
        allTypes = Collections.unmodifiableSet(faceTypes);
    }

    @Override
    public List<Set<String>> allTypes()
    {
        return Collections.singletonList(allTypes);
    }

    @Override
    public List<String> artist()
    {
        return Collections.singletonList(artist);
    }

    @Override
    public double manaValue()
    {
        return mana.manaValue();
    }

    @Override
    public double minManaValue()
    {
        return manaValue();
    }

    @Override
    public double maxManaValue()
    {
        return manaValue();
    }

    @Override
    public double avgManaValue()
    {
        return manaValue();
    }

    @Override
    public List<ManaType> colorIdentity()
    {
        return colorIdentity;
    }

    @Override
    public List<ManaType> colors()
    {
        return colors;
    }

    /**
     * {@inheritDoc}
     * This returns the same thing as {@link #colors()}.
     *
     * @throw IndexOutOfBoundsException if face is not equal to 0, since SingleCards only have
     * one face
     */
    @Override
    public List<ManaType> colors(int face) throws IndexOutOfBoundsException
    {
        if (face != 0)
            throw new IndexOutOfBoundsException("Single-faced cards only have one face");
        return colors;
    }

    @Override
    public List<String> commandFormats()
    {
        return commandFormats;
    }

    @Override
    public List<String> flavorText()
    {
        return Collections.singletonList(flavor);
    }

    @Override
    public List<String> imageNames()
    {
        return Collections.singletonList(imageName);
    }

    @Override
    public boolean isLand()
    {
        return isLand;
    }

    @Override
    public Map<String, Legality> legality()
    {
        return legality;
    }

    @Override
    public List<Loyalty> loyalty()
    {
        return Collections.singletonList(loyalty);
    }

    @Override
    public List<ManaCost> manaCost()
    {
        return Collections.singletonList(mana);
    }

    @Override
    public List<String> name()
    {
        return Collections.singletonList(name);
    }

    @Override
    public List<String> number()
    {
        return Collections.singletonList(number);
    }

    @Override
    public List<Integer> multiverseid()
    {
        return Collections.singletonList(multiverseid);
    }

    @Override
    public List<String> scryfallid()
    {
        return Collections.singletonList(scryfallid);
    }

    @Override
    public List<String> oracleText()
    {
        return Collections.singletonList(text);
    }

    @Override
    public List<CombatStat> power()
    {
        return Collections.singletonList(power);
    }

    @Override
    public List<String> printedText()
    {
        return Collections.singletonList(printed);
    }

    @Override
    public List<String> printedTypes()
    {
        return Collections.singletonList(printedTypes);
    }

    @Override
    public Rarity rarity()
    {
        return rarity;
    }

    @Override
    public Map<Date, List<String>> rulings()
    {
        return rulings;
    }

    @Override
    public Set<String> subtypes()
    {
        return subtypes;
    }

    @Override
    public Set<String> supertypes()
    {
        return supertypes;
    }

    @Override
    public List<CombatStat> toughness()
    {
        return Collections.singletonList(toughness);
    }

    @Override
    public List<String> typeLine()
    {
        return Collections.singletonList(typeLine);
    }

    @Override
    public Set<String> types()
    {
        return types;
    }

    @Override
    public void formatDocument(StyledDocument document, boolean printed)
    {
        Style textStyle = document.getStyle("text");
        Style reminderStyle = document.getStyle("reminder");
        Style chaosStyle = document.addStyle("CHAOS", null);
        StyleConstants.setIcon(chaosStyle, FunctionalSymbol.CHAOS.getIcon(ComponentUtils.TEXT_SIZE));
        try
        {
            document.insertString(document.getLength(), name + " ", textStyle);
            if (!mana.isEmpty())
            {
                for (Symbol symbol : mana)
                {
                    Style style = document.addStyle(symbol.toString(), null);
                    StyleConstants.setIcon(style, symbol.getIcon(ComponentUtils.TEXT_SIZE));
                    document.insertString(document.getLength(), symbol.toString(), style);
                }
                document.insertString(document.getLength(), " ", textStyle);
            }
            if (mana.manaValue() == (int)mana.manaValue())
                document.insertString(document.getLength(), "(" + (int)mana.manaValue() + ")\n", textStyle);
            else
                document.insertString(document.getLength(), "(" + mana.manaValue() + ")\n", textStyle);
            if (!mana.colors().equals(colors))
            {
                for (ManaType color : colors)
                {
                    Style indicatorStyle = document.addStyle("indicator", document.getStyle("text"));
                    StyleConstants.setForeground(indicatorStyle, color.color);
                    document.insertString(document.getLength(), String.valueOf(UnicodeSymbols.BULLET), indicatorStyle);
                }
                if (!colors().isEmpty())
                    document.insertString(document.getLength(), " ", textStyle);
            }
            if (printed)
                document.insertString(document.getLength(), printedTypes + '\n', textStyle);
            else
                document.insertString(document.getLength(), typeLine + '\n', textStyle);
            document.insertString(document.getLength(), expansion().name() + ' ' + rarity() + '\n', textStyle);

            String abilities = printed ? this.printed : text;
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

            if (power.exists() && toughness.exists())
                document.insertString(document.getLength(), power + "/" + toughness + "\n", textStyle);
            else if (loyalty.exists())
                document.insertString(document.getLength(), loyalty + "\n", textStyle);

            document.insertString(document.getLength(), artist + " " + number + "/" + expansion().count(), textStyle);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void formatDocument(StyledDocument document, boolean printed, int face)
    {
        formatDocument(document, printed);
    }
}
