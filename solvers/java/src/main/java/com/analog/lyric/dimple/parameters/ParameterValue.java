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

package com.analog.lyric.dimple.parameters;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;
import com.google.common.util.concurrent.AtomicDouble;

@ThreadSafe
public class ParameterValue extends AtomicDouble implements Cloneable
{
	/*--------------
	 * Construction
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParameterValue()
	{
		this(Double.NaN);
	}
	
	public ParameterValue(double value)
	{
		super(value);
	}
	
	public ParameterValue(ParameterValue that)
	{
		this(that.get());
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public ParameterValue clone()
	{
		return new ParameterValue(this);
	}
	
	/*------------------------
	 * ParameterValue methods
	 */

	/**
	 * Returns this object or null if it is not a {@link SharedParameterValue}.
	 */
	public @Nullable SharedParameterValue asSharedValue()
	{
		return null;
	}
	
	/**
	 * If this object {@link #isShared()}, it is returned, otherwise an unshared
	 * copy will be returned.
	 */
	public ParameterValue cloneOrShare()
	{
		return clone();
	}

	public final boolean known()
	{
		return ! Double.isNaN(get());
	}

	public boolean isShared()
	{
		return false;
	}

	/**
	 * Returns this object if {@link #isShared()} otherwise returns
	 * a shared copy.
	 */
	public SharedParameterValue toShared()
	{
		return new SharedParameterValue(this.get());
	}

	/**
	 * Returns this object if not {@link #isShared()} otherwise returns
	 * an unshared copy.
	 */
	public ParameterValue toUnshared()
	{
		return this;
	}
}
