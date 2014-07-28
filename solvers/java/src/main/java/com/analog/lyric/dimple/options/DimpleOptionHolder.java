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

package com.analog.lyric.dimple.options;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.events.EventSourceIterator;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.options.LocalOptionHolder;

/**
 * Base class for dimple objects that can hold options and generate events.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class DimpleOptionHolder extends LocalOptionHolder implements IDimpleEventSource
{
	/*-----------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public ReleasableIterator<IDimpleEventSource> getOptionDelegates()
	{
		return EventSourceIterator.create(this);
	}

}
