package com.analog.lyric.options;

import java.io.ObjectStreamException;
import java.lang.reflect.Field;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.DimpleException;

@Immutable
public abstract class OptionKey<T> implements IOptionKey<T>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final Class<?> _declaringClass;
	
	private final String _name;
	
	/*--------------
	 * Construction
	 */
	
	protected OptionKey(Class<?> declaringClass, String name)
	{
		_declaringClass = declaringClass;
		_name = name;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> IOptionKey<T> inClass(Class<?> declaringClass, String name)
	{
		try
		{
			IOptionKey<T> option = null;

			if (declaringClass.isEnum() && IOptionKey.class.isAssignableFrom(declaringClass))
			{
				for (Object obj : declaringClass.getEnumConstants())
				{
					option = (IOptionKey<T>)obj;
					if (name.equals(option.name()))
					{
						break;
					}
					else
					{
						option = null;
					}
				}
			}

			if (option == null)
			{
				Field field = declaringClass.getField(name);
				option = (IOptionKey<T>)field.get(declaringClass);
			}
			
			return option;
		}
		catch (Exception ex)
		{
			throw new DimpleException("Error loading option key '%s' from '%s': %s",
				name, declaringClass, ex.toString());
		}
	}
	
	public static <T> IOptionKey<T> forQualifiedName(String qualifiedName)
	{
		int i = qualifiedName.lastIndexOf('.');
		if (i < 0)
		{
			throw new DimpleException("'%s' is not a qualified option key name", qualifiedName);
		}
		
		String className = qualifiedName.substring(0, i);
		String name = qualifiedName.substring(i + 1);
		
		Class<?> declaringClass;
		try
		{
			declaringClass = Class.forName(className);
		}
		catch (ClassNotFoundException ex)
		{
			throw new DimpleException(ex);
		}
		
		return inClass(declaringClass, name);
	}
	
	/*-----------------------
	 * Serialization methods
	 */
	
	/**
	 * Replaces deserialized instance with canonical declaration in field.
	 * 
	 * @throws ObjectStreamException
	 */
	protected Object readResolve() throws ObjectStreamException
	{
		return getCanonicalInstance();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return name();
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public final Class<?> getDeclaringClass()
	{
		return _declaringClass;
	}
	
	@Override
	public String name()
	{
		return _name;
	}
	
	@Override
	public T lookup(IOptionHolder holder)
	{
		return holder.options().lookup(this);
	}
	
	@Override
	public void set(IOptionHolder holder, T value)
	{
		holder.options().put(this, value);
	}
	
	@Override
	public void unset(IOptionHolder holder)
	{
		holder.options().unset(this);
	}
	
	/*---------------------------
	 * AbstractOptionKey methods
	 */
	
	/**
	 * Returns the canonical instance of this option key, i.e. the one that is declared in
	 * {@link #getDeclaringClass()}.
	 */
	public OptionKey<T> getCanonicalInstance()
	{
		OptionKey<T> canonical = this;
		
		try
		{
			OptionKey<T> option = (OptionKey<T>)_declaringClass.getField(_name).get(null);
			if (_name.equals(option.name()) && option.type() == type())
			{
				canonical = option;
			}
		}
		catch (Exception ex)
		{
		}

		return canonical;
	}
	
	public static String qualifiedName(IOptionKey<?> option)
	{
		return String.format("%s.%s", option.getDeclaringClass().getName(), option.name());
	}
}
