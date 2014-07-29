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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Indicates more than one possible match found for an option key lookup.
 * <p>
 * Thrown by {@link OptionRegistry#get(String)}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class AmbiguousOptionNameException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final String _lookupName;
	private final IOptionKey<?>[] _keys;
	
	AmbiguousOptionNameException(String lookupName, Collection<IOptionKey<?>> keys)
	{
		_lookupName = lookupName;
		_keys = keys.toArray(new IOptionKey<?>[keys.size()]);
	}
	
	/**
	 * The name that produced the ambiguous results.
	 * <p>
	 * @since 0.07
	 */
	public final String optionName()
	{
		return _lookupName;
	}
	
	/**
	 * The list of keys that all matched the given {@link #optionName()}.
	 * @since 0.07
	 */
	public final List<IOptionKey<?>> ambiguousKeys()
	{
		return Collections.unmodifiableList(Arrays.asList(_keys));
	}
}
