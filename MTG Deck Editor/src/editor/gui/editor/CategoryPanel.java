package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.database.characteristics.CardData;
import editor.gui.SettingsDialog;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableModel;
import editor.gui.generic.ColorButton;

/**
 * This class represents a panel that shows information about a category in a deck.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryPanel extends JPanel
{
	/**
	 * Category in the Deck data structure.
	 */
	private Deck deck;
	/**
	 * Name of this category for display purposes.
	 */
	private String name;
	/**
	 * Table to display the contents of the category.
	 */
	protected CardTable table;
	/**
	 * Model to tell the table how to display the contents of the category.
	 */
	private CardTableModel model;
	/**
	 * Label showing the number of cards in the category.
	 */
	private JLabel countLabel;
	/**
	 * Label showing the average CMC of cards in the category.
	 */
	private JLabel avgCMCLabel;
	/**
	 * Button for editing the category.
	 */
	protected JButton editButton;
	/**
	 * Button to remove the category.
	 */
	protected JButton removeButton;
	/**
	 * Button displaying and allowing editing of the category's color.
	 */
	protected ColorButton colorButton;
	/**
	 * Border showing the name of the category.
	 */
	private TitledBorder border;
	/**
	 * Default background color of this panel.
	 */
	private Color background;
	/**
	 * Timer timing flashing of the border of this panel when it is skipped to.
	 */
	private Timer flashTimer;
	/**
	 * Combo box showing the user-defined rank of the category.
	 */
	protected JComboBox<Integer> rankBox;
	
	/**
	 * Create a new CategoryPanel.
	 * 
	 * @param d Deck containing the category to display
	 * @param n Name of the category to display
	 * @param editor EditorFrame containing the new CategoryPanel
	 */
	public CategoryPanel(Deck d, String n, EditorFrame editor)
	{
		super();
		deck = d;
		name = n;
		background = getBackground();
		flashTimer = new FlashTimer();
		
		// Each category is surrounded by a border with a title
		setBorder(border = BorderFactory.createTitledBorder(name));
		
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(0, 0));
		
		// Labels showing category stats
		JPanel statsPanel = new JPanel(new GridLayout(0, 1));
		countLabel = new JLabel("Cards: " + deck.total(name));
		statsPanel.add(countLabel);
		avgCMCLabel = new JLabel("Average CMC: 0");
		statsPanel.add(avgCMCLabel);
		topPanel.add(statsPanel, BorderLayout.WEST);
		
		// Panel containing edit and remove buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rankBox = new JComboBox<Integer>(IntStream.range(0, deck.categories().size()).boxed().toArray(Integer[]::new));
		rankBox.setSelectedIndex(deck.getCategoryRank(name));
		buttonPanel.add(rankBox);
		colorButton = new ColorButton(deck.getCategorySpec(name).getColor());
		buttonPanel.add(colorButton);
		editButton = new JButton("…");
		buttonPanel.add(editButton);
		removeButton = new JButton("−");
		buttonPanel.add(removeButton);
		topPanel.add(buttonPanel, BorderLayout.EAST);
		
		add(topPanel, BorderLayout.NORTH);
		
		// Table showing the cards in the category
		model = new CardTableModel(editor, deck.getCategoryCards(name), SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS));
		table = new CardTable(model)
		{
			@Override
			public Dimension getPreferredScrollableViewportSize()
			{
				Dimension d = getPreferredSize();
				d.height = getRowHeight()*Math.min(SettingsDialog.getAsInt(SettingsDialog.CATEGORY_ROWS), deck.size(name));
				return d;
			}
		};
		table.setAutoCreateRowSorter(true);
		table.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
		for (int i = 0; i < table.getColumnCount(); i++)
			if (model.isCellEditable(0, i))
				table.getColumn(model.getColumnName(i)).setCellEditor(model.getColumnCharacteristic(i).createCellEditor(editor));
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.addMouseWheelListener(new PDMouseWheelListener(tablePane));
		tablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(tablePane, BorderLayout.CENTER);

		update();
	}
	
	/**
	 * @return The name of the category this CategoryPanel is displaying.
	 */
	public String getCategoryName()
	{
		return name;
	}
	
	/**
	 * Change the category this panel should display to a new one.
	 * @param n Name of the new category to display
	 * @throws IllegalArgumentException If the deck does not have a category with that name.
	 */
	public void setCategoryName(String n)
	{
		if (!deck.containsCategory(n))
			throw new IllegalArgumentException("deck does not have a category named " + n);
		name = n;
	}
	
	/**
	 * Update the GUI to reflect changes in a category.
	 */
	public void update()
	{
//		model.fireTableDataChanged();
		countLabel.setText("Cards: " + deck.total(name));
		
		double avgCMC = 0.0;
		int count = 0;
		for (Card card: deck)
		{
			if (deck.getCategorySpec(name).includes(card))
			{
				avgCMC += card.minCmc()*deck.getData(card).count();
				count += deck.getData(card).count();
			}
		}
		if (count > 0)
			avgCMC /= count;
		if (avgCMC == (int)avgCMC)
			avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
		else
			avgCMCLabel.setText("Average CMC: " + String.format("%.2f", avgCMC));
		
		border.setTitle(name);
		table.revalidate();
		table.repaint();
		colorButton.setColor(deck.getCategorySpec(name).getColor());
		colorButton.repaint();
		revalidate();
		repaint();
	}
	
	/**
	 * @return The list of Cards corresponding to the selected rows in the category's table.
	 */
	public List<Card> getSelectedCards()
	{
		return Arrays.stream(table.getSelectedRows())
					 .mapToObj((r) -> deck.getCategoryCards(name)[table.convertRowIndexToModel(r)])
					 .collect(Collectors.toList());
	}
	
	/**
	 * Briefly flash to draw attention to this CategoryPanel.
	 */
	public void flash()
	{
		flashTimer.restart();
	}
	
	/**
	 * Apply settings to this CategoryPanel.
	 * 
	 * @param editor EditorFrame containing this CategoryPanel
	 */
	public void applySettings(EditorFrame editor)
	{
		List<CardData> columns = SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS);
		Color stripe = SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE);
		model.setColumns(columns);
		table.setStripeColor(stripe);
		for (int i = 0; i < table.getColumnCount(); i++)
			if (model.isCellEditable(0, i))
				table.getColumn(model.getColumnName(i)).setCellEditor(model.getColumnCharacteristic(i).createCellEditor(editor));
	}
	
	/**
	 * This class represents a mouse wheel listener that returns mouse wheel control to an outer scroll
	 * pane when this one's scroll pane has reached a limit.
	 * 
	 * It is adapted from a StackOverflow answer to the same problem, which can be found at
	 * @link{http://stackoverflow.com/questions/1377887/jtextpane-prevents-scrolling-in-the-parent-jscrollpane}.
	 * 
	 * @author Nemi
	 * @since November 24, 2009
	 */
	private class PDMouseWheelListener implements MouseWheelListener
	{
		private JScrollBar bar;
		private int previousValue = 0;
		private JScrollPane parentScrollPane;
		private JScrollPane parent;

		private JScrollPane getParentScrollPane()
		{
			if (parentScrollPane == null)
			{
				Component parent = getParent();
				while (!(parent instanceof JScrollPane) && parent != null)
					parent = parent.getParent();
				parentScrollPane = (JScrollPane)parent;
			}
			return parentScrollPane;
		}

		public PDMouseWheelListener(JScrollPane p)
		{
			parent = p;
			bar = parent.getVerticalScrollBar();
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			JScrollPane parent = getParentScrollPane();
			if (parent != null)
			{
				/*
				 * Only dispatch if we have reached top/bottom on previous scroll
				 */
				 if (e.getWheelRotation() < 0)
				 {
					 if (bar.getValue() == 0 && previousValue == 0)
						 parent.dispatchEvent(cloneEvent(e));
				 }
				 else
				 {
					 if (bar.getValue() == getMax() && previousValue == getMax())
						 parent.dispatchEvent(cloneEvent(e));
				 }
				 previousValue = bar.getValue();
			}
			/* 
			 * If parent scrollpane doesn't exist, remove this as a listener.
			 * We have to defer this till now (vs doing it in constructor) 
			 * because in the constructor this item has no parent yet.
			 */
			else
				this.parent.removeMouseWheelListener(this);
		}
		
		private int getMax()
		{
			return bar.getMaximum() - bar.getVisibleAmount();
		}
		
		private MouseWheelEvent cloneEvent(MouseWheelEvent e)
		{
			return new MouseWheelEvent(getParentScrollPane(), e.getID(), e
					.getWhen(), e.getModifiers(), 1, 1, e
					.getClickCount(), false, e.getScrollType(), e
					.getScrollAmount(), e.getWheelRotation());
		}
	}
	
	/**
	 * This class represents a timer controlling the flash of the panel when it is skipped to.
	 * The flash will last 400ms.
	 * 
	 * @author Alec Roelke
	 */
	private class FlashTimer extends Timer
	{
		/**
		 * Amount of ticks until the flash disappears.
		 */
		private final int END = 20;
		/**
		 * Color of the flash.
		 */
		private final Color FLASH = SystemColor.textHighlight;
		
		/**
		 * Progress of the flash.
		 */
		private int count;
		
		/**
		 * Create a new FlashTimer.
		 */
		public FlashTimer()
		{
			super(20, null);
			count = 0;
			addActionListener((e) -> {
				if (++count > END)
					stop();
				else
				{
					double ratio = (double)count/(double)END;
					int r = (int)(FLASH.getRed() + (background.getRed() - FLASH.getRed())*ratio);
					int g = (int)(FLASH.getGreen() + (background.getGreen() - FLASH.getGreen())*ratio);
					int b = (int)(FLASH.getBlue() + (background.getBlue() - FLASH.getBlue())*ratio);
					setBackground(new Color(r, g, b));
					repaint();
				}
			});
		}
		
		/**
		 * Stop the timer and set the panel to its default background color.
		 */
		@Override
		public void stop()
		{
			super.stop();
			setBackground(background);
			repaint();
		}
		
		/**
		 * Restart the timer, resetting the progress of the flash.
		 */
		@Override
		public void restart()
		{
			count = 0;
			super.restart();
		}
	}
}
