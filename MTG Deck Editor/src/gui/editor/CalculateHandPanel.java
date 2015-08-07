package gui.editor;

import gui.CardTable;
import gui.CardTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import database.Card;
import database.Deck;
import database.characteristics.CardCharacteristic;

@SuppressWarnings("serial")
public class CalculateHandPanel extends JPanel
{
	public CalculateHandPanel(Deck d, int handSize, Color col)
	{
		super(new BorderLayout(0, 10));
		
		JPanel listsPanel = new JPanel();
		listsPanel.setLayout(new BoxLayout(listsPanel, BoxLayout.X_AXIS));
		add(listsPanel, BorderLayout.CENTER);
		
		JPanel handPanel = new JPanel(new BorderLayout());
		handPanel.setBorder(new TitledBorder("Hand"));
		DefaultListModel<Card> handModel = new DefaultListModel<Card>();
		JList<Card> hand = new JList<Card>(handModel);
		handPanel.add(new JScrollPane(hand), BorderLayout.CENTER);
		listsPanel.add(handPanel);
		
		JPanel cardsPanel = new JPanel();
		cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
		cardsPanel.add(Box.createVerticalGlue());
		JButton addButton = new JButton("<");
		cardsPanel.add(addButton);
		JButton removeButton = new JButton(">");
		cardsPanel.add(removeButton);
		cardsPanel.add(Box.createVerticalGlue());
		listsPanel.add(cardsPanel);
		
		JPanel deckPanel = new JPanel(new BorderLayout());
		deckPanel.setBorder(new TitledBorder("Deck"));
		CardTableModel model = new CardTableModel(d, Arrays.asList(CardCharacteristic.NAME, CardCharacteristic.COUNT));
		CardTable deckTable = new CardTable(model);
		deckTable.setStripeColor(col);
		deckPanel.add(new JScrollPane(deckTable), BorderLayout.CENTER);
		listsPanel.add(deckPanel);
		
		GridBagLayout bottomLayout = new GridBagLayout();
		bottomLayout.columnWidths = new int[] {0, 0};
		bottomLayout.columnWeights = new double[] {0.0, 1.0};
		bottomLayout.rowHeights = new int[] {0, 0};
		bottomLayout.rowWeights = new double[] {1.0, 0.0};
		JPanel bottomPanel = new JPanel(bottomLayout);
		add(bottomPanel, BorderLayout.SOUTH);
		
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
		JLabel resultsLabel = new JLabel("Opening hand probability: N/A%");
		resultsPanel.add(resultsLabel);
		GridBagConstraints resultsConstraints = new GridBagConstraints();
		resultsConstraints.gridx = 1;
		resultsConstraints.gridy = 0;
		resultsConstraints.gridwidth = 1;
		resultsConstraints.gridheight = 2;
		resultsConstraints.fill = GridBagConstraints.BOTH;
		bottomPanel.add(resultsPanel, resultsConstraints);
		
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
		});
		
		calculateButton.addActionListener((e) -> {
			
		});
	}
}
