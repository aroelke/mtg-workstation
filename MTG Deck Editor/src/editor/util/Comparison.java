package editor.util;

/**
 * This class represents a logical comparison between two values.
 * 
 * TODO: Comment this class
 * TODO: Change other enums to be like this one if appropriate
 * 
 * @author Alec Roelke
 */
public enum Comparison
{
	EQ('='),
	NE('≠'),
	GE('≥'),
	LE('≤'),
	GT('>'),
	LT('<');
	
	private final char operator;
	
	private Comparison(final char op)
	{
		operator = op;
	}
	
	public static Comparison valueOf(char op)
	{
		switch (op)
		{
		case '=':
			return EQ;
		case '≠':
			return NE;
		case '≥':
			return GE;
		case '≤':
			return LE;
		case '>':
			return GT;
		case '<':
			return LT;
		default:
			throw new IllegalArgumentException("Illegal comparison " + op);
		}
	}
	
	public <T extends Comparable<? super T>> boolean test(T a, T b)
	{
		switch (operator)
		{
		case '=':
			return a.equals(b);
		case '≠':
			return !a.equals(b);
		case '≥':
			return a >= b;
		case '≤':
			return a <= b;
		case '>':
			return a > b;
		case '<':
			return a < b;
		default:
			throw new IllegalArgumentException("Illegal comparison " + operator);
		}
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(operator);
	}
}