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

import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.PowerToughness;
import editor.database.characteristics.Rarity;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
public abstract class MultiCard implements Card
{
	private List<Card> faces;
	
	public MultiCard(List<Card> f)
	{
		faces = f;
		for (Card face: faces)
			if (face.faces() > 1)
				throw new IllegalArgumentException("Only normal, single-faced cards can be joined into a multi-faced card");
	}
	
	public MultiCard(Card... f)
	{
		this(Arrays.asList(f));
	}
	
	private <T> List<T> collect(Function<Card, List<T>> characteristic)
	{
		return faces.stream().map((f) -> characteristic.apply(f).get(0)).collect(Collectors.toList());
	}

	@Override
	public CardLayout layout()
	{
		return CardLayout.SPLIT;
	}

	@Override
	public int faces()
	{
		return faces.size();
	}

	@Override
	public List<String> name()
	{
		return collect(Card::name);
	}

	@Override
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(collect(Card::manaCost));
	}

	@Override
	public List<Double> cmc()
	{
		return collect(Card::cmc);
	}

	@Override
	public ManaType.Tuple colors()
	{
		Set<ManaType> colors = new HashSet<ManaType>();
		for (Card face: faces)
			colors.addAll(face.colors());
		return new ManaType.Tuple(colors);
	}

	@Override
	public ManaType.Tuple colorIdentity()
	{
		Set<ManaType> colors = new HashSet<ManaType>();
		for (Card face: faces)
			colors.addAll(face.colorIdentity());
		return new ManaType.Tuple(colors);
	}

	@Override
	public Set<String> supertypes()
	{
		Set<String> supertypes = new HashSet<String>();
		for (Card face: faces)
			supertypes.addAll(face.supertypes());
		return supertypes;
	}

	@Override
	public Set<String> types()
	{
		Set<String> types = new HashSet<String>();
		for (Card face: faces)
			types.addAll(face.types());
		return types;
	}

	@Override
	public Set<String> subtypes()
	{
		Set<String> subtypes = new HashSet<String>();
		for (Card face: faces)
			subtypes.addAll(face.subtypes());
		return subtypes;
	}

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

	@Override
	public List<String> typeLine()
	{
		return collect(Card::typeLine);
	}

	@Override
	public Expansion expansion()
	{
		return faces.get(0).expansion();
	}

	@Override
	public Rarity rarity()
	{
		return faces.get(0).rarity();
	}

	@Override
	public List<String> oracleText()
	{
		return collect(Card::oracleText);
	}

	@Override
	public List<String> flavorText()
	{
		return collect(Card::flavorText);
	}

	@Override
	public List<String> artist()
	{
		return collect(Card::artist);
	}

	@Override
	public List<String> number()
	{
		return collect(Card::number);
	}

	@Override
	public PowerToughness.Tuple power()
	{
		return new PowerToughness.Tuple(collect(Card::power));
	}

	@Override
	public PowerToughness.Tuple toughness()
	{
		return new PowerToughness.Tuple(collect(Card::toughness));
	}

	@Override
	public Loyalty.Tuple loyalty()
	{
		return new Loyalty.Tuple(collect(Card::loyalty));
	}

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

	@Override
	public Map<String, Legality> legality()
	{
		return faces.get(0).legality();
	}

	@Override
	public List<String> imageNames()
	{
		return collect(Card::imageNames);
	}
	
	@Override
	public String toString()
	{
		return unifiedName();
	}
	
	@Override
	public int hashCode()
	{
		return id().hashCode();
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
			return true;
		if (other == null)
			return true;
		if (other.getClass() != getClass())
			return false;
		return id().equals(((MultiCard)other).id());
	}
}
