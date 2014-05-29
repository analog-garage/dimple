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

package com.analog.lyric.dimple.solvers.gibbs;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.events.SolverVariableEvent;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public abstract class GibbsSolverVariableEvent extends SolverVariableEvent
{
	/*-----------
	 * Constants
	 */
	
	private static final long serialVersionUID = 1L;

	/**
	 * Bits in {@link SolverEventSource#_flags} used for solver variable events.
	 */
	final static int USED_FLAGS = 0x03;
	
	final static int UPDATE_EVENT_MASK = 0x03;
	final static int UPDATE_EVENT_UNKNOWN = 0x00;
	final static int UPDATE_EVENT_NONE = 0x01;
	final static int UPDATE_EVENT_SCORED = 0x02;
	final static int UPDATE_EVENT_SIMPLE = 0x03;
	
	/*--------------
	 * Construction
	 */
	
	GibbsSolverVariableEvent(ISolverVariableGibbs source)
	{
		super(source);
	}
	
	/*---------------------
	 * EventObject methods
	 */
	
	@Override
	public ISolverVariableGibbs getSource()
	{
		return (ISolverVariableGibbs)source;
	}

	/*---------------------
	 * SolverEvent methods
	 */

	@Override
	public ISolverVariableGibbs getSolverObject()
	{
		return (ISolverVariableGibbs)source;
	}
	
	/*-------------------------------
	 * Static package helper methods
	 */
	
	static int getVariableUpdateEventFlags(SolverEventSource source)
	{
		int updateEventFlags = source.getFlagValue(UPDATE_EVENT_MASK);

		if (updateEventFlags == UPDATE_EVENT_UNKNOWN)
		{
			updateEventFlags = UPDATE_EVENT_NONE;
			final IDimpleEventListener listener = source.getEventListener();
			if (listener != null)
			{
				if (listener.isListeningFor(GibbsScoredVariableUpdateEvent.class, source))
				{
					updateEventFlags = UPDATE_EVENT_SCORED;
				}
				else if (listener.isListeningFor(GibbsVariableUpdateEvent.class, source))
				{
					updateEventFlags = UPDATE_EVENT_SIMPLE;
				}
			}
			source.setFlagValue(UPDATE_EVENT_MASK, updateEventFlags);
		}
		
		return updateEventFlags;
	}
	

}
