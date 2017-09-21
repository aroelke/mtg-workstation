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

import editor.database.characteristics.CombatStat;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.Rarity;
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
	 * @see Card#colorIdentity()
	 */
	private final List<ManaType> colorIdentity;
	/**
	 * This SingleCard's colors.
	 */
	public final List<ManaType> colors;
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
	 * Name of this SingleCard.
	 */
	public final String name;
	/**
	 * This SingleCard's collector's number.
	 */
	public final String number;
	/**
	 * This SingleCard's power.
	 * @see Card#power()
	 */
	public final CombatStat power;
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
	 * @see Card#toughness()
	 */
	public final CombatStat toughness;
	/**
	 * This SingleCard's type line.
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
	 * @param rarity the new Card's rarity
	 * @param set the Expansion the new Card belongs to
	 * @param text the new Card's rules text
	 * @param flavor the new Card's flavor text
	 * @param artist the new Card's artist
	 * @param number the new Card's collector's number
	 * @param power the new Card's power
	 * @param toughness the new Card's toughness
	 * @param loyalty the new Card's loyalty (0 or less for nonexistent)
	 * @param legality the new Card's legality
	 * @param imageName the new Card's image name
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
			int multiverseid,
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
		this.mana = ManaCost.parseManaCost(mana);
		this.supertypes = Collections.unmodifiableSet(supertype);
		this.types = Collections.unmodifiableSet(type);
		this.subtypes = Collections.unmodifiableSet(subtype);
		this.text = text;
		this.flavor = flavor;
		this.artist = artist;
		this.number = number;
		this.multiverseid = multiverseid;
		this.power = new CombatStat(power);
		this.toughness = new CombatStat(toughness);
		this.loyalty = new Loyalty(loyalty);
		this.imageName = imageName;
		this.rarity = rarity;
		this.rulings = rulings;
		this.legality = Collections.unmodifiableMap(legality);

		isLand = typeContains("land");
		
		// Create the type line for this Card
		StringBuilder str = new StringBuilder();
		if (supertypes.size() > 0)
			str.append(String.join(" ", supertypes)).append(" ");
		str.append(String.join(" ", types));
		if (subtypes.size() > 0)
			str.append(" " + UnicodeSymbols.EM_DASH + " ").append(String.join(" ", subtypes));
		typeLine = str.toString();

		ManaType.sort(colors);
		this.colors = Collections.unmodifiableList(colors);
		if (colorIdentity.isEmpty())
		{
			// Try to infer the color identity if it's missing
			colorIdentity.addAll(this.mana.colors());
			Matcher m = ManaCost.MANA_COST_PATTERN.matcher(text.replaceAll("\\(.*\\)", ""));
			while (m.find())
				for (ManaType col: ManaCost.parseManaCost(m.group()).colors())
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
		ManaType.sort(colorIdentity);
		this.colorIdentity = Collections.unmodifiableList(colorIdentity);

		Set<String> faceTypes = new HashSet<String>();
		faceTypes.addAll(supertypes);
		faceTypes.addAll(types);
		faceTypes.addAll(subtypes);
		allTypes = Collections.unmodifiableSet(faceTypes);
	}

	@Override
	public List<Set<String>> allTypes()
	{
		return Arrays.asList(allTypes);
	}

	@Override
	public List<String> artist()
	{
		return Arrays.asList(artist);
	}

	@Override
	public List<Double> cmc()
	{
		return Arrays.asList(mana.cmc());
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
	public List<String> flavorText()
	{
		return Arrays.asList(flavor);
	}

	@Override
	public List<String> imageNames()
	{
		return Arrays.asList(imageName);
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
		return Arrays.asList(loyalty);
	}

	@Override
	public List<ManaCost> manaCost()
	{
		return Arrays.asList(mana);
	}

	@Override
	public List<String> name()
	{
		return Arrays.asList(name);
	}

	@Override
	public List<String> number()
	{
		return Arrays.asList(number);
	}

	@Override
	public List<Integer> multiverseid()
	{
		return Arrays.asList(multiverseid);
	}
	
	@Override
	public List<String> oracleText()
	{
		return Arrays.asList(text);
	}

	@Override
	public List<CombatStat> power()
	{
		return Arrays.asList(power);
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
		return Arrays.asList(toughness);
	}

	@Override
	public List<String> typeLine()
	{
		return Arrays.asList(typeLine);
	}

	@Override
	public Set<String> types()
	{
		return types;
	}
}
