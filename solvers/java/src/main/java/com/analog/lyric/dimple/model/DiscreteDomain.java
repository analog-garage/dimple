/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model;

import java.util.Arrays;


public class DiscreteDomain extends Domain 
{
	private Object[] _elements;
	
	public DiscreteDomain(Object ... elements)
	{
		//TODO: should we check for uniqueness?
		_elements = elements;
	}
	
	public final Object[] getElements()
	{
		return _elements;
	}

	@Override
	public final boolean isDiscrete() 
	{
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this._elements);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof DiscreteDomain))
			return false;
		
		DiscreteDomain other = (DiscreteDomain) obj;
		if (!Arrays.equals(this._elements, other._elements))
			return false;
		return true;
	}

	public final int size()
	{
		return _elements.length;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(String.format("DiscreteDomain - %d elements - ", _elements != null ? _elements.length : 0));
		if(_elements != null)
		{
			for (int i = 0; i < _elements.length; i++)
			{
				sb.append(String.format("type: %s value:%s" 
						,_elements[i] != null ? _elements[i].getClass() : "null"
						,_elements[i] != null ? _elements[i].toString() : "null"));
				if (i < _elements.length-1)
					sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public final boolean isJoint()
	{
		return false;
	}
	
	// Find the list of elements corresponding to the value; return -1 if not a valid value
	public int getIndex(Object value)
	{
		int domainLength = _elements.length;
		int index = -1;
		for (int i = 0; i < domainLength; i++)
		{
			Object element = _elements[i];
			
			if (element.equals(value))		// Easy case, objects are the same
			{
				index = i;
				break;
			}
			else if (element instanceof double[] && value instanceof double[])
			{
				if (Arrays.equals((double[])element, (double[])value))
				{
					index = i;
					break;
				}
			}
			else if (element instanceof Object[] && value instanceof Object[])
			{
				if (Arrays.deepEquals((Object[])element, (Object[])value))
				{
					index = i;
					break;
				}
			}

		}
		return index;
	}

}
