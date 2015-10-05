/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.options;

import static java.util.Objects.*;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.collect.UnmodifiableReleasableIterator;

/**
 * Iterates over local values for given key in the chain of option delegates.
 * <p>
 * Specifically, this iterator visits every option holder in the list of {@linkplain IOptionHolder#getOptionDelegates()
 * option delegates} starting with an initial option holder, and looks for a
 * {@linkplain IOptionHolder#getLocalOption(IOptionKey) local option setting} for a given key.
 * The last entry is the {@linkplain IOptionKey#defaultValue() default value} of the option {@link #key}
 * for the iterator, so this iterator is guaranteed to always produce at least one value.
 * <p>
 * Note that in normal option lookup implemented by {@link AbstractOptionHolder#getOptionOrDefault}
 * only the first value would be returned. This iterator provides a way to visit all of the values.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class OptionLookupIterator<T extends Serializable> extends UnmodifiableReleasableIterator<T>
{
	/*-------
	 * State
	 */
	
	private @Nullable ReleasableIterator<? extends IOptionHolder> _optionDelegates = null;
	private @Nullable IOptionKey<T> _key = null;
	private @Nullable IOptionHolder _lastSource = null;
	 
	private final static AtomicReference<OptionLookupIterator<?>> reusableInstance = new AtomicReference<>();
	
	/*--------------
	 * Construction
	 */
	
	private OptionLookupIterator()
	{
	}

	private void reset(IOptionHolder optionHolder, IOptionKey<T> key)
	{
		_key = key;
		_optionDelegates = optionHolder.getOptionDelegates();
		_lastSource = null;
	}
	
	/**
	 * Return iterator instance.
	 * <p>
	 * When done with iterator, use {@link #release()} to allow reuse.
	 * <p>
	 * @param optionHolder
	 * @param key
	 * @since 0.08
	 */
	static public <T extends Serializable> OptionLookupIterator<T> create(IOptionHolder optionHolder, IOptionKey<T> key)
	{
		@SuppressWarnings("unchecked")
		OptionLookupIterator<T> iter = (OptionLookupIterator<T>) reusableInstance.getAndSet(null);
		if (iter == null)
		{
			iter = new OptionLookupIterator<>();
		}
		iter.reset(optionHolder, key);
		return iter;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _optionDelegates != null;
	}

	@Override
	public T next()
	{
		T value = null;
		ReleasableIterator<? extends IOptionHolder> delegates = _optionDelegates;
		IOptionKey<T> key = requireNonNull(_key);
		
		outer:
		if (delegates != null)
		{
			while (delegates.hasNext())
			{
				IOptionHolder holder = delegates.next();
				value = holder.getLocalOption(key);
				if (value != null)
				{
					_lastSource = holder;
					break outer;
				}
			}
			
			value = key.defaultValue();
			delegates.release();
			_optionDelegates = null;
			_lastSource = null;
		}
		
		return value;
	}

	/*----------------------------
	 * ReleasableIterator methods
	 */
	
	@Override
	public void release()
	{
		ReleasableIterator<?> delegateIter = _optionDelegates;
		if (delegateIter != null)
		{
			delegateIter.release();
			_optionDelegates = null;
		}
		_key = null;
		reusableInstance.set(this);
	}
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * The key whose value is being iterated over.
	 * @since 0.08
	 */
	public IOptionKey<T> key()
	{
		return requireNonNull(_key);
	}
	
	/**
	 * Returns the option holder that locally contained the last value returned by {@link #next()}
	 * <p>
	 * Returns null if {@link #next()} not yet called, or last value was the default value of the
	 * {@link #key()}.
	 * @since 0.08
	 */
	public @Nullable IOptionHolder lastSource()
	{
		return _lastSource;
	}
}
