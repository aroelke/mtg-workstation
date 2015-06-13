package gui.filter.text;

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
		super((c) -> c.artist, FilterType.ARTIST.code);
	}
}
