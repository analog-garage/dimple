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

/**
 * Abstract base class for {@link IParameterList} implementation with a single parameter.
 */
@ThreadSafe
public abstract class ParameterList1<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected volatile ParameterValue _parameter0;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterList1()
	{
		this(Double.NaN);
	}
	
	protected ParameterList1(double value)
	{
		super(false);
		_parameter0 = new ParameterValue(value);
	}
	
	protected ParameterList1(SharedParameterValue value)
	{
		super(false);
		_parameter0 = value;
	}
	
	protected ParameterList1(ParameterList1<Key> that)
	{
		super(that);
		_parameter0 = that._parameter0.cloneOrShare();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract ParameterList1<Key> clone();
	
	/*------------------------
	 * IParameterList methods
	 */
	
	@Override
	protected final ParameterValue getParameterValue(int index)
	{
		assertIndexInRange(index);
		return _parameter0;
	}
	
	@Override
	protected final void setParameterValue(int index, ParameterValue value)
	{
		assertIndexInRange(index);
		_parameter0 = value;
	}
	
	@Override
	public final int size()
	{
		return 1;
	}
}
