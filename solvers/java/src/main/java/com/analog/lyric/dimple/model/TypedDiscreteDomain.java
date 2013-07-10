package com.analog.lyric.dimple.model;

public abstract class TypedDiscreteDomain<T> extends DiscreteDomain
{
	protected TypedDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}
	
	@Override
	public abstract T getElement(int i);

	@Override
	public abstract T[] getElements();
}
