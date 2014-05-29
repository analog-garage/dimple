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

import java.util.EventListener;

/**
 * An object that can respond to {@link DimpleEvent}s.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public interface IDimpleEventListener extends EventListener
{
	/**
	 * Indicate whether listener will handle events with given {@code eventClass} type that originate
	 * on given {@code source} object or its children.
	 * <p>
	 * This is intended to be used by {@link IDimpleEventSource} implementations to determine whether to
	 * expend resources to raise events. When this method returns false the listener advertises that it will
	 * ignore matching events.
	 * 
	 * @since 0.06
	 */
	public boolean isListeningFor(Class<? extends DimpleEvent> eventClass, IDimpleEventSource source);
	
	/**
	 * Notify listener of a new event.
	 * <p>
	 * @param event a non-null instance of a subclass of {@link DimpleEvent} that has not yet been consumed.
	 * @return false if event was not already consumed and no handler was found for it.
	 * 
	 * @since 0.06
	 * @see DimpleEvent#consumed()
	 */
	public boolean raiseEvent(DimpleEvent event);
}
