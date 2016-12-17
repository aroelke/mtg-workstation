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

import editor.database.characteristics.CombatStat;
import editor.database.characteristics.Legality;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.Rarity;
import editor.util.Lazy;

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
	private Lazy<List<Set<String>>> allTypes;
	/**
	 * List containing the artist of each of this MultiCard's faces.
	 */
	private Lazy<List<String>> artist;
	/**
	 * List of converted mana costs of the faces of this MultiCard.
	 */
	private Lazy<List<Double>> cmc;
	/**
	 * Tuple of the color identity of this MultiCard.
	 */
	private Lazy<ManaType.Tuple> colorIdentity;
	/**
	 * Tuple of all of the colors of this MultiCard.
	 */
	private Lazy<ManaType.Tuple> colors;
	/**
	 * List of Cards that represent faces.  They should all have exactly one face.
	 */
	private List<Card> faces;
	/**
	 * List containing the flavor text of each of this MultiCard's faces.
	 */
	private Lazy<List<String>> flavorText;
	/**
	 * List containing the image name of each of this MultiCard's faces.
	 */
	private Lazy<List<String>> imageNames;
	/**
	 * Tuple containing the loyalty of each of this MultiCard's faces.
	 */
	private Lazy<List<Integer>> loyalty;
	/**
	 * List of mana costs of the faces of this MultiCard.
	 */
	private Lazy<ManaCost.Tuple> manaCost;
	/**
	 * List of the names of the faces of this MultiCard.
	 */
	private Lazy<List<String>> name;
	/**
	 * List containing the collector's number of each of this MultiCard's faces.
	 */
	private Lazy<List<String>> number;
	/**
	 * List containing the oracle text of each of this MultiCard's faces.
	 */
	private Lazy<List<String>> oracleText;
	/**
	 * Tuple containing the power of each of this MultiCard's faces.
	 */
	private Lazy<List<CombatStat>> power;
	/**
	 * Map containing the rulings of this MultiCard and the dates they were made on.
	 */
	private Lazy<Map<Date, List<String>>> rulings;
	/**
	 * Set of this MultiCard's subtypes including all of its faces.
	 */
	private Lazy<Set<String>> subtypes;
	/**
	 * Set of this MultiCard's supertypes including all of its faces.
	 */
	private Lazy<Set<String>> supertypes;
	/**
	 * Tuple containing the toughness of each of this MultiCard's faces.
	 */
	private Lazy<List<CombatStat>> toughness;
	/**
	 * List containing the type line of each of this MultiCard's faces.
	 */
	private Lazy<List<String>> typeLine;
	/**
	 * Set of this MultiCard's types including all of its faces.
	 */
	private Lazy<Set<String>> types;
	
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
		super(f.get(0).expansion(), layout, f.size());
		
		faces = f;
		for (Card face: faces)
			if (face.faces() > 1)
				throw new IllegalArgumentException("Only normal, single-faced cards can be joined into a multi-faced card");
		
		name = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::name)));
		manaCost = new Lazy<ManaCost.Tuple>(() -> new ManaCost.Tuple(collect(Card::manaCost)));
		cmc = new Lazy<List<Double>>(() -> Collections.unmodifiableList(collect(Card::cmc)));
		colors = new Lazy<ManaType.Tuple>(() -> {
			Set<ManaType> cols = new HashSet<ManaType>();
			for (Card face: faces)
				cols.addAll(face.colors());
			return new ManaType.Tuple(cols);
		});
		colorIdentity = new Lazy<ManaType.Tuple>(() -> {
			Set<ManaType> colors = new HashSet<ManaType>();
			for (Card face: faces)
				colors.addAll(face.colorIdentity());
			return new ManaType.Tuple(colors);
		});
		supertypes = new Lazy<Set<String>>(() -> {
			Set<String> s = new HashSet<String>();
			for (Card face: faces)
				s.addAll(face.supertypes());
			return Collections.unmodifiableSet(s);
		});
		types = new Lazy<Set<String>>(() -> {
			Set<String> t = new HashSet<String>();
			for (Card face: faces)
				t.addAll(face.types());
			return Collections.unmodifiableSet(t);
		});
		subtypes = new Lazy<Set<String>>(() -> {
			Set<String> s = new HashSet<String>();
			for (Card face: faces)
				s.addAll(face.subtypes());
			return Collections.unmodifiableSet(s);
		});
		allTypes = new Lazy<List<Set<String>>>(() -> {
			List<Set<String>> a = new ArrayList<Set<String>>();
			for (Card face: faces)
			{
				Set<String> faceTypes = new HashSet<String>();
				faceTypes.addAll(face.supertypes());
				faceTypes.addAll(face.types());
				faceTypes.addAll(face.subtypes());
				a.add(Collections.unmodifiableSet(faceTypes));
			}
			return Collections.unmodifiableList(a);
		});
		typeLine = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::typeLine)));
		oracleText = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::oracleText)));
		flavorText = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::flavorText)));
		artist = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::artist)));
		number = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::number)));
		power = new Lazy<List<CombatStat>>(() -> Collections.unmodifiableList(collect(Card::power)));
		toughness = new Lazy<List<CombatStat>>(() -> Collections.unmodifiableList(collect(Card::toughness)));
		loyalty = new Lazy<List<Integer>>(() -> Collections.unmodifiableList(collect(Card::loyalty)));
		rulings = new Lazy<Map<Date, List<String>>>(() -> Collections.unmodifiableMap(faces.stream().map(Card::rulings).reduce(new TreeMap<Date, List<String>>(), (a, b) -> {
				for (Date k: b.keySet())
				{
					if (!a.containsKey(k))
						a.put(k, new ArrayList<String>());
					a.get(k).addAll(b.get(k));
				}
				return a;
			})));
		imageNames = new Lazy<List<String>>(() -> Collections.unmodifiableList(collect(Card::imageNames)));
	}
	
	@Override
	public List<Set<String>> allTypes()
	{
		return allTypes.get();
	}

	@Override
	public List<String> artist()
	{
		return artist.get();
	}

	@Override
	public List<Double> cmc()
	{
		return cmc.get();
	}

	/**
	 * Collect the values of the give characteristic from each face into a list.
	 * 
	 * @param characteristic characteristic to collect
	 * @return the value of the characteristic for each face of this MultiCard collected into a list.
	 */
	private <T> List<T> collect(Function<Card, List<T>> characteristic)
	{
		return faces.stream().map((f) -> characteristic.apply(f).get(0)).collect(Collectors.toList());
	}

	@Override
	public ManaType.Tuple colorIdentity()
	{
		return colorIdentity.get();
	}

	@Override
	public ManaType.Tuple colors()
	{
		return colors.get();
	}

	@Override
	public ManaType.Tuple colors(int face) throws IndexOutOfBoundsException
	{
		return faces.get(face).colors();
	}

	@Override
	public List<String> flavorText()
	{
		return flavorText.get();
	}

	@Override
	public List<String> imageNames()
	{
		return imageNames.get();
	}

	@Override
	public Map<String, Legality> legality()
	{
		return faces.get(0).legality();
	}

	@Override
	public List<Integer> loyalty()
	{
		return loyalty.get();
	}

	@Override
	public ManaCost.Tuple manaCost()
	{
		return manaCost.get();
	}

	@Override
	public List<String> name()
	{
		return name.get();
	}

	@Override
	public List<String> number()
	{
		return number.get();
	}

	@Override
	public List<String> oracleText()
	{
		return oracleText.get();
	}

	@Override
	public List<CombatStat> power()
	{
		return power.get();
	}

	@Override
	public Rarity rarity()
	{
		return faces.get(0).rarity();
	}

	@Override
	public Map<Date, List<String>> rulings()
	{
		return rulings.get();
	}

	@Override
	public Set<String> subtypes()
	{
		return subtypes.get();
	}

	@Override
	public Set<String> supertypes()
	{
		return supertypes.get();
	}

	@Override
	public List<CombatStat> toughness()
	{
		return toughness.get();
	}

	@Override
	public List<String> typeLine()
	{
		return typeLine.get();
	}

	@Override
	public Set<String> types()
	{
		return types.get();
	}
}
