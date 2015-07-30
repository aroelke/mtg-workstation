package database;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import database.characteristics.Expansion;
import database.characteristics.Legality;
import database.characteristics.Loyalty;
import database.characteristics.MTGColor;
import database.characteristics.ManaCost;
import database.characteristics.PowerToughness;
import database.characteristics.Rarity;
import database.symbol.Symbol;

/**
 * This class represents a Magic: the Gathering card.  It has all of the card's characteristics and can
 * compare them with other cards.  Each card can be uniquely identified by the set it is in, its name,
 * and its image name (which is its name followed by a number if there is more than one version of the same
 * card in the same set).  All of its values are constant.
 * 
 * TODO: Handle cards with multiple faces (split cards, flip cards, double-faced cards)
 * 
 * @author Alec Roelke
 */
public final class Card
{
	/**
	 * List of all supertypes that appear on cards.
	 */
	public static String[] supertypeList = {};
	/**
	 * List of all types that appear on cards (including ones that appear on Unglued and Unhinged cards, whose
	 * type lines were not updated for the most modern templating).
	 */
	public static String[] typeList = {};
	/**
	 * List of all subtypes that appear on cards.
	 */
	public static String[] subtypeList = {};
	/**
	 * List of all formats cards can be played in.
	 */
	public static String[] formatList = {};
	public static final String FACE_SEPARATOR = "//";
	
	/**
	 * TODO: Comment this (and all the getters and Face)
	 */
	private final Face[] faces;
	/**
	 * The Expansion this Card belongs to.
	 */
	private final Expansion set;
	/**
	 * This Card's rarity.
	 */
	private final Rarity rarity;
	/**
	 * Formats and legality for this Card.
	 */
	private final Map<String, Legality> legality;
	
	/**
	 * List containing all types on this Card.
	 */
	private final List<String> allTypes;
	/**
	 * Unique identifier for this Card, which is its expansion name, name, and 
	 * image name concatenated together.
	 */
	private final String ID;
	/**
	 * This Card's color identity, which is a list containing its colors and
	 * colors of any mana symbols that appear in its rules text that is not
	 * reminder text, and in abilities that are given it by basic land types.
	 */
	private final MTGColor.Tuple colorIdentity;
	
	/**
	 * Create a new Card.
	 * 
	 * @param name The new Card's name
	 * @param mana The new Card's mana cost
	 * @param colors The new Card's colors
	 * @param supertype The new Card's supertypes
	 * @param type The new Card's types
	 * @param subtype The new Card's subtypes
	 * @param rarity The new Card's rarity
	 * @param set The Expansion the new Card belongs to
	 * @param text The new Card's rules text
	 * @param flavor The new Card's flavor text
	 * @param artist The new Card's artist
	 * @param number The new Card's collector's number
	 * @param power The new Card's power
	 * @param toughness The new Card's toughness
	 * @param loyalty The new Card's loyalty
	 * @param layout The new Card's layout
	 * @param legality The new Card's legality
	 * @param imageName The new Card's image name
	 */
	public Card(String name,
				String mana,
				List<MTGColor> colors,
				List<String> supertype,
				List<String> type,
				List<String> subtype,
				Rarity rarity,
				Expansion set,
				String text,
				String flavor,
				String artist,
				String number,
				String power,
				String toughness,
				String loyalty,
				Map<String, Legality> legality,
				String imageName)
	{
		faces = new Face[] {new Face(name,
									 new ManaCost(mana),
									 new MTGColor.Tuple(colors),
									 supertype,
									 type,
									 subtype,
									 text,
									 flavor,
									 artist,
									 number,
									 new PowerToughness(power),
									 new PowerToughness(toughness),
									 new Loyalty(loyalty),
									 imageName)};
		this.rarity = rarity;
		this.set = set;
		this.legality = Collections.unmodifiableMap(legality);
		
		// Populate the list of all types
		allTypes = new ArrayList<String>();
		allTypes.addAll(supertypes());
		allTypes.addAll(types());
		allTypes.addAll(subtypes());
		
		// Create the UID for this Card
		ID = this.set.code + name() + imageName();
		
		// Create this Card's color identity
		List<MTGColor> identity = new ArrayList<MTGColor>(colors);
		Matcher m = ManaCost.MANA_COST_PATTERN.matcher(text);
		while (m.find())
			for (MTGColor col: ManaCost.valueOf(m.group()).colors())
				identity.add(col);
		for (String sub: subtype)
		{
			if (sub.equalsIgnoreCase("plains"))
				identity.add(MTGColor.WHITE);
			else if (sub.equalsIgnoreCase("island"))
				identity.add(MTGColor.BLUE);
			else if (sub.equalsIgnoreCase("swamp"))
				identity.add(MTGColor.BLACK);
			else if (sub.equalsIgnoreCase("mountain"))
				identity.add(MTGColor.RED);
			else if (sub.equalsIgnoreCase("forest"))
				identity.add(MTGColor.GREEN);
		}
		MTGColor.sort(identity);
		colorIdentity = new MTGColor.Tuple(identity);
/*
		Matcher m = ManaCost.manaCostPattern.matcher(text);
		while (m.find())
		{
			ManaCost cost = ManaCost.valueOf(m.group());
			text = text.replace(m.group(), cost.toString());
		}
*/
	}
	
	public Card(List<Card> cards)
	{
		if (cards.stream().map((c) -> c.rarity).distinct().count() > 1)
			throw new IllegalArgumentException("All faces must have the same rarity");
		if (cards.stream().map((c) -> c.set).distinct().count() > 1)
			throw new IllegalArgumentException("All faces must belong to the same expansion");
		if (cards.stream().map((c) -> c.legality).distinct().count() > 1)
			throw new IllegalArgumentException("All faces must have the same format legalities");
		if (cards.stream().map((c) -> c.faces.length).anyMatch((n) -> n != 1))
			throw new IllegalArgumentException("Only individual card faces can be joined");
		
		// Get the card's faces
		faces = cards.stream().map((c) -> c.faces[0]).toArray(Face[]::new);
		
		// Get the values that are common to all of the faces
		rarity = cards.get(0).rarity;
		set = cards.get(0).set;
		legality = cards.get(0).legality;
		
		// Populate the list of all types
		allTypes = new ArrayList<String>();
		allTypes.addAll(supertypes());
		allTypes.addAll(types());
		allTypes.addAll(subtypes());
		
		// Create the UID for this Card
		ID = set.code + name() + imageName();
		
		// Create this Card's color identity
		List<MTGColor> identity = new ArrayList<MTGColor>();
		for (Face f: faces)
		{
			identity.addAll(f.colors);
			Matcher m = ManaCost.MANA_COST_PATTERN.matcher(f.text);
			while (m.find())
				for (MTGColor col: ManaCost.valueOf(m.group()).colors())
					identity.add(col);
			for (String sub: f.subtypes)
			{
				if (sub.equalsIgnoreCase("plains"))
					identity.add(MTGColor.WHITE);
				else if (sub.equalsIgnoreCase("island"))
					identity.add(MTGColor.BLUE);
				else if (sub.equalsIgnoreCase("swamp"))
					identity.add(MTGColor.BLACK);
				else if (sub.equalsIgnoreCase("mountain"))
					identity.add(MTGColor.RED);
				else if (sub.equalsIgnoreCase("forest"))
					identity.add(MTGColor.GREEN);
			}
		}
		MTGColor.sort(identity);
		colorIdentity = new MTGColor.Tuple(identity);
	}
	
	// TODO: Rather than perform these operations every time they are called, precalculate them
	
	public String name()
	{
		StringJoiner str = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (Face face: faces)
			str.add(face.name);
		return str.toString();
	}

	public String[] names()
	{
		return Arrays.stream(faces).map((f) -> f.name).toArray(String[]::new);
	}
	
	public ManaCost[] mana()
	{
		return Arrays.stream(faces).map((f) -> f.mana).toArray(ManaCost[]::new);
	}

	public MTGColor.Tuple colors()
	{
		ArrayList<MTGColor> colors = new ArrayList<MTGColor>();
		for (Face face: faces)
			colors.addAll(face.colors);
		return new MTGColor.Tuple(colors);
	}

	public List<String> supertypes()
	{
		Set<String> supertypes = new HashSet<String>();
		for (Face face: faces)
			supertypes.addAll(face.supertypes);
		return new ArrayList<String>(supertypes);
	}

	public List<String> types()
	{
		Set<String> types = new HashSet<String>();
		for (Face face: faces)
			types.addAll(face.types);
		return new ArrayList<String>(types);
	}

	public List<String> subtypes()
	{
		Set<String> subtypes = new HashSet<String>();
		for (Face face: faces)
			subtypes.addAll(face.subtypes);
		return new ArrayList<String>(subtypes);
	}

	public String typeLine()
	{
		StringJoiner str = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (Face face: faces)
			str.add(face.typeLine);
		return str.toString();
	}
	
	public String[] typeLines()
	{
		return Arrays.stream(faces).map((f) -> f.typeLine).toArray(String[]::new);
	}
	
	public Expansion expansion()
	{
		return set;
	}

	public Rarity rarity()
	{
		return rarity;
	}

	public String text()
	{
		StringJoiner str = new StringJoiner("\n----------\n");
		for (Face face: faces)
			str.add(face.text);
		return str.toString();
	}
	
	public String[] texts()
	{
		return Arrays.stream(faces).map((f) -> f.text).toArray(String[]::new);
	}

	public String flavor()
	{
		StringJoiner str = new StringJoiner("\n----------\n");
		for (Face face: faces)
			str.add(face.flavor);
		return str.toString();
	}
	
	public String[] flavors()
	{
		return Arrays.stream(faces).map((f) -> f.flavor).toArray(String[]::new);
	}

	public String artist()
	{
		StringJoiner str = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (Face face: faces)
			str.add(face.artist);
		return str.toString();
	}
	
	public String[] artists()
	{
		return Arrays.stream(faces).map((f) -> f.artist).toArray(String[]::new);
	}

	public String number()
	{
		StringJoiner str = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (Face face: faces)
			str.add(face.number);
		return str.toString();
	}
	
	public String[] numbers()
	{
		return Arrays.stream(faces).map((f) -> f.number).toArray(String[]::new);
	}

	public PowerToughness power()
	{
		return faces[0].power;
	}
	
	public PowerToughness[] powers()
	{
		return Arrays.stream(faces).map((f) -> f.power).toArray(PowerToughness[]::new);
	}
	
	public boolean powerVariable()
	{
		return Arrays.stream(powers()).anyMatch(PowerToughness::variable);
	}

	public PowerToughness toughness()
	{
		return faces[0].toughness;
	}

	public PowerToughness[] toughnesses()
	{
		return Arrays.stream(faces).map((f) -> f.toughness).toArray(PowerToughness[]::new);
	}
	
	public boolean toughnessVariable()
	{
		return Arrays.stream(toughnesses()).anyMatch(PowerToughness::variable);
	}
	
	public Loyalty loyalty()
	{
		return faces[0].loyalty;
	}
	
	public Loyalty[] loyalties()
	{
		return Arrays.stream(faces).map((f) -> f.loyalty).toArray(Loyalty[]::new);
	}

	public Map<String, Legality> legality()
	{
		return legality;
	}

	public String imageName()
	{
		return faces[0].imageName;
	}
	
	public String[] imageNames()
	{
		return Arrays.stream(faces).map((f) -> f.imageName).toArray(String[]::new);
	}

	public List<String> allTypes()
	{
		return allTypes;
	}

	public String id()
	{
		return ID;
	}

	public MTGColor.Tuple colorIdentity()
	{
		return colorIdentity;
	}

	/**
	 * Compare this Card's name to another lexicographically.
	 * 
	 * @param other Card to compare with
	 * @return The lexicographical difference between this Card's name and the
	 * other Card's name.
	 */
	public int compareName(Card other)
	{
		return Collator.getInstance(Locale.US).compare(name(), other.name());
	}
	
	/**
	 * Normalize this Card's name so it can be searched using the keyboard ignoring
	 * case.  For example, the ligature "æ" will be replaced with "ae" regardless of
	 * case.
	 * 
	 * @return This Card's normalized name.
	 */
	public String normalizedName()
	{
		String normal = new String(name().toLowerCase());
		normal = Normalizer.normalize(normal, Normalizer.Form.NFD);
		normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
		return normal;
	}
	
	/**
	 * @param other Card to compare with
	 * @return The difference between this Card's mana cost and the other one's
	 * mana cost.  If the card has multiple faces, the front/left/unflipped one
	 * is used.
	 */
	public int compareManaCost(Card other)
	{
		return mana()[0].compareTo(other.mana()[0]);
	}
	
	/**
	 * @param other Card to compare with
	 * @return The difference between this Card's colors and the other one's
	 * colors.
	 */
	public int compareColors(Card other)
	{
		if (colors().size() != other.colors().size())
			return colors().size() - other.colors().size();
		else
		{
			int diff = 0;
			for (int i = 0; i < colors().size(); i++)
				diff += colors().get(i).compareTo(other.colors().get(i))*Math.pow(10, (4 - i));
			return diff;
		}
	}
	
	/**
	 * @param other Card to compare with
	 * @return The lexicographical distance between this Card's type line and the other
	 * one's type line.
	 */
	public int compareTypeLine(Card other)
	{
		return Collator.getInstance(Locale.US).compare(typeLine(), other.typeLine());
	}
	
	/**
	 * @param s String to search for.  This String should not contain white space.
	 * @return <code>true</code> if this Card's supertype list contains the String, and
	 * <code>false</code> otherwise.
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
	 * @param s String to search for.  This String should not contain white space.
	 * @return <code>true</code> if this Card's type list contains the String, and
	 * <code>false</code> otherwise.
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
	 * @param s String to search for.  This String should not contain white space.
	 * @return <code>true</code> if this Card's subtype list contains the String, and
	 * <code>false</code> otherwise.
	 */
	public boolean subtypeContains(String s)
	{
		if (Pattern.compile("\\s").matcher(s).find())
			throw new IllegalArgumentException("Subtypes don't contain white space");
		for (String subtype: subtypes())
			if (s.equalsIgnoreCase(subtype))
				return true;
		return false;
	}
	
	/**
	 * @param other Card to compare with
	 * @return A negative number if this Card is more common than the other, 0 if they have
	 * the same rarity, and a positive number otherwise.  Special is considered more rare
	 * than mythic rare, and basic land is considered more common than common.
	 */
	public int compareRarity(Card other)
	{
		return rarity().compareTo(other.rarity());
	}
	
	/**
	 * @param other Card to compare with
	 * @return The lexicographical difference between this Card's expansion and the other one, or if
	 * they have the same expansion, the difference in collector number.
	 */
	public int compareExpansion(Card other)
	{
		Collator collator = Collator.getInstance(Locale.US);
		return collator.compare(expansion().name, other.expansion().name)*1000 + collator.compare(number(), other.number());
	}
	
	/**
	 * @return The normalized, lower case version of this Card's rules text.  For example,
	 * "æ" will be replaced with "ae."
	 * @see database.Card#normalizedName()
	 */
	public String normalizedText()
	{
		String normal = new String(text().toLowerCase());
		normal = Normalizer.normalize(normal, Normalizer.Form.NFD);
		normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
		return normal;
	}
	
	/**
	 * @return The normalized, lower case version of this Card's flavor text.  For example,
	 * "æ" will be replaced with "ae."
	 * @see database.Card#normalizedName()
	 */
	public String normalizedFlavor()
	{
		String normal = new String(flavor().toLowerCase());
		normal = Normalizer.normalize(normal, Normalizer.Form.NFD);
		normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
		return normal;
	}
	
	/**
	 * @param other Card to compare with
	 * @return The lexicographical difference between this Card's artist and the other one's artist
	 */
	public int compareArtist(Card other)
	{
		return Collator.getInstance(Locale.US).compare(artist(), other.artist());
	}
	
	/**
	 * @param other Card to compare with
	 * @return The difference between this Card's power and the other one's power, multiplied by 2
	 * to account for fractional powers.
	 */
	public int comparePower(Card other)
	{
		return power().compareTo(other.power());
	}
	
	/**
	 * @param other Card to compare with
	 * @return The difference between this Card's toughness and the other one's toughness, multiplied
	 * by 2 to account for fractional powers.
	 */
	public int compareToughness(Card other)
	{
		return toughness().compareTo(other.toughness());
	}
	
	/**
	 * @param other Card to compare with
	 * @return The difference between this Card's loyalty and the other one's loyalty
	 */
	public int compareLoyalty(Card other)
	{
		return loyalty().compareTo(other.loyalty());
	}
	
	/**
	 * @param other Card to compare with
	 * @return The difference between this Card's collector's number and the other one's
	 * collector's number
	 */
	public int compareNumber(Card other)
	{
		return Collator.getInstance(Locale.US).compare(number(), other.number());
	}
	
	/**
	 * @return <code>true</code> if this Card can be the commander of a Commander deck and
	 * <code>false</code> otherwise.
	 */
	public boolean canBeCommander()
	{
		return supertypeContains("legendary") || text().toLowerCase().contains("can be your commander");
	}
	
	/**
	 * @return <code>true</code> if a deck can have any number of copies of this Card and
	 * <code>false</code> otherwise.
	 */
	public boolean ignoreCountRestriction()
	{
		return supertypeContains("basic") || text().toLowerCase().contains("a deck can have any number");
	}
	
	/**
	 * @param format Name of the format to check
	 * @return <code>true</code> if this Card is legal (restricted or unrestricted)
	 * in the specified format, and <code>false</code> otherwise.  If the format's
	 * legality isn't specified by the inventory file, then the card is assumed to be
	 * illegal in it.
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
	 * @return The list of names of Formats in which this Card is legal.
	 */
	public List<String> legalIn()
	{
		return legality().keySet().stream().filter(this::legalIn).collect(Collectors.toList());
	}
	
	/**
	 * @param format Name of the format to check
	 * @return The Legality of this Card in the given format, or BANNED if
	 * the format can't be found and isn't block, freeform, or classic.
	 */
	public Legality legalityIn(String format)
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
	 * @return This Card's rules text, with the symbols replaced with HTML image tags for
	 * display in HTML-enabled panels.
	 */
	public String HTMLText()
	{
		String html = new String(text());
		Matcher reminder = Pattern.compile("(\\([^)]+\\))").matcher(html);
		while (reminder.find())
			html = html.replace(reminder.group(), "<i>" + reminder.group() + "</i>");
		Matcher symbols = Pattern.compile("\\{([^}]+)\\}").matcher(html);
		while (symbols.find())
		{
			try
			{
				Symbol symbol = Symbol.valueOf(symbols.group(1));
				html = html.replace(symbols.group(), symbol.getHTML());
			}
			catch (Exception e)
			{}
		}
		html = html.replace("\n", "<br>");
		return html;
	}
	
	/**
	 * @return This Card's flavor text, with the symbols replaced with HTML image tags for
	 * display in HTML-enabled panels.
	 */
	public String HTMLFlavor()
	{
		String html = new String(flavor());
		Matcher symbols = Pattern.compile("\\{([^}]+)\\}").matcher(html);
		while (symbols.find())
		{
			Symbol symbol = Symbol.valueOf(symbols.group(1));
			html = html.replace(symbols.group(), symbol.getHTML());
		}
		html = html.replace("\n", "<br>");
		return "<i>" + html + "</i>";
	}
	
	/**
	 * @return A String containing most of the information contained in this Card,
	 * formatted to slightly mimic a real Magic: the Gathering card and with symbols
	 * replaced by HTML for display in HTML-enabled panels.
	 * 
	 * TODO: Make this handle multiple faces properly
	 */
	public String toHTMLString()
	{
		StringBuilder str = new StringBuilder();
		str.append(name() + " " + mana()[0].toHTMLString());
		if (mana()[0].cmc() == (int)mana()[0].cmc())
			str.append(" (" + (int)mana()[0].cmc() + ")<br>");
		else
			str.append(" (" + mana()[0].cmc() + ")<br>");
		
		str.append(typeLine()).append("<br>");
		
		str.append(expansion().name + " " + rarity() + "<br>");
		
		if (!text().equals(""))
			str.append(HTMLText() + "<br>");
		if (!flavor().equals(""))
			str.append(HTMLFlavor() + "<br>");
		
		if (typeContains("Creature") || typeContains("Summon") && !typeContains("Enchant"))
			str.append(power() + "/" + toughness() + "<br>");
		else if (typeContains("Planeswalker"))
			str.append(loyalty() + "<br>");
		
		str.append(artist() + " " + number() + "/" + expansion().count);
		
		return str.toString();
	}
	
	/**
	 * @return A number that uniquely identifies this Card.
	 */
	@Override
	public int hashCode()
	{
		return id().hashCode();
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if this Card's UID is the same as the other one's, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
			return true;
		if (other == null)
			return true;
		if (!(other instanceof Card))
			return false;
		Card o = (Card)other;
		return id().equals(o.id());
	}
	
	private class Face
	{
		/**
		 * Name of this Card
		 */
		public final String name;
		/**
		 * Mana cost of this Card
		 */
		public final ManaCost mana;
		/**
		 * This Card's colors.
		 */
		public final MTGColor.Tuple colors;
		/**
		 * This Card's supertypes.
		 */
		public final List<String> supertypes;
		/**
		 * This Card's types.
		 */
		public final List<String> types;
		/**
		 * This Card's subtypes.
		 */
		public final List<String> subtypes;
		/**
		 * This Card's rules text.
		 */
		public final String text;
		/**
		 * This Cards flavor text.
		 */
		public final String flavor;
		/**
		 * This Card's artist.
		 */
		public final String artist;
		/**
		 * This Card's collector's number.
		 */
		public final String number;
		/**
		 * This Card's power, if it is a creature (it's empty otherwise).
		 */
		public final PowerToughness power;
		/**
		 * This Card's toughness, if it is a creature (it's empty otherwise).
		 */
		public final PowerToughness toughness;
		/**
		 * This Card's loyalty, if it is a planeswalker (it's empty otherwise).
		 */
		public final Loyalty loyalty;
		
		/**
		 * This Card's image name.
		 */
		public final String imageName;
		/**
		 * This Card's type line, which is "[Supertype(s) Type(s) - Subtype(s)]
		 */
		public final String typeLine;
		
		public Face(String name,
					ManaCost mana,
					MTGColor.Tuple colors,
					List<String> supertypes,
					List<String> types,
					List<String> subtypes,
					String text,
					String flavor,
					String artist,
					String number,
					PowerToughness power,
					PowerToughness toughness,
					Loyalty loyalty,
					String imageName)
		{
			this.name = name;
			this.mana = mana;
			this.colors = colors;
			this.supertypes = supertypes;
			this.types = types;
			this.subtypes = subtypes;
			this.text = text;
			this.flavor = flavor;
			this.artist = artist;
			this.number = number;
			this.power = power;
			this.toughness = toughness;
			this.loyalty = loyalty;
			this.imageName = imageName;
			
			// Create the type line for this Card
			StringBuilder str = new StringBuilder();
			if (supertypes.size() > 0)
				str.append(String.join(" ", supertypes)).append(" ");
			str.append(String.join(" ", types));
			if (subtypes.size() > 0)
				str.append(" — ").append(String.join(" ", subtypes));
			typeLine = str.toString();
		}
	}
}
