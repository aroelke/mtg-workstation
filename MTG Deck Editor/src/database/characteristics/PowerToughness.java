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
			value = Double.NaN;
			expression = "";
		}
		else
		{
			expression = e.replaceAll("\\s+", "");
			e = e.replaceAll("[^0-9+-.]+","").replaceAll("[+-]$", "");
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
		if (Double.isNaN(value) && !Double.isNaN(o.value))
			return 1;
		else if (!Double.isNaN(value) && Double.isNaN(o.value))
			return -1;
		else if (Double.isNaN(value) && Double.isNaN(o.value))
			return 0;
		else
			return (int)(2.0*value - 2.0*o.value);
	}
	
	@Override
	public String toString()
	{
		return expression;
	}
}
