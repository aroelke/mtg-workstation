package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import database.characteristics.CardCharacteristic;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog
{
	public SettingsDialog(JFrame owner)
	{
		super(owner, "Preferences", Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		
		JFileChooser inventoryChooser = new JFileChooser();
		inventoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// Tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");
		DefaultMutableTreeNode inventoryNode = new DefaultMutableTreeNode("Inventory");
		root.add(inventoryNode);
		DefaultMutableTreeNode pathsNode = new DefaultMutableTreeNode("Paths");
		inventoryNode.add(pathsNode);
		DefaultMutableTreeNode inventoryAppearanceNode = new DefaultMutableTreeNode("Appearance");
		inventoryNode.add(inventoryAppearanceNode);
		DefaultMutableTreeNode editorNode = new DefaultMutableTreeNode("Editor");
		root.add(editorNode);
		
		// Settings panels
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new CardLayout());
		add(settingsPanel, BorderLayout.CENTER);
		
		// Inventory paths
		JPanel inventoryPanel = new JPanel();
		GridBagLayout pathsLayout = new GridBagLayout();
		pathsLayout.columnWidths = new int[] {0};
		pathsLayout.columnWeights = new double[] {1.0};
		pathsLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		pathsLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0};
		inventoryPanel.setLayout(pathsLayout);
		inventoryPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		settingsPanel.add(inventoryPanel, pathsNode.toString());
		
		// Inventory site
		JPanel inventorySitePanel = new JPanel();
		inventorySitePanel.setLayout(new BoxLayout(inventorySitePanel, BoxLayout.X_AXIS));
		inventorySitePanel.add(new JLabel("Inventory Site:"));
		inventorySitePanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JTextField inventorysiteField = new JTextField(15);
		inventorySitePanel.add(inventorysiteField);
		GridBagConstraints inventorySiteConstraints = new GridBagConstraints();
		inventorySiteConstraints.fill = GridBagConstraints.HORIZONTAL;
		inventorySiteConstraints.anchor = GridBagConstraints.WEST;
		inventorySiteConstraints.gridx = 0;
		inventorySiteConstraints.gridy = 0;
		inventorySiteConstraints.insets = new Insets(0, 0, 5, 0);
		inventoryPanel.add(inventorySitePanel, inventorySiteConstraints);
		
		// Inventory file name
		JPanel inventoryFilePanel = new JPanel();
		inventoryFilePanel.setLayout(new BoxLayout(inventoryFilePanel, BoxLayout.X_AXIS));
		inventoryFilePanel.add(new JLabel("Inventory File:"));
		inventoryFilePanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JTextField inventoryFileField = new JTextField(10);
		inventoryFilePanel.add(inventoryFileField);
		inventoryFilePanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JLabel currentVersionLabel = new JLabel("(Current version: 1.0.0)");
		currentVersionLabel.setFont(new Font(currentVersionLabel.getFont().getFontName(), Font.ITALIC, currentVersionLabel.getFont().getSize()));
		inventoryFilePanel.add(currentVersionLabel);
		GridBagConstraints inventoryFileConstraints = new GridBagConstraints();
		inventoryFileConstraints.fill = GridBagConstraints.HORIZONTAL;
		inventoryFileConstraints.anchor = GridBagConstraints.WEST;
		inventoryFileConstraints.gridx = 0;
		inventoryFileConstraints.gridy = 1;
		inventoryFileConstraints.insets = new Insets(0, 0, 5, 0);
		inventoryPanel.add(inventoryFilePanel, inventoryFileConstraints);
		
		// Inventory file directory
		JPanel inventoryDirPanel = new JPanel();
		inventoryDirPanel.setLayout(new BoxLayout(inventoryDirPanel, BoxLayout.X_AXIS));
		inventoryDirPanel.add(new JLabel("Inventory File Location:"));
		inventoryDirPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JTextField inventoryDirField = new JTextField(25);
		inventoryDirPanel.add(inventoryDirField);
		inventoryDirPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JButton inventoryDirButton = new JButton("\u2026");
		inventoryDirButton.addActionListener((e) -> {
			if (inventoryChooser.showDialog(null, "Select Folder") == JFileChooser.APPROVE_OPTION)
				inventoryDirField.setText(inventoryChooser.getSelectedFile().getPath());
		});
		inventoryDirPanel.add(inventoryDirButton);
		GridBagConstraints inventoryDirConstraints = new GridBagConstraints();
		inventoryDirConstraints.fill = GridBagConstraints.HORIZONTAL;
		inventoryDirConstraints.anchor = GridBagConstraints.WEST;
		inventoryDirConstraints.gridx = 0;
		inventoryDirConstraints.gridy = 2;
		inventoryPanel.add(inventoryDirPanel, inventoryDirConstraints);
		
		// Check for update on startup
		JPanel updatePanel = new JPanel(new BorderLayout());
		JCheckBox updateCheckBox = new JCheckBox("Check for update on program start");
		updatePanel.add(updateCheckBox, BorderLayout.WEST);
		GridBagConstraints updateConstraints = new GridBagConstraints();
		updateConstraints.anchor = GridBagConstraints.WEST;
		updateConstraints.gridx = 0;
		updateConstraints.gridy = 3;
		inventoryPanel.add(updatePanel, updateConstraints);
		
		// Inventory appearance
		JPanel inventoryAppearancePanel = new JPanel();
		inventoryAppearancePanel.setLayout(new BoxLayout(inventoryAppearancePanel, BoxLayout.Y_AXIS));
		settingsPanel.add(inventoryAppearancePanel, inventoryAppearanceNode.toString());
		
		// Columns
		JPanel inventoryColumnsPanel = new JPanel(new GridLayout(0, 5));
		inventoryColumnsPanel.setBorder(new TitledBorder("Columns"));
		List<JCheckBox> columnCheckBoxes = new ArrayList<JCheckBox>();
		for (CardCharacteristic characteristic: CardCharacteristic.inventoryValues())
		{
			JCheckBox checkBox = new JCheckBox(characteristic.toString());
			columnCheckBoxes.add(checkBox);
			inventoryColumnsPanel.add(checkBox);
		}
		inventoryAppearancePanel.add(inventoryColumnsPanel);
		
		// Stripe color
		JPanel inventoryColorPanel = new JPanel(new BorderLayout());
		inventoryColorPanel.setBorder(new TitledBorder("Stripe Color"));
		JColorChooser inventoryStripeColor = new JColorChooser();
		inventoryColorPanel.add(inventoryStripeColor);
		inventoryAppearancePanel.add(inventoryColorPanel);
		
		// Editor
		JPanel editorPanel = new JPanel();
		GridBagLayout editorLayout = new GridBagLayout();
		editorLayout.columnWidths = new int[] {0};
		editorLayout.columnWeights = new double[] {1.0};
		editorLayout.rowHeights = new int[] {0, 0};
		editorLayout.rowWeights = new double[] {0.0, 1.0};
		editorPanel.setLayout(editorLayout);
		editorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		settingsPanel.add(editorPanel, editorNode.toString());
		
		// Recent count
		JPanel recentPanel = new JPanel();
		recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.X_AXIS));
		recentPanel.add(new JLabel("Recent file count:"));
		recentPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JSpinner recentSpinner = new JSpinner(new SpinnerNumberModel(4, 1, Integer.MAX_VALUE, 1));
		recentPanel.add(recentSpinner);
		GridBagConstraints recentConstraints = new GridBagConstraints();
		recentConstraints.fill = GridBagConstraints.VERTICAL;
		recentConstraints.anchor = GridBagConstraints.WEST;
		recentConstraints.gridx = 0;
		recentConstraints.gridy = 0;
		editorPanel.add(recentPanel, recentConstraints);
		
		// Tree panel
		JPanel treePanel = new JPanel(new BorderLayout());
		JTree tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(null);
		tree.addTreeSelectionListener((e) -> {
			((CardLayout)settingsPanel.getLayout()).show(settingsPanel, e.getPath().getLastPathComponent().toString());
		});
		treePanel.add(tree, BorderLayout.CENTER);
		treePanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.EAST);
		treePanel.setPreferredSize(new Dimension(110, 0));
		add(treePanel, BorderLayout.WEST);
		
		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton applyButton = new JButton("Apply");
		buttonPanel.add(applyButton);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> dispose());
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> dispose());
		buttonPanel.add(cancelButton);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
		bottomPanel.add(buttonPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(owner);
	}
}
