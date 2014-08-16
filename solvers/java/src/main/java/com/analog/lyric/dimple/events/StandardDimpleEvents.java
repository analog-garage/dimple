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

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.analog.lyric.dimple.model.core.BoundaryVariableAddEvent;
import com.analog.lyric.dimple.model.core.BoundaryVariableRemoveEvent;
import com.analog.lyric.dimple.model.core.FactorAddEvent;
import com.analog.lyric.dimple.model.core.FactorRemoveEvent;
import com.analog.lyric.dimple.model.core.SubgraphAddEvent;
import com.analog.lyric.dimple.model.core.SubgraphRemoveEvent;
import com.analog.lyric.dimple.model.core.VariableAddEvent;
import com.analog.lyric.dimple.model.core.VariableRemoveEvent;
import com.analog.lyric.dimple.model.variables.VariableChangeEvent;
import com.analog.lyric.dimple.model.variables.VariableFixedValueChangeEvent;
import com.analog.lyric.dimple.model.variables.VariableInputChangeEvent;
import com.analog.lyric.dimple.solvers.core.FactorToVariableMessageEvent;
import com.analog.lyric.dimple.solvers.core.VariableToFactorMessageEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsScoredVariableUpdateEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverVariableEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsVariableUpdateEvent;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Iterators;


/**
 * Singleton enumeration of standard {@link DimpleEvent} subclasses.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public enum StandardDimpleEvents implements Iterable<Class<? extends DimpleEvent>>
{
	INSTANCE;
	
	/*-------
	 * State
	 */
	
	private final NavigableMap<String,Class<? extends DimpleEvent>> _eventTypes;
	
	@NonNullByDefault(false)
	private static enum Caseless implements Comparator<String>
	{
		COMPARATOR;
		
		@Override
		public int compare(String s1, String s2)
		{
			return s1.compareToIgnoreCase(s2);
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	@SuppressWarnings("unchecked")
	private StandardDimpleEvents()
	{
		_eventTypes = new TreeMap<String,Class<? extends DimpleEvent>>(Caseless.COMPARATOR);
			
		final Class<?>[] eventTypes = new Class<?>[] {
			BoundaryVariableAddEvent.class,
			BoundaryVariableRemoveEvent.class,
			DataEvent.class,
			DimpleEvent.class,
			FactorAddEvent.class,
			FactorGraphEvent.class,
			FactorGraphFactorEvent.class,
			FactorGraphSubgraphEvent.class,
			FactorGraphVariableEvent.class,
			FactorRemoveEvent.class,
			FactorToVariableMessageEvent.class,
			GibbsScoredVariableUpdateEvent.class,
			GibbsSolverVariableEvent.class,
			GibbsVariableUpdateEvent.class,
			ModelEvent.class,
			SolverEvent.class,
			SolverFactorEvent.class,
			SolverVariableEvent.class,
			SubgraphAddEvent.class,
			SubgraphRemoveEvent.class,
			VariableAddEvent.class,
			VariableRemoveEvent.class,
			VariableChangeEvent.class,
			VariableFixedValueChangeEvent.class,
			VariableInputChangeEvent.class,
			VariableToFactorMessageEvent.class
		};
		
		for (Class<?> eventType : eventTypes)
		{
			_eventTypes.put(eventType.getSimpleName(), (Class<? extends DimpleEvent>) eventType);
		}
	}
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public Iterator<Class<? extends DimpleEvent>> iterator()
	{
		return Iterators.unmodifiableIterator(_eventTypes.values().iterator());
	}
	
	/*-------------------------------
	 * Collection compatible methods
	 */
	
	public int size()
	{
		return _eventTypes.size();
	}
	
	public @Nullable Class<? extends DimpleEvent> get(String name)
	{
		return _eventTypes.get(name);
	}
}
