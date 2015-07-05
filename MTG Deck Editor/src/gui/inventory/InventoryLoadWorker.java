package gui.inventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import database.Card;
import database.Inventory;
import database.characteristics.Expansion;
import database.characteristics.Legality;
import database.characteristics.MTGColor;
import database.characteristics.Rarity;

/**
 * This class represents a worker that loads cards from a JSON file in the background.
 * 
 * TODO: Make this conform to proper use of Swing multithreading
 * (i.e., don't create the inventory or populate the lists of sets, etc., in doInBackground())
 * 
 * @author Alec Roelke
 */
public class InventoryLoadWorker extends SwingWorker<Inventory, String>
{
	/**
	 * Dialog displaying progress information.
	 */
	private InventoryLoadDialog dialog;
	/**
	 * File to load from.
	 */
	private File file;
	
	/**
	 * Create a new InventoryWorker.
	 * 
	 * @param d Dialog to show progress
	 */
	public InventoryLoadWorker(InventoryLoadDialog d, File f)
	{
		super();
		dialog = d;
		file = f;
		
		dialog.setIndeterminate(true);
		addPropertyChangeListener((e) -> {
			if ("progress".equals(e.getPropertyName()))
			{
				int p = (Integer)e.getNewValue();
				dialog.setIndeterminate(p < 0);
				dialog.setValue(p);
			}
		});
	}
	
	/**
	 * Change the label in the dialog to match the stage this worker is in.
	 */
	@Override
	protected void process(List<String> chunks)
	{
		for (String chunk: chunks)
			dialog.setStage(chunk);
	}
	
	/**
	 * Import a list of all cards that exist in Magic: the Gathering from a JSON file downloaded from
	 * @link{http://www.mtgjson.com}.  Also populate the lists of types and expansions (and their blocks).
	 * 
	 * @return The inventory of cards that can be added to a deck.
	 */
	@Override
	protected Inventory doInBackground() throws Exception
	{
		publish("Opening " + file.getName() + "...");
		
		ArrayList<Card> cards = new ArrayList<Card>();
		Set<String> expansionNames = new HashSet<String>();
		Set<String> blockNames = new HashSet<String>();
		Set<String> supertypeSet = new HashSet<String>();
		Set<String> typeSet = new HashSet<String>();
		Set<String> subtypeSet = new HashSet<String>();
		Set<String> formatSet = new HashSet<String>();
		
		// Read the inventory file
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")))
		{
			publish("Parsing " + file.getName() + "...");
			JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
			int numCards = 0;
			for (Map.Entry<String, JsonElement> setNode: root.entrySet())
				numCards += setNode.getValue().getAsJsonObject().get("cards").getAsJsonArray().size();
			
			publish("Reading cards from " + file.getName() + "...");
			int cardsProcessed = 0;
			setProgress(cardsProcessed);
			for (Map.Entry<String, JsonElement> setNode: root.entrySet())
			{
				if (isCancelled())
				{
					expansionNames.clear();
					blockNames.clear();
					supertypeSet.clear();
					typeSet.clear();
					subtypeSet.clear();
					formatSet.clear();
					cards.clear();
					break;
				}
				
				// Create the new Expansion
				JsonObject setProperties = setNode.getValue().getAsJsonObject();
				JsonArray setCards = setProperties.get("cards").getAsJsonArray();
				Expansion set = new Expansion(setProperties.get("name").getAsString(),
											  setProperties.has("block") ? setProperties.get("block").getAsString() : "<No Block>", 
											  setProperties.get("code").getAsString(), 
											  setCards.size());
				expansionNames.add(set.name);
				blockNames.add(set.block);
				publish("Loading cards from " + set.name + "...");
				
				for (JsonElement cardElement: setCards)
				{
					// Create the new card for the expansion
					JsonObject card = cardElement.getAsJsonObject();
					
					// If the card is a token, skip it
					String layout = card.get("layout").getAsString();
					if (layout.equals("token"))
						continue;
					
					// Card's name
					String name = card.get("name").getAsString();
					
					// Card's mana cost
					String mana = card.has("manaCost") ? card.get("manaCost").getAsString() : "";
					
					// Card's set of colors (which is stored as a list, since order matters)
					List<MTGColor> colors = new ArrayList<MTGColor>();
					if (card.has("colors"))
					{
						JsonArray colorsArray = card.get("colors").getAsJsonArray();
						for (JsonElement colorElement: colorsArray)
							colors.add(MTGColor.get(colorElement.getAsString()));
					}
					colors.sort((a, b) -> a.colorOrder(b));
					
					// Card's set of supertypes
					List<String> supertypes = new ArrayList<String>();
					if (card.has("supertypes"))
					{
						JsonArray superArray = card.get("supertypes").getAsJsonArray();
						for (JsonElement superElement: superArray)
						{
							supertypes.add(superElement.getAsString());
							supertypeSet.add(superElement.getAsString());
						}
					}
					
					// Card's set of types
					List<String> types = new ArrayList<String>();
					for (JsonElement typeElement: card.get("types").getAsJsonArray())
					{
						types.add(typeElement.getAsString());
						typeSet.add(typeElement.getAsString());
					}
					
					// Card's set of subtypes
					List<String> subtypes = new ArrayList<String>();
					if (card.has("subtypes"))
					{
						for (JsonElement subElement: card.get("subtypes").getAsJsonArray())
						{
							subtypes.add(subElement.getAsString());
							subtypeSet.add(subElement.getAsString());
						}
					}
					
					// Card's rarity
					Rarity rarity = Rarity.get(card.get("rarity").getAsString());
					
					// Card's rules text
					String text = card.has("text") ? card.get("text").getAsString() : "";
					
					// Card's flavor text
					String flavor = card.has("flavor") ? card.get("flavor").getAsString() : "";
					
					// Card's artist
					String artist = card.get("artist").getAsString();
					
					// Card's number (this is a string since some don't have numbers or are things like "1a")
					String number = card.has("number") ? card.get("number").getAsString() : "--";
					
					// Card's power and toughness (empty if it doesn't have power or toughness)
					String power = card.has("power") ? card.get("power").getAsString() : "";
					String toughness = card.has("toughness") ? card.get("toughness").getAsString() : "";
			
					// Card's loyalty (empty if it isn't a planeswalker or is Garruk, the Veil-Cursed)
					String loyalty = card.has("loyalty") ? card.get("loyalty").getAsString() : "";
					
					// Card's legality in formats
					Map<String, Legality> legality = new HashMap<String, Legality>();
					if (card.has("legalities"))
					{
						for (Map.Entry<String, JsonElement> l: card.get("legalities").getAsJsonObject().entrySet())
						{
							formatSet.add(l.getKey());
							legality.put(l.getKey(), Legality.get(l.getValue().getAsString()));
						}
					}
					
					// Card's image name
					String imageName = card.get("imageName").getAsString();
					
					// Create the new card with all the values acquired above
					cards.add(new Card(name,
									   mana,
									   colors,
									   supertypes,
									   types,
									   subtypes,
									   rarity,
									   set,
									   text,
									   flavor,
									   artist,
									   number,
									   power,
									   toughness,
									   loyalty,
									   layout,
									   legality,
									   imageName));
					setProgress(++cardsProcessed*100/numCards);
				}
			}
			
			// Store the lists of expansion and block names and types and sort them alphabetically
			Expansion.expansions = expansionNames.toArray(new String[expansionNames.size()]);
			Arrays.sort(Expansion.expansions);
			Expansion.blocks = blockNames.toArray(new String[blockNames.size()]);
			Arrays.sort(Expansion.blocks);
			Card.supertypeList = supertypeSet.toArray(new String[supertypeSet.size()]);
			Arrays.sort(Card.supertypeList);
			Card.typeList = typeSet.toArray(new String[typeSet.size()]);
			Arrays.sort(Card.typeList);
			Card.subtypeList = subtypeSet.toArray(new String[subtypeSet.size()]);
			Arrays.sort(Card.subtypeList);
			Card.formatList = formatSet.toArray(new String[formatSet.size()]);
			Arrays.sort(Card.formatList);
		}
		
		return new Inventory(cards);
	}
	
	/**
	 * When this worker is finished, close the dialog and allow it to return the Inventory
	 * that was created.
	 */
	@Override
	protected void done()
	{
		dialog.setVisible(false);
		dialog.dispose();
	}
}
