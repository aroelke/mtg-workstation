package database.characteristics;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
public class PowerToughness implements Comparable<PowerToughness>
{
	public final double value;
	public final String expression;
	
	public PowerToughness(double v)
	{
		value = v;
		expression = String.valueOf(v);
	}
	
	public PowerToughness(String e)
	{
		if (e == null || e.isEmpty())
		{
			value = 0.0;
			expression = "";
		}
		else
		{
			expression = e;
			e = e.replaceAll("[*\\s]+","").replaceAll("-$", "");
			value = e.isEmpty() ? 0.0 : Double.valueOf(e);
		}
	}
	
	@Override
	public int compareTo(PowerToughness o)
	{
		return (int)(2.0*(value - o.value));
	}
}
