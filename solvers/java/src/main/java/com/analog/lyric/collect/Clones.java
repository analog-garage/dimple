package com.analog.lyric.collect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class Clones
{
	/**
	 * Private constructor prevents subclassing (abstract final not allowed).
	 */
	private Clones() {}

	/**
	 * Clones {@code object} by looking up its {@code clone()} method using reflection.
	 * This implementation does not check to see if object implements {@link Cloneable} interface.
	 */
	public static <T> T cloneReflectively(T object)
		throws CloneNotSupportedException, IllegalAccessException, InvocationTargetException
	{
		Class<?> objectClass = object.getClass();
		Method cloneMethod = null;
		try
		{
			cloneMethod = objectClass.getMethod("clone");
		}
		catch (NoSuchMethodException ex)
		{
			throw new CloneNotSupportedException(objectClass.getName());
		}
		
		if ((cloneMethod.getModifiers() & Modifier.PUBLIC) == 0)
		{
			throw new CloneNotSupportedException(objectClass.getName());
		}
		
		return (T) cloneMethod.invoke(object);
	}
}
