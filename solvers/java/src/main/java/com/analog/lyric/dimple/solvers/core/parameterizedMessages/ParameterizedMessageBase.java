/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.core.parameterizedMessages;

import java.io.PrintStream;

import com.analog.lyric.util.misc.IPrintable;

/**
 * Base implementation of {@link IParameterizedMessage}
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class ParameterizedMessageBase implements IParameterizedMessage
{
	private static final long serialVersionUID = 1L;

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public final void println(PrintStream out, int verbosity)
	{
		IPrintable.Methods.println(this, out, verbosity);
	}
	
	@Override
	public String toString(int verbosity)
	{
		return IPrintable.Methods.toString(this, verbosity);
	}

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	@Override
	public abstract ParameterizedMessageBase clone();

}
