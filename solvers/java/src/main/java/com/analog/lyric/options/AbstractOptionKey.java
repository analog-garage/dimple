package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public abstract class AbstractOptionKey<T> implements IOptionKey<T>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final String _name;
	
	/*--------------
	 * Construction
	 */
	
	protected AbstractOptionKey(String name)
	{
		_name = name;
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
}
