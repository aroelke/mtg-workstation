package editor.database;

import java.awt.datatransfer.DataFlavor;
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

import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.MTGColor;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.PowerToughness;
import editor.database.characteristics.Rarity;
import editor.database.symbol.ChaosSymbol;
import editor.database.symbol.Symbol;

/**
 * This class represents a Magic: the Gathering card.  It has all of the card's characteristics and can
 * compare them with other cards.  Each card can be uniquely identified by the set it is in, its name,
 * and its image name (which is its name followed by a number if there is more than one version of the same
 * card in the same set).  All of its values are constant.
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
	/**
	 * Separator string between characteristics of a multi-face card.
	 */
	public static final String FACE_SEPARATOR = "//";
	/**
	 * DataFlavor representing cards being transferred.
	 */
	public static final DataFlavor cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Card[].class.getName() + "\"", "Card Array");
	
	/**
	 * Array containing the faces of this Card.
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
	 * Unique identifier for this Card, which is its expansion name, name, and 
	 * image name concatenated together.
	 * TODO: Make use of the mtgjson.com id field
	 */
	private final String ID;
	/**
	 * This Card's color identity, which is a list containing its colors and
	 * colors of any mana symbols that appear in its rules text that is not
	 * reminder text, and in abilities that are given it by basic land types.
	 */
	private final MTGColor.Tuple colorIdentity;
	
	/**
	 * Create a new Card with a single face.
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
		
		// Create the UID for this Card
		ID = this.set.code + name() + faces[0].imageName;
		
		// Create this Card's color identity
		List<MTGColor> identity = new ArrayList<MTGColor>(colors);
		identity.addAll(faces[0].mana.colors());
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
	
	/**
	 * Create a new Card from the list of single-faced Cards.  Each Card in the list
	 * represents a face of the new Card, and should be in order that the faces appear
	 * on the actual card (i.e. front/left/unflipped first).
	 * 
	 * @param cards List of faces to create the new card from
	 */
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
		
		// Create the UID for this Card
		ID = set.code + name() + faces[0].imageName;
		
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
	
	/**
	 * @return The unified name of this Card.  If the Card has multiple faces, they will be separated
	 * by a separator.
	 */
	public String name()
	{
		StringJoiner str = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (Face face: faces)
			str.add(face.name);
		return str.toString();
	}
	
	/**
	 * @return A String representation of this Card, which is its name.  To get a unique identifier
	 * for the Card, use {@link Card#id()}.
	 */
	@Override
	public String toString()
	{
		return name();
	}
	
	/**
	 * @return The mana cost of this Card.  This is represented as a tuple, since multi-faced
	 * cards have multiple costs that need to be treated separately.
	 */
	public ManaCost.Tuple mana()
	{
		return new ManaCost.Tuple(Arrays.stream(faces).map((f) -> f.mana).toArray(ManaCost[]::new));
	}
	
	/**
	 * @return This Card's converted mana cost, which is the smallest converted mana cost
	 * among its faces.
	 */
	public double cmc()
	{
		return mana().stream().mapToDouble(ManaCost::cmc).min().orElse(0.0);
	}

	/**
	 * @return The colors of this Card, which is the union of the colors of its faces.
	 */
	public MTGColor.Tuple colors()
	{
		ArrayList<MTGColor> colors = new ArrayList<MTGColor>();
		for (Face face: faces)
			colors.addAll(face.colors);
		return new MTGColor.Tuple(colors);
	}

	/**
	 * @return A list containing all of the supertypes that appear on all of the faces of
	 * this Card.
	 */
	public List<String> supertypes()
	{
		Set<String> supertypes = new HashSet<String>();
		for (Face face: faces)
			supertypes.addAll(face.supertypes);
		return new ArrayList<String>(supertypes);
	}

	/**
	 * @return A list containing all of the card types that appear on all of the faces of
	 * this Card.
	 */
	public List<String> types()
	{
		Set<String> types = new HashSet<String>();
		for (Face face: faces)
			types.addAll(face.types);
		return new ArrayList<String>(types);
	}

	/**
	 * @return a list containing all of the subtypes that appear on all of the faces of
	 * this Card.
	 */
	public List<String> subtypes()
	{
		Set<String> subtypes = new HashSet<String>();
		for (Face face: faces)
			subtypes.addAll(face.subtypes);
		return new ArrayList<String>(subtypes);
	}

	/**
	 * @return The type line of this Card, which is the type lines of its faces separated
	 * by separators.
	 */
	public String typeLine()
	{
		StringJoiner str = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (Face face: faces)
			str.add(face.typeLine);
		return str.toString();
	}
	
	/**
	 * @return The Expansion this Card belongs to.
	 */
	public Expansion expansion()
	{
		return set;
	}

	/**
	 * @return This Card's Rarity.
	 */
	public Rarity rarity()
	{
		return rarity;
	}

	/**
	 * @return The texts of all of the faces of this Card concatenated together.
	 * This should mostly be used for searching, since using it for display could
	 * cause confusion.
	 */
	public String text()
	{
		StringJoiner str = new StringJoiner("\n");
		for (Face face: faces)
			str.add(face.text);
		return str.toString();
	}

	/**
	 * @return The flavor texts of all of the faces of this Card concatenated together.
	 * This should mostly be used for searching, since using it for display could cause
	 * confusion.
	 */
	public String flavor()
	{
		StringJoiner str = new StringJoiner("\n");
		for (Face face: faces)
			str.add(face.flavor);
		return str.toString();
	}

	/**
	 * @return This Card's artist.  Currently it assumes that all faces are by the same
	 * artist.
	 */
	public String artist()
	{
		return faces[0].artist;
	}
	
	/**
	 * @return The list of artists for the faces of this Card (probably they are all
	 * the same).
	 */
	public List<String> artists()
	{
		return Arrays.stream(faces).map((f) -> f.artist).collect(Collectors.toList());
	}
	
	/**
	 * @return The collector numbers of all faces of this Card.
	 */
	public String[] number()
	{
		return Arrays.stream(faces).map((f) -> f.number).toArray(String[]::new);
	}
	
	/**
	 * @return A tuple containing the power values of each face of this Card.
	 */
	public PowerToughness.Tuple power()
	{
		return new PowerToughness.Tuple(Arrays.stream(faces).map((f) -> f.power).toArray(PowerToughness[]::new));
	}
	
	/**
	 * @return <code>true</code> if any of this Card's faces has a power value that can vary,
	 * and <code>false</code> otherwise.
	 */
	public boolean powerVariable()
	{
		return power().stream().anyMatch(PowerToughness::variable);
	}

	/**
	 * @return A tuple containing the toughness values of each face of this Card.
	 */
	public PowerToughness.Tuple toughness()
	{
		return new PowerToughness.Tuple(Arrays.stream(faces).map((f) -> f.toughness).toArray(PowerToughness[]::new));
	}
	
	/**
	 * @return <code>true</code> if any of this Card's faces has a toughness value that can vary,
	 * and <code>false</code> otherwise.
	 */
	public boolean toughnessVariable()
	{
		return toughness().stream().anyMatch(PowerToughness::variable);
	}
	
	/**
	 * @return A tuple containing the loyalty values of each face of this Card.
	 */
	public Loyalty.Tuple loyalty()
	{
		return new Loyalty.Tuple(Arrays.stream(faces).map((f) -> f.loyalty).toArray(Loyalty[]::new));
	}

	/**
	 * @return A map of formats onto this Card's Legalities in them.
	 */
	public Map<String, Legality> legality()
	{
		return legality;
	}
	
	/**
	 * @return The image name of each face of this Card.
	 */
	public String[] imageNames()
	{
		return Arrays.stream(faces).map((f) -> f.imageName).distinct().toArray(String[]::new);
	}

	/**
	 * @return A list containing all supertypes, card types, and subtypes of all of the
	 * Faces of this Card.
	 */
	public List<List<String>> allTypes()
	{
		List<List<String>> allTypes = new ArrayList<List<String>>();
		for (Face f: faces)
		{
			List<String> faceTypes = new ArrayList<String>();
			faceTypes.addAll(f.supertypes);
			faceTypes.addAll(f.types);
			faceTypes.addAll(f.subtypes);
			allTypes.add(faceTypes);
		}
		return allTypes;
	}

	/**
	 * @return This Card's unique identifier, which is its name, expansion name,
	 * and front face's image name concatenated together.
	 */
	public String id()
	{
		return ID;
	}

	/**
	 * @return This Card's color identity.
	 */
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
	 * case.  This will be a list containing the name of each face.
	 * 
	 * @return This Card's normalized name.
	 */
	public List<String> normalizedName()
	{
		List<String> names = new ArrayList<String>();
		for (Face f: faces)
		{
			String normal = new String(f.name.toLowerCase());
			normal = Normalizer.normalize(normal, Normalizer.Form.NFD);
			normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
			names.add(normal);
		}
		return names;
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
	 * @return The normalized, lower case version of this Card's rules text.  For example,
	 * "æ" will be replaced with "ae."  This will be a list containing the rules text of
	 * each face.
	 * @see editor.database.Card#normalizedName()
	 */
	public List<String> normalizedText()
	{
		List<String> texts = new ArrayList<String>();
		for (Face f: faces)
		{
			String normal = new String(f.text.toLowerCase());
			normal = Normalizer.normalize(normal, Normalizer.Form.NFD);
			normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
			texts.add(normal);
		}
		return texts;
	}
	
	/**
	 * @return The normalized, lower case version of this Card's flavor text.  For example,
	 * "æ" will be replaced with "ae."  This will be a list containing the flavor text of each
	 * face.
	 * @see editor.database.Card#normalizedName()
	 */
	public List<String> normalizedFlavor()
	{
		List<String> flavors = new ArrayList<String>();
		for (Face f: faces)
		{
			String normal = new String(f.flavor.toLowerCase());
			normal = Normalizer.normalize(normal, Normalizer.Form.NFD);
			normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
			flavors.add(normal);
		}
		return flavors;
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
	 * @return A String containing most of the information contained in this Card,
	 * formatted to slightly mimic a real Magic: the Gathering card and with symbols
	 * replaced by HTML for display in HTML-enabled panels.
	 */
	public String toHTMLString()
	{
		StringJoiner join = new StringJoiner("<br>-----<br>");
		for (Face f: faces)
		{
			StringBuilder str = new StringBuilder();
			str.append(f.name + (f.mana.isEmpty() ? "" : " " + f.mana.toHTMLString()));
			if (f.mana.cmc() == (int)f.mana.cmc())
				str.append(" (" + (int)f.mana.cmc() + ")<br>");
			else
				str.append(" (" + f.mana.cmc() + ")<br>");
			str.append(f.typeLine + "<br>");
			str.append(set.name + " " + rarity + "<br>");
			
			if (!f.text.isEmpty())
				str.append(f.HTMLText() + "<br>");
			if (!f.flavor.isEmpty())
				str.append(f.HTMLFlavor() + "<br>");
			
			if (!Double.isNaN(f.power.value) && !Double.isNaN(f.toughness.value))
				str.append(f.power + "/" + f.toughness + "<br>");
			else if (f.loyalty.value > 0)
				str.append(f.loyalty + "<br>");
			
			str.append(f.artist + " " + f.number + "/" + set.count);
			
			join.add(str.toString());
		}
		return join.toString();
	}
	
	/**
	 * @return A String containing most of the information contained in the Face at
	 * the given index, formatted to slightly mimic a real Magic: the Gathering card
	 * and with symbols replaced by HTML for display in HTML-enabled panels.
	 */
	public String faceHTMLString(int f)
	{
		return faces[f].toHTMLString();
	}
	
	/**
	 * @return The number of faces this Card has.
	 */
	public int faces()
	{
		return faces.length;
	}
	
	/**
	 * @return A number that uniquely identifies this Card.
	 */
	@Override
	public int hashCode()
	{
		return ID.hashCode();
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
		return ID.equals(o.ID);
	}
	
	/**
	 * This class represents a face of a multi-faced card.  A "face" is one of the sides
	 * of the card if it is double-faced, one of the halves if it is split, and the contents
	 * of one of the sides if it is a flip card, or the only face if it isn't any of those.
	 * 
	 * @author Alec Roelke
	 */
	private class Face
	{
		/**
		 * Name of this Face.
		 */
		public final String name;
		/**
		 * Mana cost of this Face.  Split cards will have independent mana costs for each
		 * face, double-faced cards typically will have no mana cost for the back face, and
		 * flip cards have the same mana cost for both faces.
		 */
		public final ManaCost mana;
		/**
		 * This Face's colors.
		 */
		public final MTGColor.Tuple colors;
		/**
		 * This Face's supertypes.
		 */
		public final List<String> supertypes;
		/**
		 * This Face's types.
		 */
		public final List<String> types;
		/**
		 * This Face's subtypes.
		 */
		public final List<String> subtypes;
		/**
		 * This Face's rules text.
		 */
		public final String text;
		/**
		 * This Face's flavor text.
		 */
		public final String flavor;
		/**
		 * This Face's artist.
		 */
		public final String artist;
		/**
		 * This Face's collector's number.
		 */
		public final String number;
		/**
		 * This Face's power, if it is a creature (it's empty otherwise).
		 */
		public final PowerToughness power;
		/**
		 * This Face's toughness, if it is a creature (it's empty otherwise).
		 */
		public final PowerToughness toughness;
		/**
		 * This Face's loyalty, if it is a planeswalker (it's 0 otherwise).
		 */
		public final Loyalty loyalty;
		
		/**
		 * This Face's image name.  If the card is a flip or split card, all Faces
		 * of that card will have the same image name.
		 */
		public final String imageName;
		/**
		 * This Face's type line, which is "[Supertype(s) Type(s) - Subtype(s)]
		 */
		public final String typeLine;
		
		/**
		 * Create a new Face.
		 * 
		 * @param name Name of the new Face
		 * @param mana Mana cost of the new Face
		 * @param colors Colors of the new Face
		 * @param supertypes Supertypes of the new Face (can be empty)
		 * @param types Card types of the new Face (should not be empty)
		 * @param subtypes Subtypes of the new Face (can be empty)
		 * @param text Rules text of the new Face
		 * @param flavor Flavor text of the new Face
		 * @param artist Artist of the new Face
		 * @param number Collector number of the new Face
		 * @param power Power of the new Face
		 * @param toughness Toughness of the new Face
		 * @param loyalty Loyalty of the new Face
		 * @param imageName Image name of the new Face
		 */
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
		
		/**
		 * @return This Face's rules text, with the symbols replaced with HTML image tags for
		 * display in HTML-enabled panels.
		 */
		public String HTMLText()
		{
			String html = new String(text);
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
			html = html.replace("CHAOS", ChaosSymbol.CHAOS.getHTML());
			html = html.replace("\n", "<br>");
			return html;
		}
		
		/**
		 * @return This Face's flavor text, with the symbols replaced with HTML image tags for
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
		 * @return A String containing most of the information contained in this Face,
		 * formatted to slightly mimic a real Magic: the Gathering card and with symbols
		 * replaced by HTML for display in HTML-enabled panels.
		 */
		public String toHTMLString()
		{
			StringBuilder str = new StringBuilder();
			str.append(name + (mana.isEmpty() ? "" : " " + mana.toHTMLString()));
			if (mana.cmc() == (int)mana.cmc())
				str.append(" (" + (int)mana.cmc() + ")<br>");
			else
				str.append(" (" + mana.cmc() + ")<br>");
			str.append(typeLine + "<br>");
			str.append(set.name + " " + rarity + "<br>");
			
			if (!text.isEmpty())
				str.append(HTMLText() + "<br>");
			if (!flavor.isEmpty())
				str.append(HTMLFlavor() + "<br>");
			
			if (!Double.isNaN(power.value) && !Double.isNaN(toughness.value))
				str.append(power + "/" + toughness + "<br>");
			else if (loyalty.value > 0)
				str.append(loyalty + "<br>");
			
			str.append(artist + " " + number + "/" + set.count);
			
			return str.toString();
		}
	}
}
