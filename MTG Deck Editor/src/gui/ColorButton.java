package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JButton;

/**
 * This class represents a button that displays a color rather than text.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ColorButton extends JButton
{
	/**
	 * Color to display.
	 */
	private Color color;
	/**
	 * The amount of button to display outside the box of color.
	 */
	private int border;
	
	/**
	 * Create a new ColorButton with the given color and border width.
	 * 
	 * @param col Color to display
	 * @param b Border between color box and edge of button
	 */
	public ColorButton(Color col, int b)
	{
		super(" ");
		setColor(col);
		border = b;
	}
	
	/**
	 * Create a new ColorButton with the given color and a border of 5.
	 * 
	 * @param col Color to display
	 */
	public ColorButton(Color col)
	{
		this(col, 5);
	}
	
	/**
	 * Create a new ColorButton with a random color and a border of 5.
	 */
	public ColorButton()
	{
		this(null);
		Random rand = new Random();
		setColor(Color.getHSBColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
	}
	
	/**
	 * @return The color being displayed by this ColorButton.
	 */
	public Color color()
	{
		return color;
	}

	/**
	 * @param col Set the color of this ColorButton.
	 */
	public void setColor(Color col)
	{
		color = col;
		repaint();
	}

	/**
	 * Draw this ColorButton.
	 * 
	 * @param g The Graphics object that will draw the button.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(color());
		g.fillRect(border, border, getWidth() - 2*border, getHeight() - 2*border);
	}
}
