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

import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.events.SolverGraphEvent;

/**
 * Base class for events that are raised from a {@link GibbsSolverGraph}.
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class GibbsSolverGraphEvent extends SolverGraphEvent
{
	/*-----------
	 * Constants
	 */
	
	private static final long serialVersionUID = 1L;
	
	final static int GRAPH_EVENT_MASK   = 0xFFFF7000;
	final static int GRAPH_EVENTS_KNOWN = 0xFFFF1000;
	final static int GRAPH_BURNIN       = 0xFFFF2000;
	final static int GRAPH_SAMPLE_STATS = 0xFFFF4000;

	/*--------------
	 * Construction
	 */
	
	GibbsSolverGraphEvent(GibbsSolverGraph source)
	{
		super(source);
	}

	/*--------------------------
	 * SolverGraphEvent methods
	 */
	
	@Override
	public GibbsSolverGraph getSource()
	{
		return (GibbsSolverGraph) source;
	}

	@Override
	public GibbsSolverGraph getSolverObject()
	{
		return (GibbsSolverGraph) source;
	}
	
	/*-------------------------------
	 * GibbsSolverGraphEvent methods
	 */
	
	static boolean raiseBurnInEvent(GibbsSolverGraph source)
	{
		return (getEventFlags(source) & GRAPH_BURNIN) != 0;
	}
	
	static boolean raiseSampleStatsEvent(GibbsSolverGraph source)
	{
		return (getEventFlags(source) & GRAPH_SAMPLE_STATS) != 0;
	}
	
	private static int getEventFlags(SolverEventSource source)
	{
		int eventFlags = source.getFlagValue(GRAPH_EVENT_MASK);
		
		if ((eventFlags & GRAPH_EVENTS_KNOWN) == 0)
		{
			eventFlags = GRAPH_EVENTS_KNOWN;
			final IDimpleEventListener listener = source.getEventListener();
			if (listener != null)
			{
				if (listener.isListeningFor(GibbsBurnInEvent.class, source))
				{
					eventFlags |= GRAPH_BURNIN;
				}
				if (listener.isListeningFor(GibbsSampleStatisticsEvent.class, source))
				{
					eventFlags |= GRAPH_SAMPLE_STATS;
				}
			}
			
			source.setFlagValue(GRAPH_EVENT_MASK, eventFlags);
		}
		
		return eventFlags;
	}
}