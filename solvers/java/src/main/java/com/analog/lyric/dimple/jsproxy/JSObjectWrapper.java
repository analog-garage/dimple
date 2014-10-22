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

package com.analog.lyric.dimple.jsproxy;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
@NonNullByDefault(false)
public class JSObjectWrapper implements IJSObject
{
	private final JSObject _obj;
	
	public static IJSObject wrap(Object obj)
	{
		if (obj instanceof IJSObject)
		{
			return (IJSObject)obj;
		}
		else if (obj instanceof JSObject)
		{
			return new JSObjectWrapper((JSObject)obj);
		}
		
		return (IJSObject)obj;
	}
	
	public JSObjectWrapper(JSObject obj)
	{
		_obj = obj;
	}
	
	@Override
	public Object call(String functionName, Object[] args) throws JSException
	{
		return _obj.call(functionName, args);
	}

	@Override
	public Object eval(String arg0) throws JSException
	{
		return _obj.eval(arg0);
	}

	@Override
	public Object getMember(String name) throws JSException
	{
		return _obj.getMember(name);
	}

	@Override
	public Object getSlot(int i) throws JSException
	{
		return _obj.getSlot(i);
	}

	@Override
	public void removeMember(String name) throws JSException
	{
		_obj.removeMember(name);
	}

	@Override
	public void setMember(String name, Object value) throws JSException
	{
		_obj.setMember(name, value);
	}

	@Override
	public void setSlot(int i, Object value) throws JSException
	{
		_obj.setSlot(i, value);
	}

}
