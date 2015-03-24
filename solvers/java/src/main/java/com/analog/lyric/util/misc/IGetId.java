/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.util.misc;

import java.util.UUID;

import com.analog.lyric.dimple.model.core.Ids;

/**
 * Interface for objects that provide identifiers.
 * <p>
 * @see Ids
 */
public interface IGetId
{
	/**
	 * @deprecated use {@link #getGlobalId()} instead
	 */
	@Deprecated
	public long getId();

	/**
	 * A local identifier that uniquely identifies the object within its immediate container.
	 * @since 0.08
	 */
	public int getLocalId();

	/**
	 * A global identifier that uniquely identifies the object within its environment.
	 * @since 0.08
	 */
	public long getGlobalId();

	/**
	 * A "universal" unique identifier for the object.
	 * @since 0.08
	 */
	public UUID getUUID();
	
}
