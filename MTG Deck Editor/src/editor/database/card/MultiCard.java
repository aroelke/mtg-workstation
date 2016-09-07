package editor.database.card;

import java.util.ArrayList;
import java.util.Arrays;
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
	 * Create a new MultiCard out of the given list of Cards. Each one should only have one face.
	 * 
	 * @param layout TODO: Comment this
	 * @param f Cards to use as faces
	 */
	public MultiCard(CardLayout layout, List<Card> f)
	{
		super(f.get(0).expansion(), layout, f.size());
		
		faces = f;
		for (Card face: faces)
			if (face.faces() > 1)
				throw new IllegalArgumentException("Only normal, single-faced cards can be joined into a multi-faced card");
	}
	
	/**
	 * Create a new MultiCard out of the given Cards.
	 * 
	 * @param layout TODO: Comment this
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
		return faces.stream().map((f) -> characteristic.apply(f).get(0)).collect(Collectors.toList());
	}

	/**
	 * @return A list containing the names of all of this MultiCard's faces.
	 */
	@Override
	public List<String> name()
	{
		return collect(Card::name);
	}

	/**
	 * @return A list containing the mana costs of all of this MultiCard's faces.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(collect(Card::manaCost));
	}

	/**
	 * @return A list containing the converted mana costs of all of this MultiCard's faces.
	 */
	@Override
	public List<Double> cmc()
	{
		return collect(Card::cmc);
	}

	/**
	 * @return A list containing all of this MultiCard's colors.
	 */
	@Override
	public ManaType.Tuple colors()
	{
		Set<ManaType> colors = new HashSet<ManaType>();
		for (Card face: faces)
			colors.addAll(face.colors());
		return new ManaType.Tuple(colors);
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
		return faces.get(face).colors();
	}

	/**
	 * @return A list containing the colors in this MultiCard's color identity.
	 */
	@Override
	public ManaType.Tuple colorIdentity()
	{
		Set<ManaType> colors = new HashSet<ManaType>();
		for (Card face: faces)
			colors.addAll(face.colorIdentity());
		return new ManaType.Tuple(colors);
	}

	/**
	 * @return A set containing all of the supertypes among this MultiCard's faces.
	 */
	@Override
	public Set<String> supertypes()
	{
		Set<String> supertypes = new HashSet<String>();
		for (Card face: faces)
			supertypes.addAll(face.supertypes());
		return supertypes;
	}

	/**
	 * @return A set containing all of the types among this MultiCard's faces.
	 */
	@Override
	public Set<String> types()
	{
		Set<String> types = new HashSet<String>();
		for (Card face: faces)
			types.addAll(face.types());
		return types;
	}

	/**
	 * @return A set containing all of the subtypes among this MultiCard's faces.
	 */
	@Override
	public Set<String> subtypes()
	{
		Set<String> subtypes = new HashSet<String>();
		for (Card face: faces)
			subtypes.addAll(face.subtypes());
		return subtypes;
	}

	/**
	 * @return A list of sets which each contains the supertypes, types, and subtypes
	 * of each of this MultiCard's faces.
	 */
	@Override
	public List<Set<String>> allTypes()
	{
		List<Set<String>> allTypes = new ArrayList<Set<String>>();
		for (Card face: faces)
		{
			HashSet<String> faceTypes = new HashSet<String>();
			faceTypes.addAll(face.supertypes());
			faceTypes.addAll(face.types());
			faceTypes.addAll(face.subtypes());
			allTypes.add(faceTypes);
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
		return collect(Card::typeLine);
	}

	/**
	 * @return This MultiCard's Rarity.
	 */
	@Override
	public Rarity rarity()
	{
		return faces.get(0).rarity();
	}

	/**
	 * @return A list containing the oracle text of each of this MultiCard's faces.
	 */
	@Override
	public List<String> oracleText()
	{
		return collect(Card::oracleText);
	}

	/**
	 * @return A list containing the flavor text of each of this MultiCard's faces.
	 */
	@Override
	public List<String> flavorText()
	{
		return collect(Card::flavorText);
	}

	/**
	 * @return A list containing each of this MultiCard's artists.
	 */
	@Override
	public List<String> artist()
	{
		return collect(Card::artist);
	}

	/**
	 * @return A list containing the collector's number of each of this MultiCard's faces.
	 */
	@Override
	public List<String> number()
	{
		return collect(Card::number);
	}

	/**
	 * @return A list containing the power of each of this MultiCard's faces (that's a creature).
	 */
	@Override
	public PowerToughness.Tuple power()
	{
		return new PowerToughness.Tuple(collect(Card::power));
	}

	/**
	 * @return A list containing the toughness of each of this MultiCard's faces (that's a
	 * creature).
	 */
	@Override
	public PowerToughness.Tuple toughness()
	{
		return new PowerToughness.Tuple(collect(Card::toughness));
	}

	/**
	 * @return A list containing the loyalty of each of this MultiCard's faces (that's a
	 * planeswalker).
	 */
	@Override
	public Loyalty.Tuple loyalty()
	{
		return new Loyalty.Tuple(collect(Card::loyalty));
	}

	/**
	 * @return A map containing dates and rulings for all of this MultiCard's faces
	 * corresponding to those dates.
	 */
	@Override
	public Map<Date, List<String>> rulings()
	{
		Map<Date, List<String>> rulings = faces.stream().map(Card::rulings).reduce(new TreeMap<Date, List<String>>(), (a, b) -> {
			for (Date k: b.keySet())
			{
				if (!a.containsKey(k))
					a.put(k, new ArrayList<String>());
				a.get(k).addAll(b.get(k));
			}
			return a;
		});
		return rulings;
	}

	/**
	 * @return A map containing formats and this MultiCard's legality in them.
	 */
	@Override
	public Map<String, Legality> legality()
	{
		return faces.get(0).legality();
	}

	/**
	 * @return A list containing the image names of each of this MultiCard's faces.
	 */
	@Override
	public List<String> imageNames()
	{
		return collect(Card::imageNames);
	}
}
