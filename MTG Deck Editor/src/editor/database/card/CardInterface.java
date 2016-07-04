package editor.database.card;

import java.awt.datatransfer.DataFlavor;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import editor.database.characteristics.Expansion;
import editor.database.characteristics.Legality;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.PowerToughness;
import editor.database.characteristics.Rarity;
import editor.database.symbol.StaticSymbol;
import editor.database.symbol.Symbol;
import editor.gui.MainFrame;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public interface CardInterface
{
	/**
	 * String representing this Card's name in its text box.
	 */
	public String THIS = "~";
	/**
	 * Separator string between characteristics of a multi-face card.
	 */
	public String FACE_SEPARATOR = "//";
	/**
	 * TODO: Comment this
	 */
	public String TEXT_SEPARATOR = "-----";
	/**
	 * DataFlavor representing cards being transferred.
	 */
	public DataFlavor cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Card[].class.getName() + "\"", "Card Array");

	public String id();
	
	public CardLayout layout();
	
	public int faces();
	
	public String unifiedName();
	
	public List<String> name();
	
	public default List<String> normalizedName()
	{
		return name().stream()
				.map((n) -> Normalizer.normalize(n.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("æ", "ae"))
				.collect(Collectors.toList());
	}
	
	public default List<String> legendName()
	{
		List<String> legendNames = new ArrayList<String>();
		for (String fullName: normalizedName())
		{
			if (!supertypes().contains("Legendary"))
				legendNames.add(fullName);
			else
			{
				int comma = fullName.indexOf(',');
				if (comma > 0)
					legendNames.add(fullName.substring(0, comma).trim());
				else
				{
					int the = fullName.indexOf("the ");
					if (the == 0)
						legendNames.add(fullName);
					else if (the > 0)
						legendNames.add(fullName.substring(0, the).trim());
					else
					{
						int of = fullName.indexOf("of ");
						if (of > 0)
							legendNames.add(fullName.substring(0, of).trim());
						else
							legendNames.add(fullName);
					}
				}
			}
		}
		return legendNames;
	}
	
	public default int compareName(CardInterface other)
	{
		return Collator.getInstance(Locale.US).compare(unifiedName(), other.unifiedName());
	}
	
	public ManaCost.Tuple manaCost();
	
	public List<Double> cmc();
	
	public double minCmc();
	
	public ManaType.Tuple colors();
	
	public ManaType.Tuple colorIdentity();
	
	public List<String> supertypes();
	
	public default boolean supertypeContains(String s)
	{
		if (Pattern.compile("\\s").matcher(s).find())
			throw new IllegalArgumentException("Supertypes don't contain white space");
		for (String supertype: supertypes())
			if (s.equalsIgnoreCase(supertype))
				return true;
		return false;
	}
	
	public List<String> types();
	
	public default boolean typeContains(String s)
	{
		if (Pattern.compile("\\s").matcher(s).find())
			throw new IllegalArgumentException("Types don't contain white space");
		for (String type: types())
			if (s.equalsIgnoreCase(type))
				return true;
		return false;
	}
	
	public List<String> subtypes();
	
	public List<List<String>> allTypes();
	
	public List<String> typeLine();
	
	public default String unifiedTypeLine()
	{
		StringJoiner join = new StringJoiner(" " + FACE_SEPARATOR + " ");
		for (String line: typeLine())
			join.add(line);
		return join.toString();
	}
	
	public Expansion expansion();
	
	public Rarity rarity();
	
	public List<String> oracleText();
	
	public default List<String> normalizedOracle()
	{
		List<String> texts = new ArrayList<String>();
		for (int i = 0; i < faces(); i++)
		{
			String normal = Normalizer.normalize(oracleText().get(i).toLowerCase(), Normalizer.Form.NFD);
			normal = normal.replaceAll("\\p{M}", "").replace("æ", "ae");
			normal = normal.replace(legendName().get(i), CardInterface.THIS).replace(normalizedName().get(i), CardInterface.THIS);
			texts.add(normal);
		}
		return texts;
	}
	
	public List<String> flavorText();
	
	public default List<String> normalizedFlavor()
	{
		return flavorText().stream()
				.map((f) -> Normalizer.normalize(f.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("æ", "ae"))
				.collect(Collectors.toList());
	}
	
	public List<String> artist();
	
	public List<String> number();
	
	public PowerToughness.Tuple power();
	
	public boolean powerVariable();
	
	public PowerToughness.Tuple toughness();
	
	public boolean toughnessVariable();
	
	public Loyalty.Tuple loyalty();
	
	public Map<Date, List<String>> rulings();
	
	public Map<String, Legality> legality();
	
	public default boolean legalIn(String format)
	{
		if (format.equalsIgnoreCase("prismatic") && legalIn("classic") && legality().get(format) != Legality.BANNED)
			return true;
		else if (format.equalsIgnoreCase("classic") || format.equalsIgnoreCase("freeform"))
			return true;
		else if (format.contains("Block"))
		{
			format = format.substring(0, format.indexOf("Block")).trim();
			if (expansion().block.equalsIgnoreCase(format))
				return true;
			else if (format.equalsIgnoreCase("urza") && expansion().block.equalsIgnoreCase("urza's"))
				return true;
			else if (format.equalsIgnoreCase("lorwyn-shadowmoor") && (expansion().block.equalsIgnoreCase("lorwyn") || expansion().block.equalsIgnoreCase("shadowmoor")))
				return true;
			else if (format.equalsIgnoreCase("shards of alara") && expansion().block.equalsIgnoreCase("alara"))
				return true;
			else if (format.equalsIgnoreCase("tarkir") && expansion().block.equalsIgnoreCase("khans of tarkir"))
				return true;
			else
				return false;
		}
		else if (!legality().containsKey(format))
			return false;
		else
			return legality().get(format) != Legality.BANNED;
	}
	
	public default List<String> legalIn()
	{
		return legality().keySet().stream().filter(this::legalIn).collect(Collectors.toList());
	}
	
	public default Legality legalityIn(String format)
	{
		if (legalIn(format))
		{
			if (format.equalsIgnoreCase("prismatic"))
				format = "classic";
			return legality().containsKey(format) ? legality().get(format) : Legality.LEGAL;
		}
		else
			return Legality.BANNED;
	}
	
	public List<String> imageNames();
	
	public default boolean canBeCommander()
	{
		return supertypeContains("legendary") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("can be your commander"));
	}
	
	public default boolean ignoreCountRestriction()
	{
		return supertypeContains("basic") || oracleText().stream().map(String::toLowerCase).anyMatch((s) -> s.contains("a deck can have any number"));
	}
	
	public default void formatDocument(StyledDocument document, int f)
	{
		Style textStyle = document.getStyle("text");
		Style reminderStyle = document.getStyle("reminder");
		Style chaosStyle = document.addStyle("CHAOS", null);
		StyleConstants.setIcon(chaosStyle, StaticSymbol.CHAOS.getIcon(MainFrame.TEXT_SIZE));
		try
		{
			document.insertString(document.getLength(), name().get(f) + " ", textStyle);
			if (!manaCost().get(f).isEmpty())
			{
				for (Symbol symbol: manaCost().get(f))
				{
					Style style = document.addStyle(symbol.toString(), null);
					StyleConstants.setIcon(style, symbol.getIcon(MainFrame.TEXT_SIZE));
					document.insertString(document.getLength(), symbol.toString(), style);
				}
				document.insertString(document.getLength(), " ", textStyle);
			}
			if (cmc().get(f) == cmc().get(f).doubleValue())
				document.insertString(document.getLength(), "(" + (int)cmc().get(f).doubleValue() + ")\n", textStyle);
			else
				document.insertString(document.getLength(), "(" + cmc().get(f) + ")\n", textStyle);
			document.insertString(document.getLength(), typeLine().get(f) + '\n', textStyle);
			document.insertString(document.getLength(), expansion().name + ' ' + rarity() + '\n', textStyle);
			
			String oracle = oracleText().get(f);
			if (!oracle.isEmpty())
			{
				int start = 0;
				Style style = textStyle;
				for (int i = 0; i < oracle.length(); i++)
				{
					switch (oracle.charAt(i))
					{
					case '{':
						document.insertString(document.getLength(), oracle.substring(start, i), style);
						start = i + 1;
						break;
					case '}':
						Symbol symbol = Symbol.valueOf(oracle.substring(start, i));
						Style symbolStyle = document.addStyle(symbol.toString(), null);
						StyleConstants.setIcon(symbolStyle, symbol.getIcon(MainFrame.TEXT_SIZE));
						document.insertString(document.getLength(), symbol.toString(), symbolStyle);
						start = i + 1;
						break;
					case '(':
						document.insertString(document.getLength(), oracle.substring(start, i), style);
						style = reminderStyle;
						start = i;
						break;
					case ')':
						document.insertString(document.getLength(), oracle.substring(start, i + 1), style);
						style = textStyle;
						start = i + 1;
						break;
					case 'C':
						if (oracle.substring(i, i + 5).equals("CHAOS"))
						{
							document.insertString(document.getLength(), oracle.substring(start, i), style);
							document.insertString(document.getLength(), "CHAOS", chaosStyle);
							start = i += 5;
						}
						break;
					default:
						break;
					}
					if (i == oracle.length() - 1 && oracle.charAt(i) != '}' && oracle.charAt(i) != ')')
						document.insertString(document.getLength(), oracle.substring(start, i + 1), style);
				}
				document.insertString(document.getLength(), "\n", textStyle);
			}
			String flavor = flavorText().get(f);
			if (!flavor.isEmpty())
			{
				int start = 0;
				for (int i = 0; i < flavor.length(); i++)
				{
					switch (flavor.charAt(i))
					{
					case '{':
						document.insertString(document.getLength(), flavor.substring(start, i), reminderStyle);
						start = i + 1;
						break;
					case '}':
						Symbol symbol = Symbol.valueOf(flavor.substring(start, i));
						Style symbolStyle = document.addStyle(symbol.toString(), null);
						StyleConstants.setIcon(symbolStyle, symbol.getIcon(MainFrame.TEXT_SIZE));
						document.insertString(document.getLength(), " ", symbolStyle);
						start = i + 1;
						break;
					default:
						break;
					}
					if (i == flavor.length() - 1 && flavor.charAt(i) != '}')
						document.insertString(document.getLength(), flavor.substring(start, i + 1), reminderStyle);
				}
				document.insertString(document.getLength(), "\n", reminderStyle);
			}
			
			if (!Double.isNaN(power().get(f).value) && !Double.isNaN(toughness().get(f).value))
				document.insertString(document.getLength(), power().get(f) + "/" + toughness().get(f) + "\n", textStyle);
			else if (loyalty().get(f).value > 0)
				document.insertString(document.getLength(), loyalty().get(f) + "\n", textStyle);
			
			document.insertString(document.getLength(), artist() + " " + number().get(f) + "/" + expansion().count, textStyle);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}
	
	public default void formatDocument(StyledDocument document)
	{
		Style textStyle = document.getStyle("text");
		try
		{
			for (int f = 0; f < faces(); f++)
			{
				formatDocument(document, f);
				if (f < faces() - 1)
					document.insertString(document.getLength(), "\n" + TEXT_SEPARATOR + "\n", textStyle);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString();
}
