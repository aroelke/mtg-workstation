package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog
{
	public SettingsDialog(JFrame owner)
	{
		super(owner, "Preferences", Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		
		// Tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");
		DefaultMutableTreeNode pathsNode = new DefaultMutableTreeNode("Paths");
		root.add(pathsNode);
		DefaultMutableTreeNode inventoryNode = new DefaultMutableTreeNode("Inventory");
		root.add(inventoryNode);
		DefaultMutableTreeNode editorNode = new DefaultMutableTreeNode("Editor");
		root.add(editorNode);
		
		// Settings panels
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new CardLayout());
		add(settingsPanel, BorderLayout.CENTER);
		
		// Paths
		JPanel pathsPanel = new JPanel();
		GridBagLayout pathsLayout = new GridBagLayout();
		pathsLayout.columnWidths = new int[] {0};
		pathsLayout.columnWeights = new double[] {1.0};
		pathsLayout.rowHeights = new int[] {0, 0, 0, 0};
		pathsLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0};
		pathsPanel.setLayout(pathsLayout);
		pathsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		settingsPanel.add(pathsPanel, pathsNode.toString());
		
		// Inventory location
		JPanel inventoryLocationPanel = new JPanel();
		inventoryLocationPanel.setLayout(new BoxLayout(inventoryLocationPanel, BoxLayout.X_AXIS));
		inventoryLocationPanel.add(new JLabel("Inventory Site:"));
		inventoryLocationPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JTextField inventoryLocationField = new JTextField(30);
		inventoryLocationPanel.add(inventoryLocationField);
		GridBagConstraints inventoryLocationConstraints = new GridBagConstraints();
		inventoryLocationConstraints.fill = GridBagConstraints.HORIZONTAL;
		inventoryLocationConstraints.anchor = GridBagConstraints.WEST;
		inventoryLocationConstraints.gridx = 0;
		inventoryLocationConstraints.gridy = 0;
		inventoryLocationConstraints.insets = new Insets(0, 0, 5, 0);
		pathsPanel.add(inventoryLocationPanel, inventoryLocationConstraints);
		
		// Inventory file name
		JPanel inventoryFilePanel = new JPanel();
		inventoryFilePanel.setLayout(new BoxLayout(inventoryFilePanel, BoxLayout.X_AXIS));
		inventoryFilePanel.add(new JLabel("Inventory File:"));
		inventoryFilePanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JTextField inventoryFileField = new JTextField(20);
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
		pathsPanel.add(inventoryFilePanel, inventoryFileConstraints);
		
		// Inventory file directory
		JPanel inventoryDirPanel = new JPanel();
		inventoryDirPanel.setLayout(new BoxLayout(inventoryDirPanel, BoxLayout.X_AXIS));
		inventoryDirPanel.add(new JLabel("Inventory File Location:"));
		inventoryDirPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JTextField inventoryDirField = new JTextField(50);
		inventoryDirPanel.add(inventoryDirField);
		inventoryDirPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		JButton inventoryDirButton = new JButton("\u2026");
		inventoryDirPanel.add(inventoryDirButton);
		GridBagConstraints inventoryDirConstraints = new GridBagConstraints();
		inventoryDirConstraints.fill = GridBagConstraints.HORIZONTAL;
		inventoryDirConstraints.anchor = GridBagConstraints.WEST;
		inventoryDirConstraints.gridx = 0;
		inventoryDirConstraints.gridy = 2;
		pathsPanel.add(inventoryDirPanel, inventoryDirConstraints);
		
		// Inventory
		JPanel inventoryPanel = new JPanel();
		inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(inventoryPanel, inventoryNode.toString());
		
		// Editor
		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(editorPanel, editorNode.toString());
		
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
