package editor.database.card;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
public enum CardLayout
{
	NORMAL("Normal"),
	SPLIT("Split", true),
	FLIP("Flip", true),
	DOUBLE_FACED("Double-faced", true),
	TOKEN("Token"),
	PLANE("Plane"),
	SCHEME("Scheme"),
	PHENOMENON("Phenomenon"),
	LEVELER("Leveler"),
	VANGUARD("Vanguard");
	
	private final String layout;
	public final boolean isMultiFaced;
	
	private CardLayout(String l, boolean m)
	{
		layout = l;
		isMultiFaced = m;
	}
	
	private CardLayout(String l)
	{
		this(l, false);
	}
	
	@Override
	public String toString()
	{
		return layout;
	}
}
