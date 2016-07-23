package editor.database.card;

import java.awt.datatransfer.DataFlavor;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
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
import editor.database.symbol.StaticSymbol;
import editor.database.symbol.Symbol;
import editor.gui.MainFrame;

/**
 * This interface represents an abstract Card with various characteristics.
 * 
 * TODO: If there are any performance issues, try pre-collecting values
 * TODO: Add a user-controlled tag to cards.
 * TODO: Add printed text in addition to Oracle text.
 * @author Alec Roelke
 */
public interface Card
{
	/**
	 * String representing this Card's name in its text box.
	 */
	public String THIS = "~";
	/**
	 * Separator string between characteristics of a multi-face card.
	 */
	public String FACE_SEPARATOR = "//";
	/**
	 * Separator for card text when displaying multiple cards in a single text box.
	 */
	public String TEXT_SEPARATOR = "-----";
	/**
	 * DataFlavor representing cards being transferred.
	 */
	public DataFlavor cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Card[].class.getName() + "\"", "Card Array");

	/**
	 * TODO: Use the ID field from mtgjson rather than calculating it like this
	 * @return This Card's id, which is its expansion code, unified name, and
	 * first image name concatenated together.
	 */
	public default String id()
	{
		return expansion().code + unifiedName() + imageNames().get(0);
	}
	
	/**
	 * @return The CardLayout of this Card.
	 */
	public CardLayout layout();
	
	/**
	 * @return The number of faces this Card has.
	 */
	public int faces();
	
	/**
	 * @return A List containing the name of each face of this Card.
	 */
	public List<String> name();
	
	/**
	 * @return A String consisting of the names of each of the faces of this Card
	 * concatenated together with a separator.
	 */
	public default String unifiedName()
	{
		StringJoiner join = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (String name: name())
			join.add(name);
		return join.toString();
	}
	
	/**
	 * @return A version of this Card's name with special characters converted
	 * to versions that would appear on a standard QWERTY keyboard.
	 */
	public default List<String> normalizedName()
	{
		return name().stream()
				.map((n) -> Normalizer.normalize(n.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("æ", "ae"))
				.collect(Collectors.toList());
	}
	
	/**
	 * @return A version of this Card's name with Legendary characteristics removed (such as
	 * titles following "the," "of," or a comma). 
	 */
	public default List<String> legendName()
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
		return legendNames;
	}
	
	/**
	 * Compare this Card's unified name lexicographically with another's.
	 * 
	 * @param other Card to compare names with
	 * @return A positive number if this Card's name comes after the other's, 0 if
	 * they are the same, or a negative number if it comes before.
	 */
	public default int compareName(Card other)
	{
		return Collator.getInstance(Locale.US).compare(unifiedName(), other.unifiedName());
	}
	
	/**
	 * @return A list containing the mana costs of the faces of this Card.
	 */
	public ManaCost.Tuple manaCost();
	
	/**
	 * @return A list containing the converted mana costs of the faces of this Card.
	 */
	public List<Double> cmc();
	
	/**
	 * @return The smallest converted mana cost among faces of this card.
	 */
	public default double minCmc()
	{
		return cmc().stream().reduce(Double.MAX_VALUE,Double::min);
	}
	
	/**
	 * @return A list containing the colors of this Card.
	 */
	public ManaType.Tuple colors();
	
	/**
	 * @return A list containing the colors in this Card's color identity.
	 */
	public ManaType.Tuple colorIdentity();
	
	/**
	 * @return A set containing the supertypes among all the faces of this Card.
	 */
	public Set<String> supertypes();
	
	/**
	 * @param s Supertype to search for
	 * @return <code>true</code> if the given String is among this Card's
	 * supertypes, case insensitive, and <code>false</code> otherwise.
	 */
	public default boolean supertypeContains(String s)
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
	public Set<String> types();
	
	/**
	 * @param s Type to search for
	 * @return <code>true</code> if the given String is among this Card's types,
	 * case insensitive, and <code>false</code> otherwise.
	 */
	public default boolean typeContains(String s)
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
	public Set<String> subtypes();
	
	/**
	 * @return A list of sets of Strings which each contain all the supertypes,
	 * types, and subtypes of the corresponding face.
	 */
	public List<Set<String>> allTypes();
	
	/**
	 * @return A list of Strings containing the full, formatted type line of each
	 * face.
	 */
	public List<String> typeLine();
	
	/**
	 * @return A String consisting of the type lines of each face of this Card
	 * joined with a separator.
	 */
	public default String unifiedTypeLine()
	{
		StringJoiner join = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (String line: typeLine())
			join.add(line);
		return join.toString();
	}
	
	/**
	 * @return This Card's Expansion.
	 */
	public Expansion expansion();
	
	/**
	 * @return This Card's Rarity.
	 */
	public Rarity rarity();
	
	/**
	 * @return The Oracle text of each of this Card's faces in a list.
	 */
	public List<String> oracleText();
	
	/**
	 * @return A list containing the Oracle text of each of this Card's faces with special characters
	 * replaced by versions that appear on a standard QWERTY keyboard.
	 */
	public default List<String> normalizedOracle()
	{
		List<String> texts = new ArrayList<String>();
		for (int i = 0; i < faces(); i++)
		{
			String normal = Normalizer.normalize(oracleText().get(i).toLowerCase(), Normalizer.Form.NFD);
			normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
			normal = normal.replace(legendName().get(i), Card.THIS).replace(normalizedName().get(i), Card.THIS);
			texts.add(normal);
		}
		return texts;
	}
	
	/**
	 * @return A list containing the flavor text of each face of this Card.
	 */
	public List<String> flavorText();
	
	/**
	 * @return A list containing the flavor text of each of this Card's faces with special characters
	 * replaced by versions that appear on a standard QWERTY keyboard.
	 */
	public default List<String> normalizedFlavor()
	{
		return flavorText().stream()
				.map((f) -> Normalizer.normalize(f.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("æ", "ae"))
				.collect(Collectors.toList());
	}
	
	/**
	 * @return A list containing the artist of each face of this Card.
	 */
	public List<String> artist();
	
	/**
	 * @return A list containing the collector's number of each face of this Card.
	 */
	public List<String> number();
	
	/**
	 * @return A list containing the power of each face of this Card (that's a creature).
	 */
	public PowerToughness.Tuple power();
	
	/**
	 * @return <code>true</code> if this Card has a face that is a creature with variable
	 * power (a * value), and <code>false</code> otherwise.
	 */
	public default boolean powerVariable()
	{
		return power().stream().anyMatch(PowerToughness::variable);
	}
	
	/**
	 * @return A list containing the toughness of each face of this Card (that's a creature).
	 */
	public PowerToughness.Tuple toughness();
	
	/**
	 * @return <code>true</code> if this Card has a face that is a creature with variable
	 * toughness (a * value), and <code>false</code> otherwise.
	 */
	public default boolean toughnessVariable()
	{
		return toughness().stream().anyMatch(PowerToughness::variable);
	}
	
	/**
	 * @return A list containing the loyalty of each face of this Card (that's a
	 * planeswalker).
	 */
	public Loyalty.Tuple loyalty();
	
	/**
	 * @return A map whose keys are dates and whose values are the rulings of this
	 * Card on those dates.
	 */
	public Map<Date, List<String>> rulings();
	
	/**
	 * @return A map whose keys are format names and whose values are the Legalities
	 * of this Card in those formats.
	 */
	public Map<String, Legality> legality();
	
	/**
	 * @param format Format to look up
	 * @return <code>true</code> if the given format exists and this Card is legal in it, and
	 * <code>false</code> otherwise.
	 */
	public default boolean legalIn(String format)
	{
		if (format.equalsIgnoreCase("prismatic") && legalIn("classic") && legality().get(format) != Legality.BANNED)
			return true;
		else if (format.equalsIgnoreCase("classic") || format.equalsIgnoreCase("freeform"))
			return true;
		else if (format.contains("Block"))
		{
			format = format.substring(0, format.indexOf("Block")).trim();
			if (expansion().block.equalsIgnoreCase(format))
				return true;
			else if (format.equalsIgnoreCase("urza") && expansion().block.equalsIgnoreCase("urza's"))
				return true;
			else if (format.equalsIgnoreCase("lorwyn-shadowmoor") && (expansion().block.equalsIgnoreCase("lorwyn") || expansion().block.equalsIgnoreCase("shadowmoor")))
				return true;
			else if (format.equalsIgnoreCase("shards of alara") && expansion().block.equalsIgnoreCase("alara"))
				return true;
			else if (format.equalsIgnoreCase("tarkir") && expansion().block.equalsIgnoreCase("khans of tarkir"))
				return true;
			else
				return false;
		}
		else if (!legality().containsKey(format))
			return false;
		else
			return legality().get(format) != Legality.BANNED;
	}
	
	/**
	 * @return A list of formats that this Card is legal in.
	 */
	public default List<String> legalIn()
	{
		return legality().keySet().stream().filter(this::legalIn).collect(Collectors.toList());
	}
	
	/**
	 * @param format Format to look up
	 * @return The legality (legal, restricted, banned) of this Card in the given format.
	 */
	public default Legality legalityIn(String format)
	{
		if (legalIn(format))
		{
			if (format.equalsIgnoreCase("prismatic"))
				format = "classic";
			return legality().containsKey(format) ? legality().get(format) : Legality.LEGAL;
		}
		else
			return Legality.BANNED;
	}
	
	/**
	 * @return A list containing the name of each image corresponding to a face of this Card.
	 */
	public List<String> imageNames();
	
	/**
	 * @return <code>true</code> if this Card can be a commander in the Commander format, and
	 * <code>false</code> otherwise.
	 */
	public default boolean canBeCommander()
	{
		return supertypeContains("legendary") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("can be your commander"));
	}
	
	/**
	 * @return <code>true</code> if there can be any number of copies of this Card in a deck,
	 * and <code>false</code> otherwise.
	 */
	public default boolean ignoreCountRestriction()
	{
		return supertypeContains("basic") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("a deck can have any number"));
	}
	
	/**
	 * Add text and icons to the given document so it contains a nice-looking version of this
	 * Card's Oracle text.  The document is expected to have styles "text" and "reminder."
	 * 
	 * @param document Document to format
	 * @param f Face to add to the document
	 */
	public default void formatDocument(StyledDocument document, int f)
	{
		Style textStyle = document.getStyle("text");
		Style reminderStyle = document.getStyle("reminder");
		Style chaosStyle = document.addStyle("CHAOS", null);
		StyleConstants.setIcon(chaosStyle, StaticSymbol.CHAOS.getIcon(MainFrame.TEXT_SIZE));
		try
		{
			document.insertString(document.getLength(), name().get(f) + " ", textStyle);
			if (!manaCost().get(f).isEmpty())
			{
				for (Symbol symbol: manaCost().get(f))
				{
					Style style = document.addStyle(symbol.toString(), null);
					StyleConstants.setIcon(style, symbol.getIcon(MainFrame.TEXT_SIZE));
					document.insertString(document.getLength(), symbol.toString(), style);
				}
				document.insertString(document.getLength(), " ", textStyle);
			}
			if (cmc().get(f) == cmc().get(f).doubleValue())
				document.insertString(document.getLength(), "(" + (int)cmc().get(f).doubleValue() + ")\n", textStyle);
			else
				document.insertString(document.getLength(), "(" + cmc().get(f) + ")\n", textStyle);
			document.insertString(document.getLength(), typeLine().get(f) + '\n', textStyle);
			document.insertString(document.getLength(), expansion().name + ' ' + rarity() + '\n', textStyle);
			
			String oracle = oracleText().get(f);
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
						Symbol symbol = Symbol.valueOf(oracle.substring(start, i));
						if (symbol == null)
						{
							System.err.println("Unexpected symbol {" + oracle.substring(start, i) + "} in ruling for " + unifiedName() + ".");
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
						Symbol symbol = Symbol.valueOf(flavor.substring(start, i));
						if (symbol == null)
						{
							System.err.println("Unexpected symbol {" + flavor.substring(start, i) + "} in ruling for " + unifiedName() + ".");
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
			
			if (!Double.isNaN(power().get(f).value) && !Double.isNaN(toughness().get(f).value))
				document.insertString(document.getLength(), power().get(f) + "/" + toughness().get(f) + "\n", textStyle);
			else if (loyalty().get(f).value > 0)
				document.insertString(document.getLength(), loyalty().get(f) + "\n", textStyle);
			
			document.insertString(document.getLength(), artist().get(f) + " " + number().get(f) + "/" + expansion().count, textStyle);
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
	public default void formatDocument(StyledDocument document)
	{
		Style textStyle = document.getStyle("text");
		try
		{
			for (int f = 0; f < faces(); f++)
			{
				formatDocument(document, f);
				if (f < faces() - 1)
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
	public String toString();
	
	/**
	 * @return This Card's hash code.
	 */
	@Override
	public int hashCode();
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if this Card and the other Object both represent the exact same
	 * card, and <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object other);
}
