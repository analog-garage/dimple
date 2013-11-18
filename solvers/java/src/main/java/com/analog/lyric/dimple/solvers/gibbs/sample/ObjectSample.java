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

package com.analog.lyric.dimple.solvers.gibbs.sample;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;

// REFACTOR: move to general package like model.values and rename (e.g. Value)
public abstract class ObjectSample implements Cloneable
{
	@Override
	public abstract ObjectSample clone();
	
	public abstract Object getObject();
	public abstract void setObject(Object value);
	
	public double getDouble()
	{
		return FactorFunctionUtilities.toDouble(getObject());
	}
	
	public void setDouble(double value)
	{
		setObject(value);
	}

	/*-----------------------
	 * Static helper methods
	 */
	
	public static <T extends ObjectSample> T[] fromObjects(Object[] objs, T[] output)
	{
		for (int i = objs.length; --i>=0;)
		{
			output[i].setObject(objs[i]);
		}
		return output;
	}
		
	public static <T extends ObjectSample> Object[] toObjects(T[] values, Object[] output)
	{
		for (int i = values.length; --i>=0;)
		{
			output[i] = values[i].getObject();
		}
		return output;
	}
	
	public static <T extends ObjectSample> Object[] toObjects(T[] values)
	{
		return toObjects(values, new Object[values.length]);
	}
}
