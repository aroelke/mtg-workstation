package editor.database.card;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

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
 * @author Alec Roelke
 */
public class SingleCard extends Card
{
	/**
	 * Name of this SingleCard.
	 */
	public final String name;
	/**
	 * Mana cost of this SingleCard.  Split cards will have independent mana costs for each
	 * face, double-faced cards typically will have no mana cost for the back face, and
	 * flip cards have the same mana cost for both faces.
	 */
	public final ManaCost mana;
	/**
	 * This SingleCard's colors.
	 */
	public final ManaType.Tuple colors;
	/**
	 * This SingleCard's supertypes.
	 */
	public final Set<String> supertypes;
	/**
	 * This SingleCard's types.
	 */
	public final Set<String> types;
	/**
	 * This SingleCard's subtypes.
	 */
	public final Set<String> subtypes;
	/**
	 * TODO: Comment this
	 */
	public final Set<String> allTypes;
	/**
	 * This SingleCard's rules text.
	 */
	public final String text;
	/**
	 * This SingleCard's flavor text.
	 */
	public final String flavor;
	/**
	 * This SingleCard's artist.
	 */
	public final String artist;
	/**
	 * This SingleCard's collector's number.
	 */
	public final String number;
	/**
	 * This SingleCard's power, if it is a creature (it's empty otherwise).
	 */
	public final PowerToughness power;
	/**
	 * This SingleCard's toughness, if it is a creature (it's empty otherwise).
	 */
	public final PowerToughness toughness;
	/**
	 * This SingleCard's loyalty, if it is a planeswalker (it's 0 otherwise).
	 */
	public final Loyalty loyalty;
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
	 */
	private final ManaType.Tuple colorIdentity;
	/**
	 * This SingleCard's image name.  If the card is a flip or split card, all SingleCards
	 * of that card will have the same image name.
	 */
	public final String imageName;
	/**
	 * This SingleCard's type line, which is "[Supertype(s) Type(s) - Subtype(s)]
	 */
	public final String typeLine;
	
	/**
	 * Create a new Card with a single face.
	 * 
	 * @param layout The new Card's layout
	 * @param name The new Card's name
	 * @param mana The new Card's mana cost
	 * @param colors The new Card's colors
	 * @param colorIdentity the new Card's color identity
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
			List<ManaType> colorIdentity,
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
		super(set, layout, 1);
		
		this.name = name;
		this.mana = ManaCost.valueOf(mana);
		this.colors = new ManaType.Tuple(colors);
		this.supertypes = Collections.unmodifiableSet(supertype);
		this.types = Collections.unmodifiableSet(type);
		this.subtypes = Collections.unmodifiableSet(subtype);
		this.text = text;
		this.flavor = flavor;
		this.artist = artist;
		this.number = number;
		this.power = new PowerToughness(power);
		this.toughness = new PowerToughness(toughness);
		this.loyalty = new Loyalty(loyalty);
		this.imageName = imageName;
		this.rarity = rarity;
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
		
		if (colorIdentity.isEmpty())
		{
			// Try to infer the color identity if it's missing
			colorIdentity.addAll(this.mana.colors());
			Matcher m = ManaCost.MANA_COST_PATTERN.matcher(text.replaceAll("\\(.*\\)", ""));
			while (m.find())
				for (ManaType col: ManaCost.valueOf(m.group()).colors())
					if (col != ManaType.COLORLESS)
						colorIdentity.add(col);
			for (String sub: subtype)
			{
				if (sub.equalsIgnoreCase("plains"))
					colorIdentity.add(ManaType.WHITE);
				else if (sub.equalsIgnoreCase("island"))
					colorIdentity.add(ManaType.BLUE);
				else if (sub.equalsIgnoreCase("swamp"))
					colorIdentity.add(ManaType.BLACK);
				else if (sub.equalsIgnoreCase("mountain"))
					colorIdentity.add(ManaType.RED);
				else if (sub.equalsIgnoreCase("forest"))
					colorIdentity.add(ManaType.GREEN);
			}
		}
		this.colorIdentity = new ManaType.Tuple(colorIdentity);
		
		Set<String> faceTypes = new HashSet<String>();
		faceTypes.addAll(supertypes);
		faceTypes.addAll(types);
		faceTypes.addAll(subtypes);
		allTypes = Collections.unmodifiableSet(faceTypes);
	}
	
	/**
	 * @return The list of names of the faces of this SingleCard.
	 */
	@Override
	public List<String> name()
	{
		return Arrays.asList(name);
	}
	
	/**
	 * @return The mana cost of this SingleCard.  This is represented as a tuple, since multi-faced
	 * cards have multiple costs that need to be treated separately.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(mana);
	}
	
	/**
	 * @return A Tuple<Double> containing the converted mana cost of this SingleCard.
	 */
	@Override
	public List<Double> cmc()
	{
		return Arrays.asList(mana.cmc());
	}

	/**
	 * @return The colors of this SingleCard.
	 */
	@Override
	public ManaType.Tuple colors()
	{
		return colors;
	}
	
	/**
	 * @param face Unused
	 * @return The colors of this SingleCard.
	 */
	@Override
	public ManaType.Tuple colors(int face)
	{
		return colors;
	}
	
	/**
	 * @return A String containing all the types of this SingleCard as they would appear on it.
	 */
	@Override
	public List<String> typeLine()
	{
		return Arrays.asList(typeLine);
	}

	/**
	 * @return A list containing all of the supertypes of this SingleCard.
	 */
	@Override
	public Set<String> supertypes()
	{
		return supertypes;
	}

	/**
	 * @return A list containing all of the card types on this SingleCard.
	 */
	@Override
	public Set<String> types()
	{
		return types;
	}

	/**
	 * @return a list containing all of the subtypes on this SingleCard.
	 */
	@Override
	public Set<String> subtypes()
	{
		return subtypes;
	}

	/**
	 * @return This SingleCard's Rarity.
	 */
	@Override
	public Rarity rarity()
	{
		return rarity;
	}

	/**
	 * @return This SingleCard's Oracle text.
	 */
	@Override
	public List<String> oracleText()
	{
		return Arrays.asList(text);
	}

	/**
	 * @return This SingleCard's flavor text.
	 */
	@Override
	public List<String> flavorText()
	{
		return Arrays.asList(flavor);
	}
	
	/**
	 * @return This SingleCard's artist.
	 */
	@Override
	public List<String> artist()
	{
		return Arrays.asList(artist);
	}
	
	/**
	 * @return The collector number of this Card.
	 */
	@Override
	public List<String> number()
	{
		return Arrays.asList(number);
	}
	
	/**
	 * @return A tuple containing the power value of this Card.
	 */
	@Override
	public PowerToughness.Tuple power()
	{
		return new PowerToughness.Tuple(power);
	}

	/**
	 * @return A tuple containing the toughness value of this Card.
	 */
	@Override
	public PowerToughness.Tuple toughness()
	{
		return new PowerToughness.Tuple(toughness);
	}
	
	/**
	 * @return A tuple containing the loyalty value of this Card.
	 */
	@Override
	public Loyalty.Tuple loyalty()
	{
		return new Loyalty.Tuple(loyalty);
	}

	/**
	 * @return A map containing dates and rulings that occurred on those dates
	 * for this Card.
	 */
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
	 * @return The image name of this Card.
	 */
	@Override
	public List<String> imageNames()
	{
		return Arrays.asList(imageName);
	}

	/**
	 * @return A list containing all supertypes, card types, and subtypes of this Card.
	 */
	@Override
	public List<Set<String>> allTypes()
	{
		return Arrays.asList(allTypes);
	}

	/**
	 * @return This Card's color identity.
	 */
	@Override
	public ManaType.Tuple colorIdentity()
	{
		return colorIdentity;
	}
}
