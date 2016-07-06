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
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.PowerToughness;
import editor.database.characteristics.Rarity;

/**
 * This class represents a Magic: the Gathering card.  It has all of the card's characteristics and can
 * compare them with other cards.  Each card can be uniquely identified by the set it is in, its name,
 * and its image name (which is its name followed by a number if there is more than one version of the same
 * card in the same set).  All of its values are constant.
 * 
 * TODO: Make sure comments are correct and field/method order makes sense.
 * 
 * @author Alec Roelke
 */
public class SingleCard implements Card
{
	/**
	 * TODO: Comment this
	 */
	private final CardLayout layout;
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
	public final ManaType.Tuple colors;
	/**
	 * This Face's supertypes.
	 */
	public final Set<String> supertypes;
	/**
	 * This Face's types.
	 */
	public final Set<String> types;
	/**
	 * This Face's subtypes.
	 */
	public final Set<String> subtypes;
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
	 * Rulings for this Card.
	 */
	private final Map<Date, List<String>> rulings;
	/**
	 * This Card's color identity, which is a list containing its colors and
	 * colors of any mana symbols that appear in its rules text that is not
	 * reminder text, and in abilities that are given it by basic land types.
	 * TODO: This doesn't work properly with reminder text (like extort).
	 */
	private final ManaType.Tuple colorIdentity;
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
	 * Create a new Card with a single face.
	 * 
	 * @param layout The new Card's layout
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
	public SingleCard(CardLayout layout,
			String name,
			String mana,
			List<ManaType> colors,
			Set<String> supertype,
			Set<String> type,
			Set<String> subtype,
			Rarity rarity,
			Expansion set,
			String text,
			String flavor,
			String artist,
			String number,
			String power,
			String toughness,
			String loyalty,
			TreeMap<Date, List<String>> rulings,
			Map<String, Legality> legality,
			String imageName)
	{
		this.layout = layout;
		this.name = name;
		this.mana = ManaCost.valueOf(mana);
		this.colors = new ManaType.Tuple(colors);
		this.supertypes = supertype;
		this.types = type;
		this.subtypes = subtype;
		this.text = text;
		this.flavor = flavor;
		this.artist = artist;
		this.number = number;
		this.power = new PowerToughness(power);
		this.toughness = new PowerToughness(toughness);
		this.loyalty = new Loyalty(loyalty);
		this.imageName = imageName;
		this.rarity = rarity;
		this.set = set;
		this.rulings = rulings;
		this.legality = Collections.unmodifiableMap(legality);
		
		// Create the type line for this Card
		StringBuilder str = new StringBuilder();
		if (supertypes.size() > 0)
			str.append(String.join(" ", supertypes)).append(" ");
		str.append(String.join(" ", types));
		if (subtypes.size() > 0)
			str.append(" — ").append(String.join(" ", subtypes));
		typeLine = str.toString();
		
		// Create this Card's color identity
		List<ManaType> identity = new ArrayList<ManaType>(colors);
		identity.addAll(this.mana.colors());
		Matcher m = ManaCost.MANA_COST_PATTERN.matcher(text);
		while (m.find())
			for (ManaType col: ManaCost.valueOf(m.group()).colors())
				if (col != ManaType.COLORLESS)
					identity.add(col);
		for (String sub: subtype)
		{
			if (sub.equalsIgnoreCase("plains"))
				identity.add(ManaType.WHITE);
			else if (sub.equalsIgnoreCase("island"))
				identity.add(ManaType.BLUE);
			else if (sub.equalsIgnoreCase("swamp"))
				identity.add(ManaType.BLACK);
			else if (sub.equalsIgnoreCase("mountain"))
				identity.add(ManaType.RED);
			else if (sub.equalsIgnoreCase("forest"))
				identity.add(ManaType.GREEN);
		}
		ManaType.sort(identity);
		colorIdentity = new ManaType.Tuple(identity);
/*
		Matcher m = ManaCost.manaCostPattern.matcher(text);
		while (m.find())
		{
			ManaCost cost = ManaCost.valueOf(m.group());
			text = text.replace(m.group(), cost.toString());
		}
*/
	}
	
	@Override
	public CardLayout layout()
	{
		return layout;
	}
	
	/**
	 * @return The list of names of the faces of this Card.
	 */
	@Override
	public List<String> name()
	{
		return Arrays.asList(name);
	}
	
	/**
	 * @return A String representation of this Card, which is its name.  To get a unique identifier
	 * for the Card, use {@link Card#id()}.
	 */
	@Override
	public String toString()
	{
		return unifiedName();
	}
	
	/**
	 * @return The mana cost of this Card.  This is represented as a tuple, since multi-faced
	 * cards have multiple costs that need to be treated separately.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(mana);
	}
	
	/**
	 * @return A Tuple<Double> containing all of the converted mana costs of the faces
	 * of this Card.
	 */
	@Override
	public List<Double> cmc()
	{
		return manaCost().stream().map(ManaCost::cmc).collect(Collectors.toList());
	}

	/**
	 * @return The colors of this Card, which is the union of the colors of its faces.
	 */
	@Override
	public ManaType.Tuple colors()
	{
		return colors;
	}
	
	@Override
	public List<String> typeLine()
	{
		return Arrays.asList(typeLine);
	}

	/**
	 * @return A list containing all of the supertypes that appear on all of the faces of
	 * this Card.
	 */
	@Override
	public Set<String> supertypes()
	{
		return supertypes;
	}

	/**
	 * @return A list containing all of the card types that appear on all of the faces of
	 * this Card.
	 */
	@Override
	public Set<String> types()
	{
		return types;
	}

	/**
	 * @return a list containing all of the subtypes that appear on all of the faces of
	 * this Card.
	 */
	@Override
	public Set<String> subtypes()
	{
		return subtypes;
	}
	
	/**
	 * @return The Expansion this Card belongs to.
	 */
	@Override
	public Expansion expansion()
	{
		return set;
	}

	/**
	 * @return This Card's Rarity.
	 */
	@Override
	public Rarity rarity()
	{
		return rarity;
	}

	/**
	 * @return The texts of all of the faces of this Card concatenated together.
	 * This should mostly be used for searching, since using it for display could
	 * cause confusion.
	 */
	@Override
	public List<String> oracleText()
	{
		return Arrays.asList(text);
	}

	/**
	 * @return The flavor texts of all of the faces of this Card concatenated together.
	 * This should mostly be used for searching, since using it for display could cause
	 * confusion.
	 */
	@Override
	public List<String> flavorText()
	{
		return Arrays.asList(flavor);
	}
	
	/**
	 * @return The list of artists for the faces of this Card (probably they are all
	 * the same).
	 */
	@Override
	public List<String> artist()
	{
		return Arrays.asList(artist);
	}
	
	/**
	 * @return The collector numbers of all faces of this Card.
	 */
	@Override
	public List<String> number()
	{
		return Arrays.asList(number);
	}
	
	/**
	 * @return A tuple containing the power values of each face of this Card.
	 */
	@Override
	public PowerToughness.Tuple power()
	{
		return new PowerToughness.Tuple(power);
	}

	/**
	 * @return A tuple containing the toughness values of each face of this Card.
	 */
	@Override
	public PowerToughness.Tuple toughness()
	{
		return new PowerToughness.Tuple(toughness);
	}
	
	/**
	 * @return A tuple containing the loyalty values of each face of this Card.
	 */
	@Override
	public Loyalty.Tuple loyalty()
	{
		return new Loyalty.Tuple(loyalty);
	}

	@Override
	public Map<Date, List<String>> rulings()
	{
		return rulings;
	}
	
	/**
	 * @return A map of formats onto this Card's Legalities in them.
	 * TODO: Calculate this based on the new policy in mtgjson.com
	 */
	@Override
	public Map<String, Legality> legality()
	{
		return legality;
	}
	
	/**
	 * @return The image name of each face of this Card.
	 */
	@Override
	public List<String> imageNames()
	{
		return Arrays.asList(imageName);
	}

	/**
	 * @return A list containing all supertypes, card types, and subtypes of all of the
	 * Faces of this Card.
	 */
	@Override
	public List<Set<String>> allTypes()
	{
		List<Set<String>> allTypes = new ArrayList<Set<String>>();
		Set<String> faceTypes = new HashSet<String>();
		faceTypes.addAll(supertypes);
		faceTypes.addAll(types);
		faceTypes.addAll(subtypes);
		allTypes.add(faceTypes);
		return allTypes;
	}

	/**
	 * @return This Card's color identity.
	 */
	@Override
	public ManaType.Tuple colorIdentity()
	{
		return colorIdentity;
	}
	
	/**
	 * @return The number of faces this Card has.
	 */
	@Override
	public int faces()
	{
		return 1;
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
		if (other.getClass() != getClass())
			return false;
		return id().equals(((Card)other).id());
	}
}
