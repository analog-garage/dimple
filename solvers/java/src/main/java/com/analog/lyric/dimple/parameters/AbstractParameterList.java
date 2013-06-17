package com.analog.lyric.dimple.parameters;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.model.DimpleException;

public abstract class AbstractParameterList<Key extends IParameterKey> implements IParameterList<Key>
{
	private static final long serialVersionUID = 1L;

	/*----------------
	 * Object methods
	 */
	
	/**
	 * All concrete subclasses should implement this by implementing a
	 * copy constructor and returning a new instance using that.
	 */
	@Override
	public abstract AbstractParameterList<Key> clone();
	
	/*------------------
	 * Iterable methods
	 */
	
	private class IteratorImpl implements Iterator<ParameterValue<Key>>
	{
		private final AtomicInteger _index = new AtomicInteger(0);
		private final Key[] _keys = getKeys();
		
		@Override
		public boolean hasNext()
		{
			return _index.get() < size();
		}

		@Override
		public ParameterValue<Key> next()
		{
			int i = _index.getAndIncrement();
			return new ParameterValue<Key>(_keys != null ? _keys[i] : null, i, get(i), isFixed(i));
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}
	}
	
	@Override
	public Iterator<ParameterValue<Key>> iterator()
	{
		return new IteratorImpl();
	}
	
	/*------------------------------
	 * IFactorParameterList methods
	 */

	@Override
	public double get(Key key)
	{
		assertHasKeys("get");
		return get(key.ordinal());
	}
	
	@Override
	public double[] getValues()
	{
		double[] values = new double[size()];
		for (int i = values.length; --i>=0; )
		{
			values[i] = get(i);
		}
		return values;
	}
	
	@Override
	public boolean hasKeys()
	{
		return getKeys() != null;
	}
	
	@Override
	public boolean isFixed(Key key)
	{
		assertHasKeys("isFixed");
		return isFixed(key.ordinal());
	}
	
	@Override
	public void set(Key key, double value)
	{
		assertHasKeys("set");
		set(key.ordinal(), value);
	}
	
	@Override
	public void setAll(ParameterValue<Key> ... values)
	{
		for (ParameterValue<Key> value : values)
		{
			Key key = value.key();
			if (key != null)
			{
				set(key, value.value());
			}
			else
			{
				set(value.index(), value.value());
			}
		}
	}
	
	@Override
	public void setAll(double ... values)
	{
		for (int i = 0, end = values.length; i < end; ++i)
		{
			set(i, values[i]);
		}
	}
	
	@Override
	public void setAllToDefault()
	{
		if (hasKeys())
		{
			Key[] keys = getKeys();
			for (int i = 0, end = keys.length; i < end; ++i)
			{
				set(i, keys[i].defaultValue());
			}
		}
	}
	
	@Override
	public void setAllMissing()
	{
		for (int i = 0, end = size(); i < end; ++ i)
		{
			set(i, Double.NaN);
		}
	}
	
	@Override
	public void setFixed(Key key, boolean fixed)
	{
		assertHasKeys("setFixed");
		setFixed(key.ordinal(), fixed);
	}
	
	/*-------------------------
	 * Subclass helper methods
	 */
	
	protected void assertHasKeys(String operation)
	{
		if (!hasKeys())
		{
			throw expectedKeys(operation);
		}
	}
	
	protected UnsupportedOperationException expectedKeys(String operation)
	{
		throw new UnsupportedOperationException(
			String.format("Attempt to invoke '%s' with key on keyless parameter list", operation));
	}
	
	protected void assertNotFixed(int index)
	{
		if (!isFixed(index))
		{
			throw expectedNotFixed(index);
		}
	}
	
	protected DimpleException expectedNotFixed(int index)
	{
		return new DimpleException("Attempt to modify fixed parameter '%s'.", getKeys()[index]);
	}
	
	protected void assertIndexInRange(int index)
	{
		if (index < 0 || index >= size())
		{
			throw indexOutOfRange(index);
		}
	}
	
	protected IndexOutOfBoundsException indexOutOfRange(int index)
	{
		return new IndexOutOfBoundsException(
			String.format("Parameter index '%d' is out of allowed range [0,%d]", index, size() - 1));
	}
	
	/**
	 * Subclasses can override this to respond to changes to option with given index.
	 * This should be called *after* the value has changed.
	 * <p>
	 * Subclasses that implement {@link #set(int, double)} or that implement modification without
	 * call that method, should invoke this after the value has been set.
	 */
	protected void valueChanged(int index)
	{
	}
}
