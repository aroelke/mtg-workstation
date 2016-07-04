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
 * TODO: Add a user-controlled tag to cards.
 * TODO: Fix CMC function for double-faced cards
 * (currently the back face has 0 CMC when it should be the same as the front)
 * TODO: Make this have subclasses for each type of card (flip, double-sided, split)
 * 
 * @author Alec Roelke
 */
public class NormalCard implements Card
{
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
	 * Rulings for this Card.
	 */
	private final Map<Date, List<String>> rulings;
	
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
	 * TODO: This doesn't work properly with reminder text (like extort).
	 */
	private final ManaType.Tuple colorIdentity;
	
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
	public NormalCard(String name,
				String mana,
				List<ManaType> colors,
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
				TreeMap<Date, List<String>> rulings,
				Map<String, Legality> legality,
				String imageName)
	{
		faces = new Face[] {new Face(name,
									 new ManaCost(mana),
									 new ManaType.Tuple(colors),
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
		this.rulings = rulings;
		this.legality = Collections.unmodifiableMap(legality);
		
		// Create the UID for this Card
		ID = this.set.code + unifiedName() + faces[0].imageName;
		
		// Create this Card's color identity
		List<ManaType> identity = new ArrayList<ManaType>(colors);
		identity.addAll(faces[0].mana.colors());
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
	
	/**
	 * Create a new Card from the list of single-faced Cards.  Each Card in the list
	 * represents a face of the new Card, and should be in order that the faces appear
	 * on the actual card (i.e. front/left/unflipped first).
	 * 
	 * @param abstractCards List of faces to create the new card from
	 */
	public NormalCard(List<Card> abstractCards)
	{
		List<NormalCard> cards = abstractCards.stream().map((c) -> (NormalCard)c).collect(Collectors.toList());
		
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
		rulings = cards.stream().map(Card::rulings).reduce(new TreeMap<Date, List<String>>(), (a, b) -> {
			for (Date k: b.keySet())
			{
				if (!a.containsKey(k))
					a.put(k, new ArrayList<String>());
				a.get(k).addAll(b.get(k));
			}
			return a;
		});
		legality = cards.get(0).legality;
		
		// Create the UID for this Card
		ID = set.code + unifiedName() + faces[0].imageName;
		
		// Create this Card's color identity
		List<ManaType> identity = new ArrayList<ManaType>();
		for (Face f: faces)
		{
			identity.addAll(f.colors);
			Matcher m = ManaCost.MANA_COST_PATTERN.matcher(f.text);
			while (m.find())
				for (ManaType col: ManaCost.valueOf(m.group()).colors())
					identity.add(col);
			for (String sub: f.subtypes)
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
		}
		ManaType.sort(identity);
		colorIdentity = new ManaType.Tuple(identity);
	}
	
	@Override
	public CardLayout layout()
	{
		return CardLayout.NORMAL;
	}
	
	/**
	 * @return The list of names of the faces of this Card.
	 */
	@Override
	public List<String> name()
	{
		return Arrays.stream(faces).map((f) -> f.name).collect(Collectors.toList());
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
		return new ManaCost.Tuple(Arrays.stream(faces).map((f) -> f.mana).toArray(ManaCost[]::new));
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
		ArrayList<ManaType> colors = new ArrayList<ManaType>();
		for (Face face: faces)
			colors.addAll(face.colors);
		return new ManaType.Tuple(colors);
	}
	
	@Override
	public List<String> typeLine()
	{
		return Arrays.stream(faces).map((f) -> f.typeLine).collect(Collectors.toList());
	}

	/**
	 * @return A list containing all of the supertypes that appear on all of the faces of
	 * this Card.
	 */
	@Override
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
	@Override
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
	@Override
	public List<String> subtypes()
	{
		Set<String> subtypes = new HashSet<String>();
		for (Face face: faces)
			subtypes.addAll(face.subtypes);
		return new ArrayList<String>(subtypes);
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
		return Arrays.stream(faces).map((f) -> f.text).collect(Collectors.toList());
	}

	/**
	 * @return The flavor texts of all of the faces of this Card concatenated together.
	 * This should mostly be used for searching, since using it for display could cause
	 * confusion.
	 */
	@Override
	public List<String> flavorText()
	{
		return Arrays.stream(faces).map((f) -> f.flavor).collect(Collectors.toList());
	}
	
	/**
	 * @return The list of artists for the faces of this Card (probably they are all
	 * the same).
	 */
	@Override
	public List<String> artist()
	{
		return Arrays.stream(faces).map((f) -> f.artist).collect(Collectors.toList());
	}
	
	/**
	 * @return The collector numbers of all faces of this Card.
	 */
	@Override
	public List<String> number()
	{
		return Arrays.stream(faces).map((f) -> f.number).collect(Collectors.toList());
	}
	
	/**
	 * @return A tuple containing the power values of each face of this Card.
	 */
	@Override
	public PowerToughness.Tuple power()
	{
		return new PowerToughness.Tuple(Arrays.stream(faces).map((f) -> f.power).toArray(PowerToughness[]::new));
	}

	/**
	 * @return A tuple containing the toughness values of each face of this Card.
	 */
	@Override
	public PowerToughness.Tuple toughness()
	{
		return new PowerToughness.Tuple(Arrays.stream(faces).map((f) -> f.toughness).toArray(PowerToughness[]::new));
	}
	
	/**
	 * @return A tuple containing the loyalty values of each face of this Card.
	 */
	@Override
	public Loyalty.Tuple loyalty()
	{
		return new Loyalty.Tuple(Arrays.stream(faces).map((f) -> f.loyalty).toArray(Loyalty[]::new));
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
		return Arrays.stream(faces).map((f) -> f.imageName).distinct().collect(Collectors.toList());
	}

	/**
	 * @return A list containing all supertypes, card types, and subtypes of all of the
	 * Faces of this Card.
	 */
	@Override
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
	@Override
	public String id()
	{
		return ID;
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
		if (other.getClass() != getClass())
			return false;
		return ID.equals(((Card)other).id());
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
		public final ManaType.Tuple colors;
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
					ManaType.Tuple colors,
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
