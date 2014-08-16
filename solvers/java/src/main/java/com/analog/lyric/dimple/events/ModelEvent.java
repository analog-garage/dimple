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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for events involving changes to a Dimple model.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public abstract class ModelEvent extends DimpleEvent
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	protected ModelEvent(IModelEventSource source)
	{
		super(source);
	}
	
	/*---------------------
	 * EventObject methods
	 */
	
	@Override
	public @Nullable IModelEventSource getSource()
	{
		return (IModelEventSource)source;
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */

	@Override
	public @Nullable IModelEventSource getModelObject()
	{
		return (IModelEventSource)source;
	}
}
