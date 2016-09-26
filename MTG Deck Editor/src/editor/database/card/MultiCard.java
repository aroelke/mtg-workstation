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

import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.PowerToughness;
import editor.database.characteristics.Rarity;

/**
 * This class represents an abstract card with multiple faces.
 * 
 * @author Alec Roelke
 */
public abstract class MultiCard extends Card
{
	/**
	 * List of Cards that represent faces.  They should all have exactly one face.
	 */
	private List<Card> faces;
	
	/**
	 * List of the names of the faces of this MultiCard.
	 */
	private List<String> name;
	/**
	 * List of mana costs of the faces of this MultiCard.
	 */
	private ManaCost.Tuple manaCost;
	/**
	 * List of converted mana costs of the faces of this MultiCard.
	 */
	private List<Double> cmc;
	/**
	 * Tuple of all of the colors of this MultiCard.
	 */
	private ManaType.Tuple colors;
	/**
	 * Tuple of the color identity of this MultiCard.
	 */
	private ManaType.Tuple colorIdentity;
	/**
	 * Set of this MultiCard's supertypes including all of its faces.
	 */
	private Set<String> supertypes;
	/**
	 * Set of this MultiCard's types including all of its faces.
	 */
	private Set<String> types;
	/**
	 * Set of this MultiCard's subtypes including all of its faces.
	 */
	private Set<String> subtypes;
	/**
	 * List containing the set of types for each of this MultiCard's faces.
	 */
	private List<Set<String>> allTypes;
	/**
	 * List containing the type line of each of this MultiCard's faces.
	 */
	private List<String> typeLine;
	/**
	 * List containing the oracle text of each of this MultiCard's faces.
	 */
	private List<String> oracleText;
	/**
	 * List containing the flavor text of each of this MultiCard's faces.
	 */
	private List<String> flavorText;
	/**
	 * List containing the artist of each of this MultiCard's faces.
	 */
	private List<String> artist;
	/**
	 * List containing the collector's number of each of this MultiCard's faces.
	 */
	private List<String> number;
	/**
	 * Tuple containing the power of each of this MultiCard's faces.
	 */
	private PowerToughness.Tuple power;
	/**
	 * Tuple containing the toughness of each of this MultiCard's faces.
	 */
	private PowerToughness.Tuple toughness;
	/**
	 * Tuple containing the loyalty of each of this MultiCard's faces.
	 */
	private Loyalty.Tuple loyalty;
	/**
	 * Map containing the rulings of this MultiCard and the dates they were made on.
	 */
	private Map<Date, List<String>> rulings;
	/**
	 * List containing the image name of each of this MultiCard's faces.
	 */
	private List<String> imageNames;
	
	/**
	 * Create a new MultiCard out of the given list of Cards. Each one should only have one face.
	 * 
	 * @param layout Layout of the new MultiCard, which should be one that has multiple faces
	 * @param f Cards to use as faces
	 */
	public MultiCard(CardLayout layout, List<Card> f)
	{
		super(f[0].expansion(), layout, f.size());
		
		faces = f;
		for (Card face: faces)
			if (face.faces() > 1)
				throw new IllegalArgumentException("Only normal, single-faced cards can be joined into a multi-faced card");
		
		name = null;
		manaCost = null;
		cmc = null;
		colors = null;
		colorIdentity = null;
		supertypes = null;
		types = null;
		subtypes = null;
		allTypes = null;
		typeLine = null;
		oracleText = null;
		flavorText = null;
		artist = null;
		number = null;
		power = null;
		toughness = null;
		loyalty = null;
		rulings = null;
		imageNames = null;
	}
	
	/**
	 * Create a new MultiCard out of the given Cards.
	 * 
	 * @param layout Layout of the new MultiCard, which should be one that has muliple faces
	 * @param f Cards to use as faces
	 */
	public MultiCard(CardLayout layout, Card... f)
	{
		this(layout, Arrays.asList(f));
	}
	
	/**
	 * @param characteristic Characteristic to collect
	 * @return The value of the characteristic for each face of this MultiCard collected into a list.
	 */
	private <T> List<T> collect(Function<Card, List<T>> characteristic)
	{
		return faces.stream().map((f) -> characteristic.apply(f)[0]).collect(Collectors.toList());
	}

	/**
	 * @return A list containing the names of all of this MultiCard's faces.
	 */
	@Override
	public List<String> name()
	{
		if (name == null)
			name = Collections.unmodifiableList(collect(Card::name));
		return name;
	}

	/**
	 * @return A list containing the mana costs of all of this MultiCard's faces.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		if (manaCost == null)
			manaCost = new ManaCost.Tuple(collect(Card::manaCost));
		return manaCost;
	}

	/**
	 * @return A list containing the converted mana costs of all of this MultiCard's faces.
	 */
	@Override
	public List<Double> cmc()
	{
		if (cmc == null)
			cmc = Collections.unmodifiableList(collect(Card::cmc));
		return cmc;
	}

	/**
	 * @return A list containing all of this MultiCard's colors.
	 */
	@Override
	public ManaType.Tuple colors()
	{
		if (colors == null)
		{
			Set<ManaType> cols = new HashSet<ManaType>();
			for (Card face: faces)
				cols.addAll(face.colors());
			colors = new ManaType.Tuple(cols);
		}
		return colors;
	}
	
	/**
	 * Get the colors of one of the faces of this MultiCard.
	 * 
	 * @param face Index of the face to get the colors of  
	 * @return A list containing the colors of the given face.
	 */
	@Override
	public ManaType.Tuple colors(int face)
	{
		return faces[face].colors();
	}

	/**
	 * @return A list containing the colors in this MultiCard's color identity.
	 */
	@Override
	public ManaType.Tuple colorIdentity()
	{
		if (colorIdentity == null)
		{
			Set<ManaType> colors = new HashSet<ManaType>();
			for (Card face: faces)
				colors.addAll(face.colorIdentity());
			colorIdentity = new ManaType.Tuple(colors);
		}
		return colorIdentity;
	}

	/**
	 * @return A set containing all of the supertypes among this MultiCard's faces.
	 */
	@Override
	public Set<String> supertypes()
	{
		if (supertypes == null)
		{
			Set<String> s = new HashSet<String>();
			for (Card face: faces)
				s.addAll(face.supertypes());
			supertypes = Collections.unmodifiableSet(s);
		}
		return supertypes;
	}

	/**
	 * @return A set containing all of the types among this MultiCard's faces.
	 */
	@Override
	public Set<String> types()
	{
		if (types == null)
		{
			Set<String> t = new HashSet<String>();
			for (Card face: faces)
				t.addAll(face.types());
			types = Collections.unmodifiableSet(t);
		}
		return types;
	}

	/**
	 * @return A set containing all of the subtypes among this MultiCard's faces.
	 */
	@Override
	public Set<String> subtypes()
	{
		if (subtypes == null)
		{
			Set<String> s = new HashSet<String>();
			for (Card face: faces)
				s.addAll(face.subtypes());
			subtypes = Collections.unmodifiableSet(s);
		}
		return subtypes;
	}

	/**
	 * @return A list of sets which each contains the supertypes, types, and subtypes
	 * of each of this MultiCard's faces.
	 */
	@Override
	public List<Set<String>> allTypes()
	{
		if (allTypes == null)
		{
			List<Set<String>> a = new ArrayList<Set<String>>();
			for (Card face: faces)
			{
				Set<String> faceTypes = new HashSet<String>();
				faceTypes.addAll(face.supertypes());
				faceTypes.addAll(face.types());
				faceTypes.addAll(face.subtypes());
				a.add(Collections.unmodifiableSet(faceTypes));
			}
			allTypes = Collections.unmodifiableList(a);
		}
		return allTypes;
	}

	/**
	 * @return A list of Strings containing the formatted type line of each face of
	 * this MultiCard.
	 */
	@Override
	public List<String> typeLine()
	{
		if (typeLine == null)
			typeLine = Collections.unmodifiableList(collect(Card::typeLine));
		return typeLine;
	}

	/**
	 * @return This MultiCard's Rarity.
	 */
	@Override
	public Rarity rarity()
	{
		return faces[0].rarity();
	}

	/**
	 * @return A list containing the oracle text of each of this MultiCard's faces.
	 */
	@Override
	public List<String> oracleText()
	{
		if (oracleText == null)
			oracleText = Collections.unmodifiableList(collect(Card::oracleText));
		return oracleText;
	}

	/**
	 * @return A list containing the flavor text of each of this MultiCard's faces.
	 */
	@Override
	public List<String> flavorText()
	{
		if (flavorText == null)
			flavorText = Collections.unmodifiableList(collect(Card::flavorText));
		return flavorText;
	}

	/**
	 * @return A list containing each of this MultiCard's artists.
	 */
	@Override
	public List<String> artist()
	{
		if (artist == null)
			artist = Collections.unmodifiableList(collect(Card::artist));
		return artist;
	}

	/**
	 * @return A list containing the collector's number of each of this MultiCard's faces.
	 */
	@Override
	public List<String> number()
	{
		if (number == null)
			number = Collections.unmodifiableList(collect(Card::number));
		return number;
	}

	/**
	 * @return A list containing the power of each of this MultiCard's faces (that's a creature).
	 */
	@Override
	public PowerToughness.Tuple power()
	{
		if (power == null)
			power = new PowerToughness.Tuple(collect(Card::power));
		return power;
	}

	/**
	 * @return A list containing the toughness of each of this MultiCard's faces (that's a
	 * creature).
	 */
	@Override
	public PowerToughness.Tuple toughness()
	{
		if (toughness == null)
			toughness = new PowerToughness.Tuple(collect(Card::toughness));
		return toughness;
	}

	/**
	 * @return A list containing the loyalty of each of this MultiCard's faces (that's a
	 * planeswalker).
	 */
	@Override
	public Loyalty.Tuple loyalty()
	{
		if (loyalty == null)
			loyalty = new Loyalty.Tuple(collect(Card::loyalty));
		return loyalty;
	}

	/**
	 * @return A map containing dates and rulings for all of this MultiCard's faces
	 * corresponding to those dates.
	 */
	@Override
	public Map<Date, List<String>> rulings()
	{
		if (rulings == null)
			rulings = Collections.unmodifiableMap(faces.stream().map(Card::rulings).reduce(new TreeMap<Date, List<String>>(), (a, b) -> {
				for (Date k: b.keySet())
				{
					if (!a.containsKey(k))
						a[k] = new ArrayList<String>();
					a[k].addAll(b[k]);
				}
				return a;
			}));
		return rulings;
	}

	/**
	 * @return A map containing formats and this MultiCard's legality in them.
	 */
	@Override
	public Map<String, Legality> legality()
	{
		return faces[0].legality();
	}

	/**
	 * @return A list containing the image names of each of this MultiCard's faces.
	 */
	@Override
	public List<String> imageNames()
	{
		if (imageNames == null)
			imageNames = Collections.unmodifiableList(collect(Card::imageNames));
		return imageNames;
	}
}
