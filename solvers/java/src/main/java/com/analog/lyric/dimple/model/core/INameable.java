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

package com.analog.lyric.dimple.model.core;

import java.util.UUID;

import com.analog.lyric.util.misc.IGetId;
import org.eclipse.jdt.annotation.Nullable;

public interface INameable extends IGetId
{
	@Override
	public int getId();
	
	public UUID getUUID();
	public void setUUID(UUID newUUID) ;
	
	/**
	 * Returns explicitly set name or, if name not set, UUID as string
	 */
	public String getName();
	
	/**
	 * Returns explicitly set name or, if name not set, null
	 */
	public @Nullable String getExplicitName();
	
	/**
	 * Returns name qualified by all parent graphs, if there are any
	 * Each parent is separated by the '.' character.
	 */
	public String getQualifiedName();
	
	/**
	 * Disallowed values:
	 * <ul>
	 * <li> .' character anywhere in the name.
	 * <li>A name already present in the immediate parent graph
	 * </ul>
	 */
	public void setName(@Nullable String name) ;

	/**
	 * Does not have to be unique. Object cannot be found with this name.
	 */
	public void setLabel(@Nullable String name) ;
	
	/**
	 * Returns explicitly setLabel or, if not set, explicitly set name, or if none set,
	 *	then a some shortened version of the UUID,
	 *	suitable for printing, but not guaranteed to be
	 *	unique
	 */
	public String getLabel();

	/**
	 * As getLabel, but each ancestor name is also
	 * truncated as necessary.
	 */
	public String getQualifiedLabel();

}
