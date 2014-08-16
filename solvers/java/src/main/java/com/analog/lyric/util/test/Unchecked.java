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

package com.analog.lyric.util.test;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@NonNullByDefault(false)
public class Unchecked
{
	/**
	 * Returns a null value with given static type.
	 * <p>
	 * This can be used to work around static null checks.
	 * <p>
	 * @since 0.06
	 */
	@SuppressWarnings("null")
	public static @NonNull <T> T nullValue(Class<T> type)
	{
		return null;
	}
}
