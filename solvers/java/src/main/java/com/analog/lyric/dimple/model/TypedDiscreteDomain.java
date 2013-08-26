package com.analog.lyric.dimple.model;

import java.util.Iterator;

// REFACTOR: I think this should eventually be folded up into DiscreteDomain.
public abstract class TypedDiscreteDomain<Element> extends DiscreteDomain implements Iterable<Element>
{
	private static final long serialVersionUID = 1L;

	protected TypedDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}
	
	@Override
	public abstract Element getElement(int i);

	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public Iterator<Element> iterator()
	{
		return new Iterator<Element>() {

			private volatile int _next = 0;
			
			@Override
			public boolean hasNext()
			{
				return _next < size();
			}

			@Override
			public Element next()
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
