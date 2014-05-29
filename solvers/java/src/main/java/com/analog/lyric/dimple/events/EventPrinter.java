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

import java.io.PrintStream;

import net.jcip.annotations.ThreadSafe;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@ThreadSafe
public class EventPrinter extends DimpleEventHandler<DimpleEvent>
{
	/*-------
	 * State
	 */
	
	private volatile int _verbosity;
	private volatile PrintStream _out;
	
	/*--------------
	 * Construction
	 */
	
	public EventPrinter()
	{
		this(System.err, 0);
	}
	
	public EventPrinter(PrintStream out, int verbosity)
	{
		_out = out;
		_verbosity = verbosity;
	}
	
	/*-----------------------------
	 * IDimpleEventHandler methods
	 */
	
	@Override
	public void handleEvent(DimpleEvent event)
	{
		synchronized(_out)
		{
			event.println(_out, _verbosity);
		}
	}
	
	/*---------------
	 * Local methods
	 */
	
	public int getVerbosity()
	{
		return _verbosity;
	}
	
	public void setVerbosity(int verbosity)
	{
		_verbosity = verbosity;
	}
			
	public PrintStream getPrintStream()
	{
		return _out;
	}
	
	public void setPrintStream(PrintStream out)
	{
		_out = out;
	}
}
