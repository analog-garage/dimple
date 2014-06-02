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

/**
 * Abstract interface for dimple event handlers.
 * <p>
 * Most implementors will simply want to extend {@link DimpleEventHandler}.
 * <p>
 * @since 0.06
 * @author Christopher Barber
 */
public interface IDimpleEventHandler<Event extends DimpleEvent>
{
	/**
	 * Takes an action in response to given {@code event}.
	 * <p>
	 * Multiple event handlers may respond to the same event. Setting {@link DimpleEvent#consumed(boolean)}
	 * to true will prevent any handlers after this one from being notified of the event (the handler is
	 * not expected to check to see if an event has already been consumed).
	 * <p>
	 * The handler does not necessarily have to be able to handle all subtypes of the {@code Event}
	 * type, only those that it was explicitly registered for with a {@link DimpleEventListener}.
	 * <p>
	 * @param event may not be null.
	 * @since 0.06
	 */
	public void handleEvent(Event event);

	/**
	 * True if handler does nothing other than to consume the event.
	 * <p>
	 * Only true for {@link DimpleEventBlocker}.
	 * 
	 * @since 0.06
	 * @see DimpleEventBlocker
	 */
	public boolean isBlocker();
}
