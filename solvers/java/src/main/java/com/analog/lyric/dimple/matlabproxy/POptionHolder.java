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

package com.analog.lyric.dimple.matlabproxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.options.DimpleOptionRegistry;
import com.analog.lyric.options.IOption;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.Option;
import com.analog.lyric.options.OptionKey;


/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class POptionHolder extends PObject
{
	public @Nullable DimpleEnvironment getEnvironment()
	{
		return size() > 0 ? getOptionHolder(0).getEnvironment() : null;
	}
	
	public abstract DimpleOptionHolder getOptionHolder(int i);
	
	public abstract int size();

	public void clearOptions()
	{
		for (int i = 0, size = size(); i < size; ++i)
		{
			getOptionHolder(i).clearLocalOptions();
		}
	}
	
	public Object[][] getLocallySetOptions()
	{
		final int size = size();
		final Object[][] options = new Object[size][];
		for (int i = 0; i < size; ++i)
		{
			final DimpleOptionHolder holder = getOptionHolder(i);
			
			ArrayList<IOption<?>> nodeOptions = new ArrayList<>(holder.getLocalOptions());
			
			Collections.sort(nodeOptions, new Comparator<Object>() {
				@NonNullByDefault(false)
				@Override
				public int compare(Object obj1, Object obj2)
				{
					IOption<?> option1 = (IOption<?>)obj1;
					IOption<?> option2 = (IOption<?>)obj2;
					
					String name1 = OptionKey.qualifiedName(option1.key());
					String name2 = OptionKey.qualifiedName(option2.key());
					
					return name1.compareTo(name2);
				}
			});
			
			final int nOptions = nodeOptions.size();
			Object[] keyValues = new Object[nOptions * 2];
			for (int j = 0; j < nOptions; ++j)
			{
				IOption<?> option = nodeOptions.get(j);
				IOptionKey<?> key = option.key();
				keyValues[j * 2] = OptionKey.qualifiedName(key);
				keyValues[j * 2 + 1] = wrapValue(option.externalValue());
			}
			options[i] = keyValues;
		}
		
		return options;
	}

	public Object[] getOption(Object optionKey)
	{
		DimpleEnvironment env = getEnvironment();
		
		if (env == null)
		{
			return ArrayUtil.EMPTY_OBJECT_ARRAY;
		}

		final IOptionKey<?> key = env.optionRegistry().asKey(optionKey);
		final int  size = size();
		final Object[] optionValues = new Object[size];
		for (int i = 0; i < size; ++i)
		{
			DimpleOptionHolder holder = getOptionHolder(i);
			Object value = holder.getOptionOrDefault(key);
			optionValues[i] = wrapValue(Option.create(key, value).externalValue());
		}
		return optionValues;
	}
	
	public void unsetOption(Object optionKey)
	{
		final DimpleEnvironment env = getEnvironment();

		if (env != null)
		{
			final IOptionKey<?> key = env.optionRegistry().asKey(optionKey);
			for (int i = 0, size = size(); i < size; ++i)
			{
				getOptionHolder(i).unsetOption(key);
			}
		}
	}
		
	/**
	 * Sets the same option value on all nodes in vector.
	 * <p>
	 * @param optionKey is String or {@link IOptionKey}.
	 * @param value is the value to be set on all nodes in the vector.
	 * @since 0.07
	 */
	public void setOptionOnAll(Object optionKey, @Nullable Object value)
	{
		final DimpleEnvironment env = getEnvironment();

		if (env != null)
		{
			final IOptionKey<?> key = env.optionRegistry().asKey(optionKey);
			Option<?> option = Option.create(key, unwrapValue(value));
			for (int i = 0, size = size(); i < size; ++i)
			{
				Option.setOptions(getOptionHolder(i), option);
			}
		}
	}
	
	/**
	 * Sets values for a single option across all nodes in the vector.
	 * <p>
	 * @param optionKey is a String or {@link IOptionKey}.
	 * @param values is an array of values of the same size as this vector.
	 * @since 0.07
	 */
	public void setOptionAcrossAll(Object optionKey, Object[] values)
	{
		final DimpleEnvironment env = getEnvironment();

		if (env != null)
		{
			final IOptionKey<?> key = env.optionRegistry().asKey(optionKey);
			for (int i = 0, size = size(); i < size; ++i)
			{
				Option<?> option = Option.create(key, unwrapValue(values[i]));
				Option.setOptions(getOptionHolder(i), option);
			}
		}
	}

	public void setOptionsOnAll(Object[] optionKeys, Object[] values)
	{
		for (int i = 0, n = Math.min(optionKeys.length, values.length); i < n; ++i)
		{
			setOptionOnAll(optionKeys[i], unwrapValue(values[i]));
		}
	}

	public void setOptionsAcrossAll(Object[] options)
	{
		final DimpleEnvironment env = getEnvironment();

		if (env != null)
		{
			final DimpleOptionRegistry registry = env.optionRegistry();
		
			for (int i = 0, size = size(); i <size; ++i)
			{
				final DimpleOptionHolder holder = getOptionHolder(i);
				Object obj = options[i];
				if (obj instanceof Object[][])
				{
					for (Object[] keyValue : (Object[][])obj)
					{
						IOptionKey<?> key = registry.asKey(keyValue[0]);
						Object value = unwrapValue(keyValue[1]);
						Option.setOptions(holder, Option.create(key, value));
					}
				}
				else if (obj instanceof Object[])
				{
					Object[] array = (Object[])obj;
					for (int j = 0, endj = array.length; j < endj; j += 2)
					{
						IOptionKey<?> key = registry.asKey(array[j]);
						Object value = unwrapValue(array[j+1]);
						Option.setOptions(holder, Option.create(key, value));
					}
				}
			}
		}
	}

	private @Nullable Object unwrapValue(@Nullable Object value)
	{
		if (value instanceof PObject)
		{
			return ((PObject)value).getDelegate();
		}
		
		return value;
	}
	
	private @Nullable Object wrapValue(@Nullable Object value)
	{
		if (value instanceof Domain)
		{
			return PHelpers.wrapDomain((Domain)value);
		}
		
		return value;
	}
}
