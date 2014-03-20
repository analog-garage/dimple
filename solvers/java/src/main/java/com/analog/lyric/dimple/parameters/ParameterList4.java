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

public abstract class ParameterList4<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	protected volatile ParameterValue _parameter0;
	protected volatile ParameterValue _parameter1;
	protected volatile ParameterValue _parameter2;
	protected volatile ParameterValue _parameter3;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterList4()
	{
		this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}
	
	protected ParameterList4(double value0, double value1, double value2, double value3)
	{
		super(false);
		_parameter0 = new ParameterValue(value0);
		_parameter1 = new ParameterValue(value1);
		_parameter2 = new ParameterValue(value2);
		_parameter3 = new ParameterValue(value3);
	}
	
	protected ParameterList4(SharedParameterValue value0, SharedParameterValue value1,
		SharedParameterValue value2, SharedParameterValue value3)
	{
		super(false);
		_parameter0 = value0;
		_parameter1 = value1;
		_parameter2 = value2;
		_parameter3 = value3;
	}
	
	protected ParameterList4(ParameterList4<Key> that)
	{
		super(that);
		_parameter0 = that._parameter0.cloneOrShare();
		_parameter1 = that._parameter1.cloneOrShare();
		_parameter2 = that._parameter2.cloneOrShare();
		_parameter3 = that._parameter3.cloneOrShare();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract ParameterList3<Key> clone();
	
	/*------------------------
	 * IParameterList methods
	 */

	@Override
	public ParameterValue getParameterValue(int index)
	{
		switch (index)
		{
		case 0:
			return _parameter0;
		case 1:
			return _parameter1;
		case 2:
			return _parameter2;
		case 3:
			return _parameter3;
		default:
			throw indexOutOfRange(index);
		}
	}
	
	@Override
	public void setParameterValue(int index, ParameterValue value)
	{
		switch (index)
		{
		case 0:
			_parameter0 = value;
			break;
		case 1:
			_parameter1 = value;
			break;
		case 2:
			_parameter2 = value;
			break;
		case 3:
			_parameter3 = value;
			break;
		default:
			throw indexOutOfRange(index);
		}
	}
	
	@Override
	public int size()
	{
		return 4;
	}

}
