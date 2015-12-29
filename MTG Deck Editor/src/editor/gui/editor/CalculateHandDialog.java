package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import editor.database.Card;
import editor.database.CategorySpec;
import editor.database.Deck;
import editor.database.characteristics.CardCharacteristic;
import editor.filter.FilterGroup;
import editor.filter.FilterType;
import editor.filter.leaf.TextFilter;
import editor.gui.CardTable;
import editor.gui.CardTableModel;
import editor.gui.CategoryList;
import editor.util.Containment;

/**
 * This class represents a dialog that allows a user to specify a set of cards and
 * calculate how likely all of them will appear in an opening hand, including
 * with mulligans.
 * 
 * TODO: Fix this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CalculateHandDialog extends JDialog
{
	/**
	 * Size of the starting hand.
	 */
	private int handSize;
	/**
	 * Deck containing cards to draw from.
	 */
	private Deck deck;
	/**
	 * Model for displaying cards to never draw.
	 */
	private DefaultListModel<Card> excludeModel;
	/**
	 * Spinner specifying the minimum number of cards that should be in
	 * the opening hand (limiting mulligans).
	 */
	private JSpinner minSizeSpinner;
	/**
	 * Label displaying intermediate progress and results.
	 */
	private JLabel resultsLabel;
	/**
	 * Button for performing the calculation.
	 */
	private JButton calculateButton;
	/**
	 * Button for adding a card to the opening hand.
	 */
	private JButton addButton;
	/**
	 * Button for removing a card from the opening hand.
	 */
	private JButton removeButton;
	/**
	 * Button for excluding a card from the opening hand.
	 */
	private JButton excludeButton;
	/**
	 * TODO: Comment this
	 */
	private JProgressBar progressBar;
	/**
	 * TODO: Comment this
	 */
	private CategoryList constraintList;

	/**
	 * Create a new CalculateHandDialog.
	 * 
	 * @param owner Parent frame of this dialog
	 * @param d Deck containing cards to draw from
	 * @param exclusion List of cards to initially exclude from drawing
	 * @param h Starting hand size
	 * @param col Stripe color of the deck table
	 */
	public CalculateHandDialog(Frame owner, Deck d, Collection<Card> exclusion, int h, Color col)
	{
		super(owner, "Calculate Hand Probability", Dialog.ModalityType.APPLICATION_MODAL);
		setBounds(0, 0, 600, 400);
		setLocationRelativeTo(owner);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		handSize = h;
		deck = d;
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setContentPane(contentPane);
		
		// Panel containing the deck table and hand/exclusion lists
		GridBagLayout topLayout = new GridBagLayout();
		topLayout.columnWidths = new int[] {0, 0, 0};
		topLayout.columnWeights = new double[] {0.5, 0.0, 1.0};
		topLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		topLayout.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0};
		JPanel listsPanel = new JPanel(topLayout);
		contentPane.add(listsPanel);
		
		// Panel containing the hand and exclusion lists
		JPanel handPanel = new JPanel();
		handPanel.setLayout(new BoxLayout(handPanel, BoxLayout.Y_AXIS));
		handPanel.setBorder(new TitledBorder("Hand"));
		constraintList = new CategoryList(true);
		JScrollPane handPane = new JScrollPane(constraintList);
		handPane.setAlignmentX(LEFT_ALIGNMENT);
		handPanel.add(handPane);
		JLabel excludeLabel = new JLabel("Exclude:");
		excludeLabel.setAlignmentX(LEFT_ALIGNMENT);
		handPanel.add(Box.createVerticalStrut(3));
		handPanel.add(excludeLabel);
		excludeModel = new DefaultListModel<Card>();
		JList<Card> exclude = new JList<Card>(excludeModel);
		JScrollPane excludePane = new JScrollPane(exclude);
		excludePane.setAlignmentX(LEFT_ALIGNMENT);
		handPanel.add(excludePane);
		GridBagConstraints handConstraints = new GridBagConstraints();
		handConstraints.gridx = 0;
		handConstraints.gridy = 0;
		handConstraints.gridwidth = 1;
		handConstraints.gridheight = 5;
		handConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(handPanel, handConstraints);
		
		// Panel containing buttons for adding and removing cards from the opening hand
		// and for excluding cards
		addButton = new JButton("<");
		GridBagConstraints addConstraints = new GridBagConstraints();
		addConstraints.gridx = 1;
		addConstraints.gridy = 1;
		addConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(addButton, addConstraints);
		removeButton = new JButton(">");
		GridBagConstraints removeConstraints = new GridBagConstraints();
		removeConstraints.gridx = 1;
		removeConstraints.gridy = 2;
		removeConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(removeButton, removeConstraints);
		excludeButton = new JButton("X");
		GridBagConstraints excludeConstraints = new GridBagConstraints();
		excludeConstraints.gridx = 1;
		excludeConstraints.gridy = 3;
		excludeConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(excludeButton, excludeConstraints);
		
		// Panel containing the deck
		JPanel deckPanel = new JPanel(new BorderLayout());
		deckPanel.setBorder(new TitledBorder("Deck"));
		CardTableModel model = new CardTableModel(d, Arrays.asList(CardCharacteristic.NAME, CardCharacteristic.COUNT));
		CardTable deckTable = new CardTable(model);
		deckTable.setStripeColor(col);
		deckPanel.add(new JScrollPane(deckTable), BorderLayout.CENTER);
		GridBagConstraints deckConstraints = new GridBagConstraints();
		deckConstraints.gridx = 2;
		deckConstraints.gridy = 0;
		deckConstraints.gridwidth = 1;
		deckConstraints.gridheight = 5;
		deckConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(deckPanel, deckConstraints);
		
		// Panel containing controls and results
		GridBagLayout bottomLayout = new GridBagLayout();
		bottomLayout.columnWidths = new int[] {0, 0};
		bottomLayout.columnWeights = new double[] {0.0, 1.0};
		bottomLayout.rowHeights = new int[] {0, 0};
		bottomLayout.rowWeights = new double[] {1.0, 0.0};
		JPanel bottomPanel = new JPanel(bottomLayout);
		contentPane.add(bottomPanel);
		
		// Panel containing controls
		GridBagLayout controlsLayout = new GridBagLayout();
		controlsLayout.columnWidths = new int[] {0, 0};
		controlsLayout.columnWeights = new double[] {0.0, 0.0};
		controlsLayout.rowHeights = new int[] {0, 0};
		controlsLayout.rowWeights = new double[] {1.0, 1.0};
		JPanel controlsPanel = new JPanel(controlsLayout);
		controlsPanel.setBorder(new TitledBorder("Settings"));
		GridBagConstraints minLabelConstraints = new GridBagConstraints();
		minLabelConstraints.gridx = 0;
		minLabelConstraints.gridy = 0;
		minLabelConstraints.insets = new Insets(5, 0, 0, 0);
		minLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		controlsPanel.add(new JLabel("Minimum size:", JLabel.RIGHT), minLabelConstraints);
		GridBagConstraints minSpinnerConstraints = new GridBagConstraints();
		minSpinnerConstraints.gridx = 1;
		minSpinnerConstraints.gridy = 0;
		minSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		controlsPanel.add(minSizeSpinner, minSpinnerConstraints);
		GridBagConstraints controlsConstraints = new GridBagConstraints();
		controlsConstraints.gridx = 0;
		controlsConstraints.gridy = 0;
		controlsConstraints.fill = GridBagConstraints.BOTH;
		bottomPanel.add(controlsPanel, controlsConstraints);
		
		// Button panel
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 1;
		buttonConstraints.gridwidth = 2;
		buttonConstraints.gridheight = 1;
		bottomPanel.add(buttonPanel, buttonConstraints);
		
		// Calculate button
		calculateButton = new JButton("Calculate");
		buttonPanel.add(calculateButton);
		
		// Close button
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener((e) -> close());
		buttonPanel.add(closeButton);
		
		// When the calculate button is clicked, it should disable controls and start the calculation
		calculateButton.addActionListener((e) -> {
			List<Category> categories = new ArrayList<Category>();
			for (CategorySpec category: constraintList.getCategories())
			{
				ArrayList<Card> cards = new ArrayList<Card>();
				for (Card c: deck)
					if (category.filter.test(c))
						for (int i = 0; i < deck.count(c); i++)
							cards.add(c);
				categories.add(new Category(category, cards));
			}
			double combinations;
			if (categories.isEmpty())
				combinations = 1.0;
			else
			{
				Collections.sort(categories, (a, b) -> a.cards.size() - b.cards.size());
				combinations = combinations(categories);
			}
			for (int i = categories.size(); i < handSize; i++)
				combinations *= (deck.total() - i);
			double hands = 1.0;
			for (int i = 0; i < handSize; i++)
				hands *= (deck.total() - i);
			resultsLabel.setText(String.format("Probability in opening hand: %.2f%%", combinations/hands*100.0));
		});
		
		// Results panel
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		resultsPanel.setBorder(new TitledBorder("Results"));
		resultsLabel = new JLabel("Probability in opening hand: N/A%");
		resultsLabel.setAlignmentX(LEFT_ALIGNMENT);
		resultsPanel.add(resultsLabel);
//		progressBar = new JProgressBar();
//		progressBar.setVisible(false);
//		progressBar.setAlignmentX(LEFT_ALIGNMENT);
//		resultsPanel.add(progressBar);
		resultsPanel.add(Box.createVerticalGlue());
		GridBagConstraints resultsConstraints = new GridBagConstraints();
		resultsConstraints.gridx = 1;
		resultsConstraints.gridy = 0;
		resultsConstraints.fill = GridBagConstraints.BOTH;
		bottomPanel.add(resultsPanel, resultsConstraints);
		
		// Initial exclusion list
		for (Card c: exclusion)
			excludeModel.addElement(c);
		
		// When the hand or exclusion list is selected, remove the selection from the other one
		constraintList.addListSelectionListener((e) -> {
			if (constraintList.getSelectedIndices().length > 0)
				exclude.clearSelection();
		});
		exclude.addListSelectionListener((e) -> {
			if (exclude.getSelectedIndices().length > 0)
				constraintList.clearSelection();
		});
		
		// Actions for the add, remove, and exclude buttons
		addButton.addActionListener((e) -> {
			for (Card c: Arrays.stream(deckTable.getSelectedRows()).mapToObj((r) -> d.get(deckTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
			{
				TextFilter panel = (TextFilter)FilterType.NAME.createFilter();
				panel.contain = Containment.CONTAINS_EXACTLY;
				panel.text = c.name();
				panel.regex = false;
				FilterGroup group = new FilterGroup();
				group.addChild(panel);
				constraintList.addCategory(new CategorySpec(c.name(), Color.BLACK, group));
			}
		});
		removeButton.addActionListener((e) -> {
			constraintList.removeCategoryAt(constraintList.getSelectedIndex());
			for (Card c: Arrays.stream(exclude.getSelectedIndices()).mapToObj((r) -> excludeModel.getElementAt(r)).collect(Collectors.toList()))
				excludeModel.removeElement(c);
		});
		excludeButton.addActionListener((e) -> {
			for (Card c: Arrays.stream(deckTable.getSelectedRows()).mapToObj((r) -> d.get(deckTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
				if (!excludeModel.contains(c))
					excludeModel.addElement(c);
		});
		
		// When the window goes to close, if a calculation is being performed, a confirmation dialog should display
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				close();
			}
		});
	}
	
	/**
	 * TODO: Comment this
	 * @param categories
	 * @return
	 */
	private double combinations(List<Category> categories)
	{
		if (categories.size() == 0)
			return 0.0;
		else if (categories.size() == 1)
			return categories.get(0).cards.size();
		else
		{
			double combinations = 0.0;
			Category current = categories.remove(0);
			for (Card c: new ArrayList<Card>(current.cards))
			{
				for (Category category: categories)
					category.cards.remove(c);
				combinations += combinations(categories);
				for (Category category: categories)
					if (category.spec.filter.test(c))
						category.cards.add(c);
			}
			categories.add(0, current);
			return combinations;
		}
	}
	
	/**
	 * If there is no job running or if the user wants to cancel the job, close the window.
	 */
	private void close()
	{
		dispose();
	}
	
	/**
	 * TODO: Comment this class
	 * @author Alec Roelke
	 */
	private class Category
	{
		public final CategorySpec spec;
		public final Collection<Card> cards;
		
		public Category(CategorySpec s, Collection<Card> c)
		{
			spec = s;
			cards = c;
		}
	}
}
