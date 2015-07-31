package gui.filter.editor.text;

import gui.filter.FilterType;

/**
 * This class represents a FilterPanel that filters Cards by artist name.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ArtistFilterPanel extends TextFilterPanel
{
	/**
	 * Create a new ArtistFilterPanel.
	 */
	public ArtistFilterPanel()
	{
		super((c) -> c.artists(), FilterType.ARTIST.code);
	}
}
