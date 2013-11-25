/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.values;

public class RealJointValue extends Value
{
	protected double[] _value;
	
	/*--------------
	 * Construction
	 */
	
	public RealJointValue(double[] value)
	{
		_value = value;
	}

	public RealJointValue(RealJointValue that)
	{
		_value = that._value; // should we deep copy?
	}
	
	@Override
	public RealJointValue clone()
	{
		return new RealJointValue(this);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public Object getObject()
	{
		return _value;
	}
	
	@Override
	public void setObject(Object value)
	{
		_value = (double[])value;
	}
	
	public double[] getValue() {return _value;}
	public void setValue(double[] value) {_value = value;}
	
	// Get/set a specific element of the sample
	public double getValue(int index) {return _value[index];}
	public void setValue(int index, double value) {_value[index] = value;}
}
