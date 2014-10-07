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
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.Option;

/**
 * 
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

	public void clearOptions()
	{
		getDelegate().clearLocalOptions();
	}
	
	public @Nullable Object getOption(Object optionKey)
	{
		final DimpleOptionHolder holder = getDelegate();
		final IOptionKey<?> key = lookupOptionKey(optionKey);
		return wrapValue(Option.create(key, holder.getOptionOrDefault(lookupOptionKey(optionKey))).externalValue());
	}
	
	public Object getOptionKeysMatching(String regexp)
	{
		return getDelegate().getEnvironment().optionRegistry().getAllMatching(regexp).toArray();
	}

	public void setOption(Object optionKey, @Nullable Object value)
	{
		final DimpleOptionHolder holder = getDelegate();
		final IOptionKey<?> key = lookupOptionKey(optionKey);
		Option.setOptions(holder, Option.create(key, unwrapValue(value)));
	}
	
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
