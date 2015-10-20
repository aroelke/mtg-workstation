package editor.gui.filter.editor.text;

import editor.gui.filter.FilterType;

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
		super(FilterType.ARTIST, (c) -> c.artists(), FilterType.ARTIST.code);
	}
}
