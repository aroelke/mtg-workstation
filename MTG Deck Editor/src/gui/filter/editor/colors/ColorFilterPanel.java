package gui.filter.editor.colors;

import gui.filter.ComboBoxPanel;
import gui.filter.FilterType;
import gui.filter.editor.FilterEditorPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
	 * Combo box for choosing which kind of set containment colors should be filtered by.
	 */
	private ComboBoxPanel<Containment> contain;
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
	 * Check box for filtering multicolored cards.
	 */
	private JCheckBox multiCheckBox;
	/**
	 * Characteristic that will be filtered, represented by a Function mapping
	 * Cards onto lists of MTGColors.
	 */
	private Function<Card, MTGColor.Tuple> colorFunction;
	
	/**
	 * Create a new ColorFilterPanel.  The panel will appear as a combo box
	 * enumerating each of the set containment options followed by check boxes
	 * for each of the colors in WUBRG order.
	 * 
	 * @param type FilterType this ColorFilterPanel edits
	 * @param f <code>Predicate<Card, List<MTGColor>></code> representing the characteristic
	 * to filter
	 */
	public ColorFilterPanel(FilterType type, Function<Card, MTGColor.Tuple> f)
	{
		super(type);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		colorFunction = f;
		
		// Set containment combo box
		add(contain = new ComboBoxPanel<Containment>(Containment.values()));
		
		// White check box
		whiteCheckBox = new JCheckBox();
		add(whiteCheckBox);
		JLabel whiteSymbolPanel = new JLabel(ColorSymbol.get(MTGColor.WHITE).getIcon(13));
		add(whiteSymbolPanel);
		
		// Blue check box
		blueCheckBox = new JCheckBox();
		add(blueCheckBox);
		JLabel blueSymbolPanel = new JLabel(ColorSymbol.get(MTGColor.BLUE).getIcon(13));
		add(blueSymbolPanel);
		
		// Black check box
		blackCheckBox = new JCheckBox();
		add(blackCheckBox);
		JLabel blackSymbolPanel = new JLabel(ColorSymbol.get(MTGColor.BLACK).getIcon(13));
		add(blackSymbolPanel);
		
		// Red check box
		redCheckBox = new JCheckBox();
		add(redCheckBox);
		JLabel redSymbolPanel = new JLabel(ColorSymbol.get(MTGColor.RED).getIcon(13));
		add(redSymbolPanel);
		
		// Green check box
		greenCheckBox = new JCheckBox();
		add(greenCheckBox);
		JLabel greenSymbolPanel = new JLabel(ColorSymbol.get(MTGColor.GREEN).getIcon(13));
		add(greenSymbolPanel);
		
		// Multicolored check box
		multiCheckBox = new JCheckBox("Multicolored");
		add(multiCheckBox);
		
		add(Box.createHorizontalGlue());
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if
	 * the Card's color characteristic matches this ColorFilterPanel's set of colors
	 * and set containment choice and <code>false</code> otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		List<MTGColor> colors = new ArrayList<MTGColor>();
		if (whiteCheckBox.isSelected())
			colors.add(MTGColor.WHITE);
		if (blueCheckBox.isSelected())
			colors.add(MTGColor.BLUE);
		if (blackCheckBox.isSelected())
			colors.add(MTGColor.BLACK);
		if (redCheckBox.isSelected())
			colors.add(MTGColor.RED);
		if (greenCheckBox.isSelected())
			colors.add(MTGColor.GREEN);
		boolean multicolored = multiCheckBox.isSelected();
		Predicate<Card> filter = (c) -> contain.getSelectedItem().test(colorFunction.apply(c), colors);
		if (multicolored)
			filter = filter.and((c) -> colorFunction.apply(c).size() > 1);
		return filter;
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
	 * @return A String representation of this ColorFilerPanel's contents, which
	 * is the colors selected (out of WUBRG).
	 */
	@Override
	protected String repr()
	{
		StringJoiner joiner = new StringJoiner("", "\"", "\"");
		if (whiteCheckBox.isSelected())
			joiner.add(String.valueOf(MTGColor.WHITE.shorthand()));
		if (blueCheckBox.isSelected())
			joiner.add(String.valueOf(MTGColor.BLUE.shorthand()));
		if (blackCheckBox.isSelected())
			joiner.add(String.valueOf(MTGColor.BLACK.shorthand()));
		if (redCheckBox.isSelected())
			joiner.add(String.valueOf(MTGColor.RED.shorthand()));
		if (greenCheckBox.isSelected())
			joiner.add(String.valueOf(MTGColor.GREEN.shorthand()));
		if (multiCheckBox.isSelected())
			joiner.add("M");
		return contain.getSelectedItem().toString() + joiner.toString();
	}
	
	/**
	 * Automatically select check boxes based on the colors present in the given
	 * String.
	 * 
	 * @param content String to read content from
	 */
	@Override
	public void setContents(String content)
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
			multiCheckBox.setSelected(false);
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
				case 'M':
					multiCheckBox.setSelected(true);
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