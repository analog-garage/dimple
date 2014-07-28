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

package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

/**
 * Option key with type {@link Long}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class LongOptionKey extends OptionKey<Long>
{
	private static final long serialVersionUID = 1L;

	private final long _defaultValue;
	
	public LongOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, 0L);
	}

	public LongOptionKey(Class<?> declaringClass, String name, long defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	@Override
	public Class<Long> type()
	{
		return Long.class;
	}

	@Override
	public Long defaultValue()
	{
		return _defaultValue;
	}

	public final long defaultLongValue()
	{
		return _defaultValue;
	}
}
