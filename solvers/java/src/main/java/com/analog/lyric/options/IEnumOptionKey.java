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

/**
 * An {@link IOptionKey} specialization for option keys that are a member of
 * an enumeration.
 */
public interface IEnumOptionKey<T> extends IOptionKey<T>
{
	/**
	 * The ordinal index of the key in its enumeration. If implemented by an
	 * actual enum type, then this method is automatically provided.
	 * @see Enum#ordinal
	 */
	public abstract int ordinal();
}
