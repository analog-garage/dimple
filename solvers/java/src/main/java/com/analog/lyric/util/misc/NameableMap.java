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

package com.analog.lyric.util.misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INameable;

public class NameableMap implements Iterable<INameable>
{
	private HashMap<String, INameable> _nameMap;
	private HashMap<UUID, INameable> _UUIDMap;
	private ArrayList<INameable> _list;
	
	public NameableMap()
	{
		this(null);
	}
	public NameableMap(@Nullable Collection<INameable> collection)
	{
		_nameMap = new HashMap<String, INameable>();
		_UUIDMap = new HashMap<UUID, INameable>();
		_list = new ArrayList<INameable>();
		
		if(collection != null)
		{
			for(INameable n : collection)
			{
				add(n);
			}
		}
	}
	
	@Override
	public String toString()
	{
		String s = String.format("NameableMap %d - %d - %d\n"
				,_nameMap.size()
				,_UUIDMap.size()
				,_list.size());
		for(INameable n : _list)
		{
			s += "\t" + n.getQualifiedLabel() + "\n";
		}
		return s;
	}
	
	public void add(INameable n)
	{
		if(_nameMap.containsKey(n.getName()))
		{
			throw new DimpleException("ERROR: name [" + n.getName() +
								"] already in map with type [" +
								_nameMap.get(n.getName()).getClass().toString() +
								"] - tried to add with type [" +
								n.getClass().toString() +
								"]");
			
		}
		if(_UUIDMap.containsKey(n.getUUID()))
		{
			throw new DimpleException(
					"\nERROR: incoming UUID [" +
					n.getUUID().toString() +
					"] already in map. \nIncoming Name: [" +
					n.getQualifiedLabel() +
					"]\nName in map:   [" +
					_UUIDMap.get(n.getUUID()).getQualifiedLabel() +
					"]\nMap string: [\n" +
					this.toString() + "]");
		}
		_nameMap.put(n.getName(), n);
		_UUIDMap.put(n.getUUID(), n);
		_list.add(n);
	}
	
	public @Nullable INameable get(String name)
	{
		return _nameMap.get(name);
	}

	public @Nullable INameable get(UUID uuid)
	{
		return _UUIDMap.get(uuid);
	}
	
	@Override
	public Iterator<INameable> iterator()
	{
		return _list.iterator();
	}
	
	public int size()
	{
		return _nameMap.size();
	}
}
