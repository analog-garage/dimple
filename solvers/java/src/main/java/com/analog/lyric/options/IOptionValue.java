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

import java.io.Serializable;

/**
 * Base interface for non-primitive option values.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public interface IOptionValue extends Serializable
{
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString();

	/*----------------------
	 * IOptionValue methods
	 */
	
	/**
	 * Indicate whether value instance has mutable state.
	 * <p>
	 * Usually it is best for option values to be immutable, but in some cases it may be necessary or
	 * desirable to have mutable option values. Subclasses that may return true for this method should
	 * explicitly document this fact.
	 * @since 0.08
	 */
	public boolean isMutable();
}
