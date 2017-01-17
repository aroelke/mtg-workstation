package editor.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * This interface defines some static methods that are useful for collections
 * and aren't part of the standard Java libraries.
 * 
 * @author Alec Roelke
 */
public interface CollectionUtils
{
	/**
	 * Convert an Object that is known to be a list to a list with a specific containing type.
	 * Useful for Swing containers that return Objects.
	 * 
	 * @param obj object to convert
	 * @param clazz class of the type contained in the list
	 * @return the given object converted to a list of the given type
	 */
	static <E> List<E> convertToList(Object obj, Class<E> clazz)
	{
		if (obj instanceof List)
			return ((List<?>)obj).stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
		else
			throw new IllegalArgumentException("expected list, got " + obj.getClass());
	}
	
	/**
	 * Convert an Object that is known to be a set to a set with a specific containing type.
	 * Useful for Swing containers that return Objects.
	 * 
	 * @param obj object to convert
	 * @param clazz class of the type contained in the set
	 * @return the given object converted to a set of the given type
	 */
	static <E> Set<E> convertToSet(Object obj, Class<E> clazz)
	{
		if (obj instanceof Set)
			return ((Set<?>)obj).stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toSet());
		else
			throw new IllegalArgumentException("expected set, got " + obj.getClass());
	}
	
	/**
	 * Join a collection of objects into a String.
	 * 
	 * @param join {@link StringJoiner} used to join String-ed objects
	 * @param objects collection of objects to join
	 * @return a String containing the String-ed versions of the given objects joined using the
	 * given joiner
	 */
	static String join(StringJoiner join, Collection<?> objects)
	{
		for (Object v: objects)
			join.add(String.valueOf(v));
		return join.toString();
	}
}
