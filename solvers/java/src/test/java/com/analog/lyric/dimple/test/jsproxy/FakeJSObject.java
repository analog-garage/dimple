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

package com.analog.lyric.dimple.test.jsproxy;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Fake implementation of JSObject for testing
 * @since 0.07
 * @author Christopher Barber
 */
@NonNullByDefault(false)
public class FakeJSObject extends JSObject
{
	private final Map<String,Object> _members;
	private final Map<Integer,Object> _slots;
	
	/*--------------
	 * Construction
	 */
	
	FakeJSObject()
	{
		_members = new TreeMap<>();
		_slots = new TreeMap<>();
	}
	
	/*------------------
	 * JSObject methods
	 */
	
	@Override
	public Object call(String functionName, Object[] args) throws JSException
	{
		// HACK - to suppor testing of JSObjectMap.keySet
		if (functionName.startsWith("_getKeys"))
		{
			final int size = _members.size();
			final String[] names = _members.keySet().toArray(new String[size]);
			final JSObject result = new FakeJSObject();
			for (int i = 0; i < size; ++i)
			{
				result.setSlot(i, names[i]);
			}
			return result;
		}
		
		return null;
	}

	@Override
	public Object eval(String arg0) throws JSException
	{
		return null;
	}

	@Override
	public Object getMember(String name) throws JSException
	{
		if (_members.containsKey(name))
		{
			return _members.get(name);
		}
		
		throw new JSException("No such member " + name);
	}

	@Override
	public Object getSlot(int i) throws JSException
	{
		if (_slots.containsKey(i))
		{
			return _slots.get(i);
		}
		
		throw new JSException("No such slot " + i);
	}
	
	@Override
	public void removeMember(String name) throws JSException
	{
		_members.remove(name);
	}

	@Override
	public void setMember(String name, Object value) throws JSException
	{
		_members.put(name, value);
	}

	@Override
	public void setSlot(int i, Object value) throws JSException
	{
		_slots.put(i, value);
	}

	/*----------------------
	 * FakeJSObject methods
	 */
	
	Set<String> getMemberNames()
	{
		return _members.keySet();
	}
}
