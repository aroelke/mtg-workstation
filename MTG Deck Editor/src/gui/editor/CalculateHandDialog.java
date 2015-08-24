package gui.editor;

import gui.CardTable;
import gui.CardTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import database.Card;
import database.Deck;
import database.Hand;
import database.characteristics.CardCharacteristic;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CalculateHandDialog extends JDialog
{
	public CalculateHandDialog(Frame owner, Deck d, Collection<Card> exclusion, int handSize, Color col)
	{
		super(owner, "Calculate Hand Probability", Dialog.ModalityType.APPLICATION_MODAL);
		setBounds(0, 0, 600, 400);
		setLocationRelativeTo(owner);
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setContentPane(contentPane);
		
		GridBagLayout topLayout = new GridBagLayout();
		topLayout.columnWidths = new int[] {0, 0, 0};
		topLayout.columnWeights = new double[] {0.5, 0.0, 1.0};
		topLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		topLayout.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0};
		JPanel listsPanel = new JPanel(topLayout);
		contentPane.add(listsPanel);
		
		JPanel handPanel = new JPanel();
		handPanel.setLayout(new BoxLayout(handPanel, BoxLayout.Y_AXIS));
		handPanel.setBorder(new TitledBorder("Hand"));
		DefaultListModel<Card> handModel = new DefaultListModel<Card>();
		JList<Card> hand = new JList<Card>(handModel);
		JScrollPane handPane = new JScrollPane(hand);
		handPane.setAlignmentX(LEFT_ALIGNMENT);
		handPanel.add(handPane);
		JLabel excludeLabel = new JLabel("Exclude:");
		excludeLabel.setAlignmentX(LEFT_ALIGNMENT);
		handPanel.add(Box.createVerticalStrut(3));
		handPanel.add(excludeLabel);
		DefaultListModel<Card> excludeModel = new DefaultListModel<Card>();
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
		
		JButton addButton = new JButton("<");
		GridBagConstraints addConstraints = new GridBagConstraints();
		addConstraints.gridx = 1;
		addConstraints.gridy = 1;
		addConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(addButton, addConstraints);
		JButton removeButton = new JButton(">");
		GridBagConstraints removeConstraints = new GridBagConstraints();
		removeConstraints.gridx = 1;
		removeConstraints.gridy = 2;
		removeConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(removeButton, removeConstraints);
		JButton excludeButton = new JButton("X");
		GridBagConstraints excludeConstraints = new GridBagConstraints();
		excludeConstraints.gridx = 1;
		excludeConstraints.gridy = 3;
		excludeConstraints.fill = GridBagConstraints.BOTH;
		listsPanel.add(excludeButton, excludeConstraints);
		
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
		
		GridBagLayout bottomLayout = new GridBagLayout();
		bottomLayout.columnWidths = new int[] {0, 0};
		bottomLayout.columnWeights = new double[] {0.0, 1.0};
		bottomLayout.rowHeights = new int[] {0, 0};
		bottomLayout.rowWeights = new double[] {1.0, 0.0};
		JPanel bottomPanel = new JPanel(bottomLayout);
		contentPane.add(bottomPanel);
		
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
		JSpinner minSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		controlsPanel.add(minSizeSpinner, minSpinnerConstraints);
		GridBagConstraints iterLabelConstraints = new GridBagConstraints();
		iterLabelConstraints.gridx = 0;
		iterLabelConstraints.gridy = 1;
		iterLabelConstraints.insets = new Insets(5, 0, 0, 0);
		iterLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		controlsPanel.add(new JLabel("Iterations:", JLabel.RIGHT), iterLabelConstraints);
		GridBagConstraints iterSpinnerConstraints = new GridBagConstraints();
		iterSpinnerConstraints.gridx = 1;
		iterSpinnerConstraints.gridy = 1;
		JSpinner iterationsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, Integer.MAX_VALUE, 1));
		controlsPanel.add(iterationsSpinner, iterSpinnerConstraints);
		GridBagConstraints controlsConstraints = new GridBagConstraints();
		controlsConstraints.gridx = 0;
		controlsConstraints.gridy = 0;
		controlsConstraints.fill = GridBagConstraints.BOTH;
		bottomPanel.add(controlsPanel, controlsConstraints);
		
		JButton calculateButton = new JButton("Calculate");
		GridBagConstraints calculateConstraints = new GridBagConstraints();
		calculateConstraints.gridx = 0;
		calculateConstraints.gridy = 1;
		bottomPanel.add(calculateButton, calculateConstraints);
		
		JPanel resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.setBorder(new TitledBorder("Results"));
		JLabel resultsLabel = new JLabel("Probability in opening hand: N/A%");
		resultsLabel.setVerticalAlignment(JLabel.TOP);
		resultsLabel.setHorizontalAlignment(JLabel.LEFT);
		resultsPanel.add(resultsLabel);
		GridBagConstraints resultsConstraints = new GridBagConstraints();
		resultsConstraints.gridx = 1;
		resultsConstraints.gridy = 0;
		resultsConstraints.gridwidth = 1;
		resultsConstraints.gridheight = 2;
		resultsConstraints.fill = GridBagConstraints.BOTH;
		bottomPanel.add(resultsPanel, resultsConstraints);
		
		for (Card c: exclusion)
			excludeModel.addElement(c);
		
		hand.addListSelectionListener((e) -> {
			if (hand.getSelectedIndices().length > 0)
				exclude.clearSelection();
		});
		exclude.addListSelectionListener((e) -> {
			if (exclude.getSelectedIndices().length > 0)
				hand.clearSelection();
		});
		
		addButton.addActionListener((e) -> {
			for (Card c: Arrays.stream(deckTable.getSelectedRows()).mapToObj((r) -> d.get(deckTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
			{
				int n = 0;
				for (int i = 0; i < handModel.size(); i++)
					if (handModel.elementAt(i).equals(c))
						n++;
				if (n < d.count(c))
					handModel.addElement(c);
			}
		});
		removeButton.addActionListener((e) -> {
			for (Card c: Arrays.stream(hand.getSelectedIndices()).mapToObj((r) -> handModel.getElementAt(r)).collect(Collectors.toList()))
				handModel.removeElement(c);
			for (Card c: Arrays.stream(exclude.getSelectedIndices()).mapToObj((r) -> excludeModel.getElementAt(r)).collect(Collectors.toList()))
				excludeModel.removeElement(c);
		});
		excludeButton.addActionListener((e) -> {
			for (Card c: Arrays.stream(deckTable.getSelectedRows()).mapToObj((r) -> d.get(deckTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
				if (!excludeModel.contains(c))
					excludeModel.addElement(c);
		});
		
		// TODO: Outsource this to a worker
		calculateButton.addActionListener((e) -> {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			List<Card> need = new ArrayList<Card>();
			for (int i = 0; i < handModel.size(); i++)
				need.add(handModel.get(i));
			Set<Card> excluded = new HashSet<Card>();
			for (int i = 0; i < excludeModel.size(); i++)
				excluded.add(excludeModel.get(i));
			
			Hand cards = new Hand(d, excluded);
			int successes = 0;
			for (int i = 0; i < (Integer)iterationsSpinner.getValue(); i++)
			{
				cards.newHand(handSize);
				do
				{
					List<Card> attempt = new ArrayList<Card>(cards.getHand());
					boolean failed = false;
					for (Card c: need)
					{
						if (!attempt.remove(c))
						{
							failed = true;
							break;
						}
					}
					if (!failed)
					{
						successes++;
						break;
					}
					else
						cards.mulligan();
				} while (cards.size() >= (Integer)minSizeSpinner.getValue());
//				resultsLabel.setText("Finished iteration " + i + ".");
			}
			resultsLabel.setText(String.format("Probability in opening hand: %.2f%%", 100.0*successes/(Integer)iterationsSpinner.getValue()));
			
			setCursor(Cursor.getDefaultCursor());
		});
	}
}
