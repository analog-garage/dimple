/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.parameters;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.dimple.exceptions.DimpleException;
import org.eclipse.jdt.annotation.Nullable;

@ThreadSafe
public abstract class AbstractParameterList<Key extends IParameterKey> implements IParameterList<Key>
{
	private static final long serialVersionUID = 1L;

	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract AbstractParameterList<Key> clone();
	
	/*------------------
	 * Iterable methods
	 */
	
	private class IteratorImpl implements Iterator<Parameter<Key>>
	{
		private final AtomicInteger _index = new AtomicInteger(0);
		private final @Nullable Key[] _keys = getKeys();
		
		@Override
		public boolean hasNext()
		{
			return _index.get() < size();
		}

		@Override
		public Parameter<Key> next()
		{
			int i = _index.getAndIncrement();
			boolean shared = true;
			SharedParameterValue value = getSharedValue(i);
			if (value == null)
			{
				shared = false;
				value = new SharedParameterValue(get(i));
			}
			final Key[] keys = _keys;
			return new Parameter<Key>(keys != null ? keys[i] : null, i, value, isFixed(i), shared);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}
	}
	
	@Override
	public Iterator<Parameter<Key>> iterator()
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
	public @Nullable SharedParameterValue getSharedValue(Key key)
	{
		assertHasKeys("getSharedValue");
		return getSharedValue(key.ordinal());
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
	public boolean isShared(Key key)
	{
		assertHasKeys("isShared");
		return isShared(key.ordinal());
	}
	
	@Override
	public void set(Key key, double value)
	{
		assertHasKeys("set");
		set(key.ordinal(), value);
	}
	
	@Override
	public void setAll(Iterable<Parameter<Key>> values)
	{
		for (Parameter<Key> value : values)
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
			if (keys != null)
			{
				for (int i = 0, end = keys.length; i < end; ++i)
				{
					if (!isFixed(i))
					{
						set(i, keys[i].defaultValue());
					}
				}
			}
		}
	}
	
	@Override
	public void setAllMissing()
	{
		for (int i = 0, end = size(); i < end; ++ i)
		{
			if (!isFixed(i))
			{
				set(i, Double.NaN);
			}
		}
	}
	
	@Override
	public void setFixed(Key key, boolean fixed)
	{
		assertHasKeys("setFixed");
		setFixed(key.ordinal(), fixed);
	}
	
	@Override
	public void setShared(Key key, boolean shared)
	{
		assertHasKeys("setShared");
		setShared(key.ordinal(), shared);
	}
	
	@Override
	public void setSharedValue(Key key, @Nullable SharedParameterValue value)
	{
		assertHasKeys("setSharedValue");
		setSharedValue(key.ordinal(), value);
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
		if (isFixed(index))
		{
			throw expectedNotFixed(index);
		}
	}
	
	protected DimpleException expectedNotFixed(int index)
	{
		Key[] keys = getKeys();
		return new DimpleException("Attempt to modify fixed parameter '%s'.", keys != null ? keys[index] : index);
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
