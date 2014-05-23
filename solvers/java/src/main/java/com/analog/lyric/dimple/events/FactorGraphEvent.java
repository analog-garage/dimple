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

package com.analog.lyric.dimple.events;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.core.FactorGraph;

/**
 * Base class for model events whose source is a {@link FactorGraph}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public abstract class FactorGraphEvent extends ModelEvent
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	/**
	 * @param source
	 * @since 0.06
	 */
	protected FactorGraphEvent(FactorGraph source)
	{
		super(source);
	}

	/*---------------------
	 * EventObject methods
	 */

	@Override
	public FactorGraph getSource()
	{
		return (FactorGraph)source;
	}

	/*---------------------
	 * DimpleEvent methods
	 */

	@Override
	public FactorGraph getModelObject()
	{
		return (FactorGraph)source;
	}
}
