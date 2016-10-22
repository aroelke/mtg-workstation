package editor.database.card;

import java.awt.datatransfer.DataFlavor;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.PowerToughness;
import editor.database.characteristics.Rarity;
import editor.database.symbol.FunctionalSymbol;
import editor.database.symbol.Symbol;
import editor.gui.MainFrame;

/**
 * This interface represents an abstract Card with various characteristics.
 *
 * TODO: Add printed text in addition to Oracle text.
 * @author Alec Roelke
 */
public abstract class Card
{
	/**
	 * String representing this Card's name in its text box.
	 */
	public static final String THIS = "~";
	/**
	 * Separator string between characteristics of a multi-face card.
	 */
	public static final String FACE_SEPARATOR = "//";
	/**
	 * Separator for card text when displaying multiple cards in a single text box.
	 */
	public static final String TEXT_SEPARATOR = "-----";
	/**
	 * DataFlavor representing cards being transferred.
	 */
	public static final DataFlavor cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Card[].class.getName() + "\"", "Card Array");
	/**
	 * Map of cards onto tags that have been applied to them.
	 */
	public static Map<Card, Set<String>> tags = new HashMap<Card, Set<String>>();


	/**
	 * @return A Set containing all tags among all cards.
	 */
	public static Set<String> tags()
	{
		return tags.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	/**
	 * Expansion this Card belongs to.
	 */
	private final Expansion expansion;
	/**
	 * Layout of this Card.
	 * @see CardLayout
	 */
	private final CardLayout layout;
	/**
	 * Number of faces this Card has.
	 */
	private final int faces;
	/**
	 * Unique identifier for this Card.
	 */
	private String id;
	/**
	 * Name of all faces of this Card separated by separators.
	 */
	private String unifiedName;
	/**
	 * All lower-case, normalized name of this Card with special characters removed.
	 */
	private List<String> normalizedName;
	/**
	 * If this Card is legendary, the name of the character or item depicted by it.  Otherwise,
	 * its normalized name.
	 */
	private List<String> legendName;
	/**
	 * Smallest converted mana cost of all faces of this Card.
	 */
	private double minCmc;
	/**
	 * Type lines of all faces of this Card, separated by separators.
	 */
	private String unifiedTypeLine;
	/**
	 * List of oracle texts of the faces of this Card, converted to lower case and with special
	 * characters removed.
	 */
	private List<String> normalizedOracle;
	/**
	 * List of flavor texts of the faces of this Card, converted to lower case and with special
	 * characters removed.
	 */
	private List<String> normalizedFlavor;
	/**
	 * Whether or not any of this Card's faces has variable power.
	 */
	private Boolean powerVariable;
	/**
	 * Whether or not any of this Card's faces has variable toughness.
	 */
	private Boolean toughnessVariable;
	/**
	 * List of formats this Card is legal in.
	 */
	private List<String> legalIn;
	/**
	 * Whether or not this Card can be a commander.
	 */
	private Boolean canBeCommander;
	/**
	 * Whether or not to ignore the card count restriction for this Card.
	 */
	private Boolean ignoreCountRestriction;

	/**
	 * Create a new Card.  Most of the parameters are assigned lazily; that is, only
	 * the first time their values are requested.
	 *
	 * @param expansion Expension the new Card belongs to
	 * @param layout Layout of the new Card
	 * @param faces Number of faces the new Card has
	 */
	public Card(Expansion expansion, CardLayout layout, int faces)
	{
		this.expansion = expansion;
		this.layout = layout;
		this.faces = faces;

		id = null;
		unifiedName = null;
		normalizedName = null;
		legendName = null;
		minCmc = -1.0;
		unifiedTypeLine = null;
		normalizedOracle = null;
		normalizedFlavor = null;
		powerVariable = null;
		toughnessVariable = null;
		legalIn = null;
		canBeCommander = null;
		ignoreCountRestriction = null;
	}

	/**
	 * TODO: Use the ID field from mtgjson rather than calculating it like this
	 * @return
	 */
	public String id()
	{
		if (id == null)
			id = expansion.code + unifiedName() + imageNames()[0];
		return id;
	}

	/**
	 * @return This Card's layout.
	 */
	public CardLayout layout()
	{
		return layout;
	}

	/**
	 * @return The number of faces this Card has.
	 */
	public int faces()
	{
		return faces;
	}

	/**
	 * @return A List containing the name of each face of this Card.
	 */
	public abstract List<String> name();

	/**
	 * @return A String consisting of the names of each of the faces of this Card
	 * concatenated together with a separator.
	 */
	public String unifiedName()
	{
		if (unifiedName == null)
		{
			StringJoiner join = new StringJoiner(" " + FACE_SEPARATOR + " ");
			for (String name: name())
				join.add(name);
			unifiedName = join.toString();
		}
		return unifiedName;
	}

	/**
	 * @return A version of this Card's name with special characters converted
	 * to versions that would appear on a standard QWERTY keyboard.
	 */
	public List<String> normalizedName()
	{
		if (normalizedName == null)
			normalizedName = Collections.unmodifiableList(name().stream()
					.map((n) -> Normalizer.normalize(n.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("\u00E6", "ae"))
					.collect(Collectors.toList()));
		return normalizedName;
	}

	/**
	 * @return A version of this Card's name with Legendary characteristics removed (such as
	 * titles following "the," "of," or a comma).
	 */
	public List<String> legendName()
	{
		if (legendName == null)
		{
			List<String> legendNames = new ArrayList<String>();
			for (String fullName: normalizedName())
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
			legendName = Collections.unmodifiableList(legendNames);
		}
		return legendName;
	}

	/**
	 * Compare this Card's unified name lexicographically with another's.
	 *
	 * @param other Card to compare names with
	 * @return A positive number if this Card's name comes after the other's, 0 if
	 * they are the same, or a negative number if it comes before.
	 */
	public int compareName(Card other)
	{
		return Collator.getInstance(Locale.US).compare(unifiedName(), other.unifiedName());
	}

	/**
	 * @return A list containing the mana costs of the faces of this Card.
	 */
	public abstract ManaCost.Tuple manaCost();

	/**
	 * @return A list containing the converted mana costs of the faces of this Card.
	 */
	public abstract List<Double> cmc();

	/**
	 * @return The smallest converted mana cost among faces of this card.
	 */
	public double minCmc()
	{
		if (minCmc < 0.0)
			minCmc = cmc().stream().reduce(Double.MAX_VALUE,Double::min);
		return minCmc;
	}

	/**
	 * @return A list containing the colors of this Card.
	 */
	public abstract ManaType.Tuple colors();

	/**
	 * Get the colors of one of the faces of this Card.
	 *
	 * @param face Index of the face to get the colors of
	 * @return A list containing the colors of the given face.
	 */
	public abstract ManaType.Tuple colors(int face);

	/**
	 * @return A list containing the colors in this Card's color identity.
	 */
	public abstract ManaType.Tuple colorIdentity();

	/**
	 * @return The Expansion this Card belongs to.
	 */
	public Expansion expansion()
	{
		return expansion;
	}

	/**
	 * @return A set containing the supertypes among all the faces of this Card.
	 */
	public abstract Set<String> supertypes();

	/**
	 * @param s Supertype to search for
	 * @return <code>true</code> if the given String is among this Card's
	 * supertypes, case insensitive, and <code>false</code> otherwise.
	 */
	public boolean supertypeContains(String s)
	{
		if (Pattern.compile("\\s").matcher(s).find())
			throw new IllegalArgumentException("Supertypes don't contain white space");
		for (String supertype: supertypes())
			if (s.equalsIgnoreCase(supertype))
				return true;
		return false;
	}

	/**
	 * @return A set containing the subtypes among all the faces of this Card.
	 */
	public abstract Set<String> types();

	/**
	 * @param s Type to search for
	 * @return <code>true</code> if the given String is among this Card's types,
	 * case insensitive, and <code>false</code> otherwise.
	 */
	public boolean typeContains(String s)
	{
		if (Pattern.compile("\\s").matcher(s).find())
			throw new IllegalArgumentException("Types don't contain white space");
		for (String type: types())
			if (s.equalsIgnoreCase(type))
				return true;
		return false;
	}

	/**
	 * @return A set containing the subtypes among all faces of this Card.
	 */
	public abstract Set<String> subtypes();

	/**
	 * @return A list of sets of Strings which each contain all the supertypes,
	 * types, and subtypes of the corresponding face.
	 */
	public abstract List<Set<String>> allTypes();

	/**
	 * @return A list of Strings containing the full, formatted type line of each
	 * face.
	 */
	public abstract List<String> typeLine();

	/**
	 * @return A String consisting of the type lines of each face of this Card
	 * joined with a separator.
	 */
	public String unifiedTypeLine()
	{
		if (unifiedTypeLine == null)
		{
			StringJoiner join = new StringJoiner(" " + FACE_SEPARATOR + " ");
			for (String line: typeLine())
				join.add(line);
			unifiedTypeLine = join.toString();
		}
		return unifiedTypeLine;
	}

	/**
	 * @return This Card's Rarity.
	 */
	public abstract Rarity rarity();

	/**
	 * @return The Oracle text of each of this Card's faces in a list.
	 */
	public abstract List<String> oracleText();

	/**
	 * @return A list containing the Oracle text of each of this Card's faces with special characters
	 * replaced by versions that appear on a standard QWERTY keyboard.
	 */
	public List<String> normalizedOracle()
	{
		if (normalizedOracle == null)
		{
			List<String> texts = new ArrayList<String>();
			for (int i = 0; i < faces; i++)
			{
				String normal = Normalizer.normalize(oracleText()[i].toLowerCase(), Normalizer.Form.NFD);
				normal = normal.replaceAll("\\p{M}", "").replace("\u00E6", "ae");
				normal = normal.replace(legendName()[i], Card.THIS).replace(normalizedName()[i], Card.THIS);
				texts.add(normal);
			}
			normalizedOracle = Collections.unmodifiableList(texts);
		}
		return normalizedOracle;
	}

	/**
	 * @return A list containing the flavor text of each face of this Card.
	 */
	public abstract List<String> flavorText();

	/**
	 * @return A list containing the flavor text of each of this Card's faces with special characters
	 * replaced by versions that appear on a standard QWERTY keyboard.
	 */
	public List<String> normalizedFlavor()
	{
		if (normalizedFlavor == null)
			normalizedFlavor = Collections.unmodifiableList(flavorText().stream()
					.map((f) -> Normalizer.normalize(f.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("\u00E6", "ae"))
					.collect(Collectors.toList()));
		return normalizedFlavor;
	}

	/**
	 * @return A list containing the artist of each face of this Card.
	 */
	public abstract List<String> artist();

	/**
	 * @return A list containing the collector's number of each face of this Card.
	 */
	public abstract List<String> number();

	/**
	 * @return A list containing the power of each face of this Card (that's a creature).
	 */
	public abstract PowerToughness.Tuple power();

	/**
	 * @return <code>true</code> if this Card has a face that is a creature with variable
	 * power (a * value), and <code>false</code> otherwise.
	 */
	public boolean powerVariable()
	{
		if (powerVariable == null)
			powerVariable = power().stream().anyMatch(PowerToughness::variable);
		return powerVariable;
	}

	/**
	 * @return A list containing the toughness of each face of this Card (that's a creature).
	 */
	public abstract PowerToughness.Tuple toughness();

	/**
	 * @return <code>true</code> if this Card has a face that is a creature with variable
	 * toughness (a * value), and <code>false</code> otherwise.
	 */
	public boolean toughnessVariable()
	{
		if (toughnessVariable == null)
			toughnessVariable = toughness().stream().anyMatch(PowerToughness::variable);
		return toughnessVariable;
	}

	/**
	 * @return A list containing the loyalty of each face of this Card (that's a
	 * planeswalker).
	 */
	public abstract Loyalty.Tuple loyalty();

	/**
	 * @return A map whose keys are dates and whose values are the rulings of this
	 * Card on those dates.
	 */
	public abstract Map<Date, List<String>> rulings();

	/**
	 * @return A map whose keys are format names and whose values are the Legalities
	 * of this Card in those formats.
	 */
	public abstract Map<String, Legality> legality();

	/**
	 * @param format Format to look up
	 * @return <code>true</code> if the given format exists and this Card is legal in it, and
	 * <code>false</code> otherwise.
	 */
	public boolean legalIn(String format)
	{
		if (format.equalsIgnoreCase("prismatic") && legalIn("classic") && legality()[format] != Legality.BANNED)
			return true;
		else if (format.equalsIgnoreCase("classic") || format.equalsIgnoreCase("freeform"))
			return true;
		else if (format.contains("Block"))
		{
			format = format.substring(0, format.indexOf("Block")).trim();
			if (expansion.block.equalsIgnoreCase(format))
				return true;
			else if (format.equalsIgnoreCase("urza") && expansion.block.equalsIgnoreCase("urza's"))
				return true;
			else if (format.equalsIgnoreCase("lorwyn-shadowmoor") && (expansion.block.equalsIgnoreCase("lorwyn") || expansion.block.equalsIgnoreCase("shadowmoor")))
				return true;
			else if (format.equalsIgnoreCase("shards of alara") && expansion.block.equalsIgnoreCase("alara"))
				return true;
			else if (format.equalsIgnoreCase("tarkir") && expansion.block.equalsIgnoreCase("khans of tarkir"))
				return true;
			else
				return false;
		}
		else if (!legality().containsKey(format))
			return false;
		else
			return legality()[format] != Legality.BANNED;
	}

	/**
	 * @return A list of formats that this Card is legal in.
	 */
	public List<String> legalIn()
	{
		if (legalIn == null)
			legalIn = Collections.unmodifiableList(legality().keySet().stream().filter(this::legalIn).collect(Collectors.toList()));
		return legalIn;
	}

	/**
	 * @param format Format to look up
	 * @return The legality (legal, restricted, banned) of this Card in the given format.
	 */
	public Legality legalityIn(String format)
	{
		if (legalIn(format))
		{
			if (format.equalsIgnoreCase("prismatic"))
				format = "classic";
			return legality().containsKey(format) ? legality()[format] : Legality.LEGAL;
		}
		else
			return Legality.BANNED;
	}

	/**
	 * @return A list containing the name of each image corresponding to a face of this Card.
	 */
	public abstract List<String> imageNames();

	/**
	 * @return <code>true</code> if this Card can be a commander in the Commander format, and
	 * <code>false</code> otherwise.
	 */
	public boolean canBeCommander()
	{
		if (canBeCommander == null)
			canBeCommander = supertypeContains("legendary") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("can be your commander"));
		return canBeCommander;
	}

	/**
	 * @return <code>true</code> if there can be any number of copies of this Card in a deck,
	 * and <code>false</code> otherwise.
	 */
	public boolean ignoreCountRestriction()
	{
		if (ignoreCountRestriction == null)
			ignoreCountRestriction = supertypeContains("basic") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("a deck can have any number"));
		return ignoreCountRestriction;
	}

	/**
	 * Add text and icons to the given document so it contains a nice-looking version of this
	 * Card's Oracle text.  The document is expected to have styles "text" and "reminder."
	 *
	 * @param document Document to format
	 * @param f Face to add to the document
	 */
	public void formatDocument(StyledDocument document, int f)
	{
		Style textStyle = document.getStyle("text");
		Style reminderStyle = document.getStyle("reminder");
		Style chaosStyle = document.addStyle("CHAOS", null);
		StyleConstants.setIcon(chaosStyle, FunctionalSymbol.CHAOS.getIcon(MainFrame.TEXT_SIZE));
		try
		{
			document.insertString(document.getLength(), name()[f] + " ", textStyle);
			if (!manaCost()[f].isEmpty())
			{
				for (Symbol symbol: manaCost()[f])
				{
					Style style = document.addStyle(symbol.toString(), null);
					StyleConstants.setIcon(style, symbol.getIcon(MainFrame.TEXT_SIZE));
					document.insertString(document.getLength(), symbol.toString(), style);
				}
				document.insertString(document.getLength(), " ", textStyle);
			}
			if (cmc()[f] == cmc()[f].doubleValue())
				document.insertString(document.getLength(), "(" + (int)cmc()[f].doubleValue() + ")\n", textStyle);
			else
				document.insertString(document.getLength(), "(" + cmc()[f] + ")\n", textStyle);
			if (!manaCost()[f].colors().equals(colors(f)))
			{
				for (ManaType color: colors(f))
				{
					Style indicatorStyle = document.addStyle("indicator", document.getStyle("text"));
					StyleConstants.setForeground(indicatorStyle, color.color);
					document.insertString(document.getLength(), "•", indicatorStyle);
				}
				if (!colors().isEmpty())
					document.insertString(document.getLength(), " ", textStyle);
			}
			document.insertString(document.getLength(), typeLine()[f] + '\n', textStyle);
			document.insertString(document.getLength(), expansion.name + ' ' + rarity() + '\n', textStyle);

			String oracle = oracleText()[f];
			if (!oracle.isEmpty())
			{
				int start = 0;
				Style style = textStyle;
				for (int i = 0; i < oracle.length(); i++)
				{
					switch (oracle.charAt(i))
					{
					case '{':
						document.insertString(document.getLength(), oracle.substring(start, i), style);
						start = i + 1;
						break;
					case '}':
						Symbol symbol = Symbol.get(oracle.substring(start, i));
						if (symbol == null)
						{
							System.err.println("Unexpected symbol {" + oracle.substring(start, i) + "} in oracle text for " + unifiedName() + ".");
							document.insertString(document.getLength(), oracle.substring(start, i), textStyle);
						}
						else
						{
							Style symbolStyle = document.addStyle(symbol.toString(), null);
							StyleConstants.setIcon(symbolStyle, symbol.getIcon(MainFrame.TEXT_SIZE));
							document.insertString(document.getLength(), symbol.toString(), symbolStyle);
						}
						start = i + 1;
						break;
					case '(':
						document.insertString(document.getLength(), oracle.substring(start, i), style);
						style = reminderStyle;
						start = i;
						break;
					case ')':
						document.insertString(document.getLength(), oracle.substring(start, i + 1), style);
						style = textStyle;
						start = i + 1;
						break;
					case 'C':
						if (oracle.substring(i, i + 5).equals("CHAOS"))
						{
							document.insertString(document.getLength(), oracle.substring(start, i), style);
							document.insertString(document.getLength(), "CHAOS", chaosStyle);
							start = i += 5;
						}
						break;
					default:
						break;
					}
					if (i == oracle.length() - 1 && oracle.charAt(i) != '}' && oracle.charAt(i) != ')')
						document.insertString(document.getLength(), oracle.substring(start, i + 1), style);
				}
				document.insertString(document.getLength(), "\n", textStyle);
			}
			String flavor = flavorText()[f];
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
						Symbol symbol = Symbol.get(flavor.substring(start, i));
						if (symbol == null)
						{
							System.err.println("Unexpected symbol {" + flavor.substring(start, i) + "} in flavor text for " + unifiedName() + ".");
							document.insertString(document.getLength(), flavor.substring(start, i), reminderStyle);
						}
						else
						{
							Style symbolStyle = document.addStyle(symbol.toString(), null);
							StyleConstants.setIcon(symbolStyle, symbol.getIcon(MainFrame.TEXT_SIZE));
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

			if (!Double.isNaN(power()[f].value) && !Double.isNaN(toughness()[f].value))
				document.insertString(document.getLength(), power()[f] + "/" + toughness()[f] + "\n", textStyle);
			else if (loyalty()[f].value > 0)
				document.insertString(document.getLength(), loyalty()[f] + "\n", textStyle);

			document.insertString(document.getLength(), artist()[f] + " " + number()[f] + "/" + expansion.count, textStyle);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Add the Oracle text of all of this Card's faces to the given document, separated
	 * by a separator on its own line.
	 *
	 * @param document Document to add text to
	 */
	public void formatDocument(StyledDocument document)
	{
		Style textStyle = document.getStyle("text");
		try
		{
			for (int f = 0; f < faces; f++)
			{
				formatDocument(document, f);
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
	 * @return A String representation of this Card to be used in tables.
	 */
	@Override
	public String toString()
	{
		return unifiedName();
	}

	/**
	 * @return This Card's hash code.
	 */
	@Override
	public int hashCode()
	{
		return id().hashCode();
	}

	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if this Card and the other Object both represent the exact same
	 * card, and <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof Card))
			return false;
		return id().equals(((Card)other).id());
	}
}
