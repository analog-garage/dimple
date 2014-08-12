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

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.environment.ExtendedLevel;

/**
 * Dimple logger interface for MATLAB.
 * @since 0.07
 * @author Christopher Barber
 */
public enum PLogger
{
	INSTANCE;
	
	public void logError(String message)
	{
		DimpleEnvironment.logError(message);
	}

	public void logWarning(String message)
	{
		DimpleEnvironment.logWarning(message);
	}

	/**
	 * This configures the Dimple logger to log to System.err.
	 * <p>
	 * This closes and removes all underlying handlers and adds
	 * a single {@link ConsoleHandler}.
	 * <p>
	 * @since 0.07
	 */
	public void logToConsole()
	{
		setSingleHandler(new ConsoleHandler());
	}
	
	/**
	 * This configures the Dimple logger to append to the specified file.
	 * <p>
	 * This closes and removes all underlying handlers and adds
	 * a single {@link FileHandler}.
	 * <p>
	 * @param filename
	 * @throws IOException if file cannot be written to.
	 * @since 0.07
	 */
	public void logToFile(String filename) throws IOException
	{
		setSingleHandler(new FileHandler(filename, true));
	}
	
	private void setSingleHandler(Handler handler)
	{
		Logger logger = DimpleEnvironment.active().logger();
		logger.setUseParentHandlers(false);
		Handler[] handlers = logger.getHandlers();
		for (Handler oldHandler : handlers)
		{
			if (oldHandler != handler)
			{
				oldHandler.close();
			}
			logger.removeHandler(oldHandler);
		}
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
	}

	public String getLevel()
	{
		Level level = DimpleEnvironment.active().logger().getLevel();
		return level != null ? level.getName() : "";
	}
	
	public void setLevel(String levelName)
	{
		DimpleEnvironment.active().logger().setLevel(stringToLevel(levelName));
	}
	
	static Level stringToLevel(String levelName)
	{
		switch (levelName.toUpperCase())
		{
		case "ALL":
			return Level.ALL;
		case "INFO":
			return Level.INFO;
		case "WARNING":
			return Level.WARNING;
		case "ERROR":
			return ExtendedLevel.ERROR;
		case "SEVERE":
			return Level.SEVERE;
		case "OFF":
			return Level.OFF;
		default:
			throw new Error(String.format("Log level '%s' not one of ALL, INFO, WARNING, ERROR, OFF", levelName));
		}
	}
}
