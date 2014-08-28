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

package com.analog.lyric.dimple.matlabproxy;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventLogger;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.StandardDimpleEvents;
import com.analog.lyric.util.misc.Matlab;

/**
 * Proxy wrapper for {@link DimpleEventLogger}.
 * <p>
 * Only intended for use by MATLAB EventLogger class.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Matlab
public class PDimpleEventLogger extends PObject implements Closeable
{
	/*-------
	 * State
	 */
	
	private final DimpleEventLogger _logger;
	
	/*--------------
	 * Construction
	 */
	
	public PDimpleEventLogger()
	{
		_logger = new DimpleEventLogger();
	}

	/*-------------------
	 * Closeable methods
	 */
	
	@Override
	public void close()
	{
		_logger.close();
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public DimpleEventLogger getDelegate()
	{
		return _logger;
	}
	
	@Override
	public DimpleEventLogger getModelerObject()
	{
		return _logger;
	}
	
	/*----------------------------
	 * PDimpleEventLogger methods
	 */
	
	public @Nullable String filename()
	{
		File file = _logger.file();
		if (file != null)
		{
			return file.getAbsolutePath();
		}
		else if (_logger.out() == System.out)
		{
			return "stdout";
		}
		else if (_logger.out() == System.err)
		{
			return "stderr";
		}
		else
		{
			return null;
		}
	}
	
	public boolean isOpen()
	{
		return _logger.isOpen();
	}
	
	public String[] listStandardEvents()
	{
		final int size = StandardDimpleEvents.INSTANCE.size();
		ArrayList<String> events = new ArrayList<String>(size);
		for (Class<?> eventType : StandardDimpleEvents.INSTANCE)
		{
			events.add(eventType.getSimpleName());
		}
		return events.toArray(new String[size]);
	}
	
	public void log(String eventTypeName, Object obj)
	{
		Class<? extends DimpleEvent> eventType = StandardDimpleEvents.INSTANCE.get(eventTypeName);
		if (eventType == null)
		{
			throw new IllegalArgumentException(String.format("No such event type '%s'", eventTypeName));
		}

		Object delegate = PObject.unwrap(obj);
		
		if (delegate instanceof IDimpleEventSource)
		{
			_logger.log(eventType, (IDimpleEventSource)delegate);
		}
		else if (obj instanceof PNodeVector)
		{
			PNodeVector nodes = (PNodeVector)obj;
			_logger.log(eventType, nodes.getModelerNodes());
		}
		else
		{
			throw new IllegalArgumentException(String.format("Cannot log events on class '%s'", obj.getClass()));
		}
	}
		
	public void unlog(String eventTypeName, Object obj)
	{
		Class<? extends DimpleEvent> eventType = StandardDimpleEvents.INSTANCE.get(eventTypeName);
		if (eventType == null)
		{
			throw new IllegalArgumentException(String.format("No such event type '%s'", eventTypeName));
		}

		Object delegate = PObject.unwrap(obj);
		
		if (delegate instanceof IDimpleEventSource)
		{
			_logger.unlog(eventType, (IDimpleEventSource)delegate);
		}
		else if (obj instanceof PNodeVector)
		{
			PNodeVector nodes = (PNodeVector)obj;
			_logger.unlog(eventType, nodes.getModelerNodes());
		}
	}

	public void open(String workingDir, String filename) throws FileNotFoundException
	{
		if (filename.equals("stdout"))
		{
			_logger.open(System.out);
		}
		else if (filename.equals("stderr"))
		{
			_logger.open(System.err);
		}
		else
		{
			_logger.open(new File(workingDir, filename));
		}
	}
	
	public @Nullable PrintStream out()
	{
		return _logger.out();
	}
	
	public void clear()
	{
		_logger.clear();
	}
	
	public int verbosity()
	{
		return _logger.verbosity();
	}
	
	public void verbosity(int verbosity)
	{
		_logger.verbosity(verbosity);
	}
}
