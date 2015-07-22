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
			expression = e.replaceAll("\\s+", "");
			e = e.replaceAll("[^0-9+-]+","").replaceAll("[+-]$", "");
			value = e.isEmpty() ? 0.0 : Double.valueOf(e);
		}
	}
	
	public boolean variable()
	{
		return expression.contains("*");
	}
	
	@Override
	public int compareTo(PowerToughness o)
	{
		return (int)(2.0*(value - o.value));
	}
	
	@Override
	public String toString()
	{
		return expression;
	}
}
