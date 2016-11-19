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
	 * List containing the set of types for each of this MultiCard's faces.
	 */
	private List<Set<String>> allTypes;
	/**
	 * List containing the artist of each of this MultiCard's faces.
	 */
	private List<String> artist;
	/**
	 * List of converted mana costs of the faces of this MultiCard.
	 */
	private List<Double> cmc;
	/**
	 * Tuple of the color identity of this MultiCard.
	 */
	private ManaType.Tuple colorIdentity;
	/**
	 * Tuple of all of the colors of this MultiCard.
	 */
	private ManaType.Tuple colors;
	/**
	 * List of Cards that represent faces.  They should all have exactly one face.
	 */
	private List<Card> faces;
	/**
	 * List containing the flavor text of each of this MultiCard's faces.
	 */
	private List<String> flavorText;
	/**
	 * List containing the image name of each of this MultiCard's faces.
	 */
	private List<String> imageNames;
	/**
	 * Tuple containing the loyalty of each of this MultiCard's faces.
	 */
	private Loyalty.Tuple loyalty;
	/**
	 * List of mana costs of the faces of this MultiCard.
	 */
	private ManaCost.Tuple manaCost;
	/**
	 * List of the names of the faces of this MultiCard.
	 */
	private List<String> name;
	/**
	 * List containing the collector's number of each of this MultiCard's faces.
	 */
	private List<String> number;
	/**
	 * List containing the oracle text of each of this MultiCard's faces.
	 */
	private List<String> oracleText;
	/**
	 * Tuple containing the power of each of this MultiCard's faces.
	 */
	private PowerToughness.Tuple power;
	/**
	 * Map containing the rulings of this MultiCard and the dates they were made on.
	 */
	private Map<Date, List<String>> rulings;
	/**
	 * Set of this MultiCard's subtypes including all of its faces.
	 */
	private Set<String> subtypes;
	/**
	 * Set of this MultiCard's supertypes including all of its faces.
	 */
	private Set<String> supertypes;
	/**
	 * Tuple containing the toughness of each of this MultiCard's faces.
	 */
	private PowerToughness.Tuple toughness;
	/**
	 * List containing the type line of each of this MultiCard's faces.
	 */
	private List<String> typeLine;
	/**
	 * Set of this MultiCard's types including all of its faces.
	 */
	private Set<String> types;
	
	/**
	 * Create a new MultiCard out of the given cards.
	 * 
	 * @param layout layout of the new MultiCard, which should be one that has muliple faces
	 * @param f cards to use as faces
	 */
	public MultiCard(CardLayout layout, Card... f)
	{
		this(layout, Arrays.asList(f));
	}
	
	/**
	 * Create a new MultiCard out of the given list of Cards. Each one should only have one face.
	 * 
	 * @param layout layout of the new MultiCard, which should be one that has multiple faces
	 * @param f cards to use as faces
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

	@Override
	public List<String> artist()
	{
		if (artist == null)
			artist = Collections.unmodifiableList(collect(Card::artist));
		return artist;
	}

	@Override
	public List<Double> cmc()
	{
		if (cmc == null)
			cmc = Collections.unmodifiableList(collect(Card::cmc));
		return cmc;
	}

	/**
	 * Collect the values of the give characteristic from each face into a list.
	 * 
	 * @param characteristic characteristic to collect
	 * @return the value of the characteristic for each face of this MultiCard collected into a list.
	 */
	private <T> List<T> collect(Function<Card, List<T>> characteristic)
	{
		return faces.stream().map((f) -> characteristic.apply(f)[0]).collect(Collectors.toList());
	}

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

	@Override
	public ManaType.Tuple colors(int face) throws IndexOutOfBoundsException
	{
		return faces[face].colors();
	}

	@Override
	public List<String> flavorText()
	{
		if (flavorText == null)
			flavorText = Collections.unmodifiableList(collect(Card::flavorText));
		return flavorText;
	}

	@Override
	public List<String> imageNames()
	{
		if (imageNames == null)
			imageNames = Collections.unmodifiableList(collect(Card::imageNames));
		return imageNames;
	}

	@Override
	public Map<String, Legality> legality()
	{
		return faces[0].legality();
	}

	@Override
	public Loyalty.Tuple loyalty()
	{
		if (loyalty == null)
			loyalty = new Loyalty.Tuple(collect(Card::loyalty));
		return loyalty;
	}

	@Override
	public ManaCost.Tuple manaCost()
	{
		if (manaCost == null)
			manaCost = new ManaCost.Tuple(collect(Card::manaCost));
		return manaCost;
	}

	@Override
	public List<String> name()
	{
		if (name == null)
			name = Collections.unmodifiableList(collect(Card::name));
		return name;
	}

	@Override
	public List<String> number()
	{
		if (number == null)
			number = Collections.unmodifiableList(collect(Card::number));
		return number;
	}

	@Override
	public List<String> oracleText()
	{
		if (oracleText == null)
			oracleText = Collections.unmodifiableList(collect(Card::oracleText));
		return oracleText;
	}

	@Override
	public PowerToughness.Tuple power()
	{
		if (power == null)
			power = new PowerToughness.Tuple(collect(Card::power));
		return power;
	}

	@Override
	public Rarity rarity()
	{
		return faces[0].rarity();
	}

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

	@Override
	public PowerToughness.Tuple toughness()
	{
		if (toughness == null)
			toughness = new PowerToughness.Tuple(collect(Card::toughness));
		return toughness;
	}

	@Override
	public List<String> typeLine()
	{
		if (typeLine == null)
			typeLine = Collections.unmodifiableList(collect(Card::typeLine));
		return typeLine;
	}

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
}
