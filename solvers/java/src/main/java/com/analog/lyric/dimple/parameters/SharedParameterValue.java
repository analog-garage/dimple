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

@ThreadSafe
public class SharedParameterValue extends ParameterValue
{
	private static final long serialVersionUID = 1L;

	/*---------------
	 * Construction
	 */
	
	public SharedParameterValue(double value)
	{
		super(value);
	}
	
	public SharedParameterValue()
	{
	}
	
	public SharedParameterValue(SharedParameterValue that)
	{
		super(that);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public SharedParameterValue clone()
	{
		return new SharedParameterValue(this);
	}
	
	@Override
	public SharedParameterValue cloneOrShare()
	{
		return this;
	}
	
	/*-------------------------
	 * ParameterValue methods
	 */
	
	@Override
	public SharedParameterValue asSharedValue()
	{
		return this;
	}
	
	@Override
	public final boolean isShared()
	{
		return true;
	}
	
	@Override
	public SharedParameterValue toShared()
	{
		return this;
	}
	
	@Override
	public ParameterValue toUnshared()
	{
		return new ParameterValue(this.get());
	}
}


