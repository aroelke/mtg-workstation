package editor.gui.inventory;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import editor.collection.Inventory;
import editor.database.Card;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.Rarity;

/**
 * This class represents a dialog that shows the progress for loading the
 * inventory and blocking the main frame until it is finished.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryLoadDialog extends JDialog
{
	/**
	 * Label showing the current stage of loading.
	 */
	private JLabel progressLabel;
	/**
	 * Progress bar showing overall progress of loading.
	 */
	private JProgressBar progressBar;
	/**
	 * Area showing past and current progress of loading.
	 */
	private JTextArea progressArea;
	/**
	 * Worker that loads the inventory.
	 */
	private InventoryLoadWorker worker;
	
	public InventoryLoadDialog(JFrame owner)
	{
		super(owner, "Loading Inventory", Dialog.ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(350, 220));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		worker = null;
		
		// Content panel
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0};
		layout.columnWeights = new double[] {1.0};
		layout.rowHeights = new int[] {0, 0, 0, 0};
		layout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0};
		JPanel contentPanel = new JPanel(layout);
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPanel);
		
		// Stage progress label
		progressLabel = new JLabel("Loading inventory...");
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.anchor = GridBagConstraints.WEST;
		labelConstraints.fill = GridBagConstraints.BOTH;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.insets = new Insets(0, 0, 2, 0);
		contentPanel.add(progressLabel, labelConstraints);
		
		// Overall progress bar
		progressBar = new JProgressBar();
		GridBagConstraints barConstraints = new GridBagConstraints();
		barConstraints.fill = GridBagConstraints.BOTH;
		barConstraints.gridx = 0;
		barConstraints.gridy = 1;
		barConstraints.insets = new Insets(0, 0, 2, 0);
		contentPanel.add(progressBar, barConstraints);
		
		// History text area
		progressArea = new JTextArea();
		progressArea.setEditable(false);
		GridBagConstraints areaConstraints = new GridBagConstraints();
		areaConstraints.fill = GridBagConstraints.BOTH;
		areaConstraints.gridx = 0;
		areaConstraints.gridy = 2;
		areaConstraints.insets = new Insets(0, 0, 10, 0);
		contentPanel.add(new JScrollPane(progressArea), areaConstraints);
		
		// Cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {
			if (worker != null)
				worker.cancel(false);
		});
		GridBagConstraints cancelConstraints = new GridBagConstraints();
		cancelConstraints.gridx = 0;
		cancelConstraints.gridy = 3;
		contentPanel.add(cancelButton, cancelConstraints);
		
		pack();
	}
	
	/**
	 * Make this dialog visible and then begin loading the inventory.  Block until it is
	 * complete, and then return the newly-created Inventory.
	 * 
	 * @return The Inventory that was created.
	 */
	public Inventory createInventory(File file)
	{
		worker = new InventoryLoadWorker(file);
		worker.execute();
		setVisible(true);
		progressArea.setText("");
		try
		{
			return worker.get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			JOptionPane.showMessageDialog(null, "Error loading inventory: " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return new Inventory();
		}
		catch (CancellationException e)
		{
			return new Inventory();
		}
	}
	
	/**
	 * This class represents a worker that loads cards from a JSON file in the background.
	 * 
	 * TODO: Use http://www.mtgsalvation.com/forums/magic-fundamentals/magic-software/494332-mtg-json-new-website-provides-mtg-card-data-in?page=21#c519 to generate card numbers for cards that don't have them
	 * 
	 * @author Alec Roelke
	 */
	private class InventoryLoadWorker extends SwingWorker<Inventory, String>
	{
		/**
		 * File to load from.
		 */
		private File file;
		
		/**
		 * Create a new InventoryWorker.
		 * 
		 * @param d Dialog to show progress
		 */
		public InventoryLoadWorker(File f)
		{
			super();
			file = f;
			
			progressBar.setIndeterminate(true);
			addPropertyChangeListener((e) -> {
				if ("progress".equals(e.getPropertyName()))
				{
					int p = (Integer)e.getNewValue();
					progressBar.setIndeterminate(p < 0);
					progressBar.setValue(p);
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
			{
				progressLabel.setText(chunk);
				progressArea.append(chunk + "\n");
			}
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
			
			List<Card> cards = new ArrayList<Card>();
			Map<Card, List<String>> faces = new HashMap<Card, List<String>>();
			Set<Expansion> expansions = new HashSet<Expansion>();
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
				setProgress(0);
				for (Map.Entry<String, JsonElement> setNode: root.entrySet())
				{
					if (isCancelled())
					{
						expansions.clear();
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
												  (setProperties.has("magicCardsInfoCode") ? setProperties.get("magicCardsInfoCode") : setProperties.get("code")).getAsString().toUpperCase(),
												  (setProperties.has("gathererCode") ? setProperties.get("gathererCode") : setProperties.get("code")).getAsString(),
												  setCards.size());
					expansions.add(set);
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
						List<ManaType> colors = new ArrayList<ManaType>();
						if (card.has("colors"))
						{
							JsonArray colorsArray = card.get("colors").getAsJsonArray();
							for (JsonElement colorElement: colorsArray)
								colors.add(ManaType.get(colorElement.getAsString()));
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
						
						// Card's rulings
						TreeMap<Date, List<String>> rulings = new TreeMap<Date, List<String>>();
						DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						if (card.has("rulings"))
						{
							for (JsonElement l: card.get("rulings").getAsJsonArray())
							{
								JsonObject o = l.getAsJsonObject();
								Date date = format.parse(o.get("date").getAsString());
								String ruling = o.get("text").getAsString();
								if (!rulings.containsKey(date))
									rulings.put(date, new ArrayList<String>());
								rulings.get(date).add(ruling);
							}
						}
						
						// Card's legality in formats
						Map<String, Legality> legality = new HashMap<String, Legality>();
						if (card.has("legalities"))
						{
							for (JsonElement l: card.get("legalities").getAsJsonArray())
							{
								JsonObject o = l.getAsJsonObject();
								formatSet.add(o.get("format").getAsString());
								legality.put(o.get("format").getAsString(), Legality.get(o.get("legality").getAsString()));
							}
						}
						
						// Card's image name
						String imageName = card.get("imageName").getAsString();
						
						// Create the new card with all the values acquired above
						Card c = new Card(name,
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
										  rulings,
										  legality,
										  imageName);
						
						// Add to map of faces if the card has multiple faces
						if (layout.equals("split") || layout.equals("flip") || layout.equals("double-faced"))
						{
							List<String> names = new ArrayList<String>();
							for (JsonElement e: card.get("names").getAsJsonArray())
								names.add(e.getAsString());
							faces.put(c, names);
						}
						
						cards.add(c);
						setProgress(cards.size()*100/numCards);
					}
				}
				
				List<Card> facesList = new ArrayList<Card>(faces.keySet());
				while (!facesList.isEmpty())
				{
					Card face = facesList.remove(0);
					List<String> faceNames = faces.get(face);
					List<Card> otherFaces = new ArrayList<Card>();
					for (Card c: facesList)
					{
						if (faceNames.contains(c.name()) && c.expansion().equals(face.expansion()))
							otherFaces.add(c);
					}
					facesList.removeAll(otherFaces);
					otherFaces.add(face);
					cards.removeAll(otherFaces);
					otherFaces.sort((a, b) -> faceNames.indexOf(a.name()) - faceNames.indexOf(b.name()));
					cards.add(new Card(otherFaces));
				}
				
				// Store the lists of expansion and block names and types and sort them alphabetically
				Expansion.expansions = expansions.stream().sorted().toArray(Expansion[]::new);
				Expansion.blocks = blockNames.stream().sorted().toArray(String[]::new);
				Card.supertypeList = supertypeSet.stream().sorted().toArray(String[]::new);
				Card.typeList = typeSet.stream().sorted().toArray(String[]::new);
				Card.subtypeList = subtypeSet.stream().sorted().toArray(String[]::new);
				Card.formatList = formatSet.stream().sorted().toArray(String[]::new);
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
			setVisible(false);
			dispose();
		}
	}
}
