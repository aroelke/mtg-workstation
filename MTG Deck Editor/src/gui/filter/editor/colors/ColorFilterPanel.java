package gui.filter.editor.colors;

import gui.filter.CardFilter;
import gui.filter.editor.FilterEditorPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import util.Containment;
import database.Card;
import database.characteristics.MTGColor;
import database.symbol.ColorSymbol;

/**
 * This class represents a panel that can filter a color characteristic of a Card.
 * A color filter can filter characteristics that contain any of, all of, none of, only,
 * or not only zero or more of the five colors.  To select colorless, simply leave all
 * of the color boxes unchecked.  All sets of colors are considered to contain colorless
 * (or the set of no colors).
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ColorFilterPanel extends FilterEditorPanel
{
	/**
	 * Symbol to display for filtering white.
	 */
	public static final ColorSymbol WHITE = ColorSymbol.SYMBOLS.get(MTGColor.WHITE);
	/**
	 * Symbol to display for filtering blue.
	 */
	public static final ColorSymbol BLUE = ColorSymbol.SYMBOLS.get(MTGColor.BLUE);
	/**
	 * Symbol to display for filtering black.
	 */
	public static final ColorSymbol BLACK = ColorSymbol.SYMBOLS.get(MTGColor.BLACK);
	/**
	 * Symbol to display for filtering red.
	 */
	public static final ColorSymbol RED = ColorSymbol.SYMBOLS.get(MTGColor.RED);
	/**
	 * Symbol to display for filtering green.
	 */
	public static final ColorSymbol GREEN = ColorSymbol.SYMBOLS.get(MTGColor.GREEN);
	
	/**
	 * Combo box for choosing which kind of set containment colors should be filtered by.
	 */
	private JComboBox<Containment> contain;
	/**
	 * Check box for filtering white.
	 */
	private JCheckBox whiteCheckBox;
	/**
	 * Check box for filtering blue.
	 */
	private JCheckBox blueCheckBox;
	/**
	 * Check box for filtering black.
	 */
	private JCheckBox blackCheckBox;
	/**
	 * Check box for filtering red.
	 */
	private JCheckBox redCheckBox;
	/**
	 * Check box for filtering green.
	 */
	private JCheckBox greenCheckBox;
	/**
	 * Code for determining what type of filter this is from a String.
	 * @see gui.filter.editor.FilterEditorPanel#setContent(String)
	 */
	private String code;
	/**
	 * Characteristic that will be filtered, represented by a Function mapping
	 * Cards onto lists of MTGColors.
	 */
	private Function<Card, List<MTGColor>> colorFunction;
	
	/**
	 * Create a new ColorFilterPanel.  The panel will appear as a combo box
	 * enumerating each of the set containment options followed by check boxes
	 * for each of the colors in WUBRG order.
	 * 
	 * @param f <code>Predicate<Card, List<MTGColor>></code> representing the characteristic
	 * to filter
	 * @param c This filter's code
	 */
	public ColorFilterPanel(Function<Card, List<MTGColor>> f, String c)
	{
		super();
		
		colorFunction = f;
		code = c;
		
		// Use a GridBagLayout to push everything against the left side of the panel
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		layout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		layout.rowWeights = new double[] {1.0};
		setLayout(layout);
		
		// Set containment combo box
		contain = new JComboBox<Containment>();
		contain.setModel(new DefaultComboBoxModel<Containment>(Containment.values()));
		GridBagConstraints equalConstraints = new GridBagConstraints();
		equalConstraints.fill = GridBagConstraints.VERTICAL;
		add(contain, equalConstraints);
		
		// White check box
		whiteCheckBox = new JCheckBox();
		GridBagConstraints wCheckConstraints = new GridBagConstraints();
		wCheckConstraints.fill = GridBagConstraints.VERTICAL;
		add(whiteCheckBox, wCheckConstraints);
		JLabel whiteSymbolPanel = new JLabel(WHITE.getIcon(13));
		GridBagConstraints wIconConstraints = new GridBagConstraints();
		wIconConstraints.fill = GridBagConstraints.VERTICAL;
		add(whiteSymbolPanel, wIconConstraints);
		
		// Blue check box
		blueCheckBox = new JCheckBox();
		GridBagConstraints uCheckConstraints = new GridBagConstraints();
		uCheckConstraints.fill = GridBagConstraints.VERTICAL;
		add(blueCheckBox, uCheckConstraints);
		JLabel blueSymbolPanel = new JLabel(BLUE.getIcon(13));
		GridBagConstraints uIconConstraints = new GridBagConstraints();
		uIconConstraints.fill = GridBagConstraints.VERTICAL;
		add(blueSymbolPanel, uIconConstraints);
		
		// Black check box
		blackCheckBox = new JCheckBox();
		GridBagConstraints bCheckConstraints = new GridBagConstraints();
		bCheckConstraints.fill = GridBagConstraints.VERTICAL;
		add(blackCheckBox, bCheckConstraints);
		JLabel blackSymbolPanel = new JLabel(BLACK.getIcon(13));
		GridBagConstraints bIconConstraints = new GridBagConstraints();
		bIconConstraints.fill = GridBagConstraints.VERTICAL;
		add(blackSymbolPanel, bIconConstraints);
		
		// Red check box
		redCheckBox = new JCheckBox();
		GridBagConstraints rCheckConstraints = new GridBagConstraints();
		rCheckConstraints.fill = GridBagConstraints.VERTICAL;
		add(redCheckBox, rCheckConstraints);
		JLabel redSymbolPanel = new JLabel(RED.getIcon(13));
		GridBagConstraints rIconConstraints = new GridBagConstraints();
		rIconConstraints.fill = GridBagConstraints.VERTICAL;
		add(redSymbolPanel, rIconConstraints);
		
		// Green check box
		greenCheckBox = new JCheckBox();
		GridBagConstraints gCheckConstraints = new GridBagConstraints();
		gCheckConstraints.fill = GridBagConstraints.VERTICAL;
		add(greenCheckBox, gCheckConstraints);
		JLabel greenSymbolPanel = new JLabel(GREEN.getIcon(13));
		GridBagConstraints gIconConstraints = new GridBagConstraints();
		gIconConstraints.fill = GridBagConstraints.VERTICAL;
		add(greenSymbolPanel, gIconConstraints);
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if
	 * the Card's color characteristic matches this ColorFilterPanel's set of colors
	 * and set containment choice and <code>false</code> otherwise.
	 */
	@Override
	public CardFilter getFilter()
	{
		List<MTGColor> colors = new ArrayList<MTGColor>();
		if (whiteCheckBox.isSelected())
			colors.add(WHITE.color());
		if (blueCheckBox.isSelected())
			colors.add(BLUE.color());
		if (blackCheckBox.isSelected())
			colors.add(BLACK.color());
		if (redCheckBox.isSelected())
			colors.add(RED.color());
		if (greenCheckBox.isSelected())
			colors.add(GREEN.color());
		return new CardFilter((c) -> contain.getItemAt(contain.getSelectedIndex()).test(colorFunction.apply(c), colors),
							  toString());
	}

	/**
	 * @return <code>false</code>, because selecting no color check boxes represents a valid
	 * choice (colorless).
	 * @see gui.filter.editor.FilterEditorPanel#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	/**
	 * @return A String representation of this ColorFilterPanel, which appears as its
	 * code followed by {} containing its list of colors.
	 */
	@Override
	public String repr()
	{
		StringJoiner joiner = new StringJoiner("", "\"", "\"");
		if (whiteCheckBox.isSelected())
			joiner.add(String.valueOf(WHITE.color().shorthand()));
		if (blueCheckBox.isSelected())
			joiner.add(String.valueOf(BLUE.color().shorthand()));
		if (blackCheckBox.isSelected())
			joiner.add(String.valueOf(BLACK.color().shorthand()));
		if (redCheckBox.isSelected())
			joiner.add(String.valueOf(RED.color().shorthand()));
		if (greenCheckBox.isSelected())
			joiner.add(String.valueOf(GREEN.color().shorthand()));
		return code + ":" + contain.getSelectedItem().toString() + joiner.toString();
	}
	
	/**
	 * Automatically select check boxes based on the colors present in the given
	 * String.
	 * 
	 * @param content String to read content from
	 */
	@Override
	public void setContent(String content)
	{
		Matcher m = Pattern.compile("^([^\"']+)[\"']").matcher(content);
		if (m.find())
		{
			contain.setSelectedItem(Containment.get(m.group(1)));
			whiteCheckBox.setSelected(false);
			blueCheckBox.setSelected(false);
			blackCheckBox.setSelected(false);
			redCheckBox.setSelected(false);
			greenCheckBox.setSelected(false);
			for (char c: content.substring(m.end(), content.length() - 1).toCharArray())
			{
				switch (c)
				{
				case 'W':
					whiteCheckBox.setSelected(true);
					break;
				case 'U':
					blueCheckBox.setSelected(true);
					break;
				case 'B':
					blackCheckBox.setSelected(true);
					break;
				case 'R':
					redCheckBox.setSelected(true);
					break;
				case 'G':
					greenCheckBox.setSelected(true);
					break;
				default:
					throw new IllegalArgumentException("Illegal color character: " + c + " (don't use {} for colors)");
				}
			}
		}
		else
			throw new IllegalArgumentException("Illegal color string: " + content);
	}
}