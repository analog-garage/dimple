package com.analog.lyric.dimple.model;

import java.util.Iterator;

// REFACTOR: I think this should eventually be folded up into DiscreteDomain.
public abstract class TypedDiscreteDomain<T> extends DiscreteDomain implements Iterable<T>
{
	private static final long serialVersionUID = 1L;

	protected TypedDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}
	
	@Override
	public abstract T getElement(int i);

	@Override
	public abstract T[] getElements();
	
	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>() {

			private volatile int _next = 0;
			
			@Override
			public boolean hasNext()
			{
				return _next < size();
			}

			@Override
			public T next()
			{
				return getElement(_next++);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
