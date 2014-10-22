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

package com.analog.lyric.dimple.jsproxy;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.Option;

/**
 * Javascript API baseclass for objects on which options can be get and set.
 * <p>
 * This is a wrapper around an underlying {@link DimpleOptionHolder} object.
 * <p>
 * For details of Dimple's option mechanism please consult Dimple User Manual.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class JSOptionHolder<Delegate extends DimpleOptionHolder> extends JSProxyObject<Delegate>
{
	JSOptionHolder(Delegate delegate)
	{
		super(delegate);
	}
	
	/*------------------------
	 * JSOptionHolder methods
	 */

	/**
	 * Unsets all options currently set on this object.
	 * @since 0.07
	 */
	public void clearOptions()
	{
		getDelegate().clearLocalOptions();
	}
	
	/**
	 * Returns the current value of the option looked up through this object.
	 * <p>
	 * @param optionKey is a qualified name of a valid Dimple option key.
	 * @return current value of option as looked up through this object.
	 * @since 0.07
	 * @see #setOption(Object, Object)
	 * @see IOptionHolder#getOptionOrDefault(IOptionKey)
	 */
	public @Nullable Object getOption(Object optionKey)
	{
		final DimpleOptionHolder holder = getDelegate();
		final IOptionKey<?> key = lookupOptionKey(optionKey);
		return wrapValue(Option.create(key, holder.getOptionOrDefault(lookupOptionKey(optionKey))).externalValue());
	}
	
	/**
	 * Returns an array of option key strings matching regular expression.
	 * @param regexp is a valid Java {@linkplain java.util.regex.Pattern regular expression string}
	 * against which option names will be compared. To get all known option keys use ".*".
	 * @since 0.07
	 */
	public Object getOptionKeysMatching(String regexp)
	{
		return getDelegate().getEnvironment().optionRegistry().getAllMatching(regexp).toArray();
	}

	public boolean isOptionSet(Object optionKey)
	{
		final DimpleOptionHolder holder = getDelegate();
		final IOptionKey<?> key = lookupOptionKey(optionKey);
		return holder.getOption(key) != null;
	}
	
	/**
	 * Sets option to specified value on this object.
	 * <p>
	 * @param optionKey is the qualified name of a valid Dimple option key.
	 * @param value is the new value to set, which must be compatible with the specified key.
	 * @since 0.07
	 * @see #unsetOption(Object)
	 * @see IOptionHolder#setOption
	 */
	public void setOption(Object optionKey, @Nullable Object value)
	{
		final DimpleOptionHolder holder = getDelegate();
		final IOptionKey<?> key = lookupOptionKey(optionKey);
		Option.setOptions(holder, Option.create(key, unwrapValue(value)));
	}
	
	/**
	 * Unsets option value on this object.
	 * <p>
	 * @param optionKey is the qualified name of a valid Dimple option key.
	 * @since 0.07
	 * @see #setOption(Object, Object)
	 * @see #clearOptions()
	 * @see IOptionHolder#unsetOption(IOptionKey)
	 */
	public void unsetOption(Object optionKey)
	{
		final DimpleOptionHolder holder = getDelegate();
		final IOptionKey<?> key = lookupOptionKey(optionKey);
		holder.unsetOption(key);
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private IOptionKey<?> lookupOptionKey(Object optionKey)
	{
		if (optionKey instanceof IOptionKey)
		{
			return (IOptionKey<?>)optionKey;
		}
		
		return getDelegate().getEnvironment().optionRegistry().asKey(optionKey);
	}
	
	private @Nullable Object unwrapValue(@Nullable Object value)
	{
		return value;
	}

	private @Nullable Object wrapValue(@Nullable Object value)
	{
		return value;
	}
}
