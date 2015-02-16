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

package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.model.variables.Variable;


public class VariableStreamSlice<V extends Variable> implements IVariableStreamSlice<V>
{
	private int _start;
	private VariableStreamBase<V> _stream;
	
	public VariableStreamSlice(int start, VariableStreamBase<V> stream)
	{
		_start = start;
		_stream = stream;
	}
	public VariableStreamSlice<V> copy()
	{
		return new VariableStreamSlice<>(_start, _stream);
	}
	
	@Override
	public VariableStreamBase<V> getStream()
	{
		return _stream;
	}
	@Override
	public V get(int i)
	{
		return getStream().get(_start+i);
	}
	@Override
	public V get(int i,boolean createVar)
	{
		return getStream().get(_start+i,createVar);
	}
}
