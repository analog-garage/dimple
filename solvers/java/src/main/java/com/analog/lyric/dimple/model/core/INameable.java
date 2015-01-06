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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.util.misc.IGetId;

public interface INameable extends IGetId
{
	/**
	 * Returns name of object.
	 * <p>
	 * This will be the name set explicitly by {@link #setName(String)}
	 * or else a generated name depending on the type of object.
	 * <p>
	 * @see #getExplicitName()
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
	 * Explicitly set or clear the label.
	 * <p>
	 * Sets the value to be returned by {@link #getLabel()}.
	 * Does not have to be unique.
	 */
	public void setLabel(@Nullable String name) ;
	
	/**
	 * Returns a string suitable for use in GUI labels or plots.
	 * <p>
	 * Unless set explicitly via {@link #setLabel}, this will typically be the same as {@link #getName()}.
	 * Unlike the name, this value is not required to be unique within its parent graph and is not
	 * intended to be used for lookup.
	 */
	public String getLabel();

	/**
	 * As getLabel, but each ancestor name is also
	 * truncated as necessary.
	 */
	public String getQualifiedLabel();

}
