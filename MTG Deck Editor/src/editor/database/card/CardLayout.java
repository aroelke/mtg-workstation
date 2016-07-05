package editor.database.card;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
public enum CardLayout
{
	NORMAL("Normal"),
	SPLIT("Split"),
	FLIP("Flip"),
	DOUBLE_FACED("Double-faced"),
	TOKEN("Token"),
	PLANE("Plane"),
	SCHEME("Scheme"),
	PHENOMENON("Phenomenon"),
	LEVELER("Leveler"),
	VANGUARD("Vanguard");
	
	private final String layout;
	
	private CardLayout(String l)
	{
		layout = l;
	}
	
	@Override
	public String toString()
	{
		return layout;
	}
}
