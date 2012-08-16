/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.analog.lyric.dimple.model.Port;

public class BatchScheduleEntry implements IScheduleEntry 
{
	private ArrayList<IScheduleEntry> _entries;
	
	public BatchScheduleEntry()
	{
		_entries = new ArrayList<IScheduleEntry>();
	}
	public ArrayList<IScheduleEntry> getEntries(){return _entries;}
	public int size(){return _entries.size();}
	
	public void add(IScheduleEntry entry) 
	{
		_entries.add(entry);
	}
	public IScheduleEntry get(int idx)
	{
		return _entries.get(idx);
	}
	
	public void update()  
	{
		for(IScheduleEntry entry : _entries)
		{
			entry.update();
		}
	}

	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, false);
	}
	public IScheduleEntry copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, true);
	}
	
	public IScheduleEntry copy(HashMap<Object, Object> old2newObjs, boolean copyToRoot)
			 
	{
		BatchScheduleEntry newEntry = new BatchScheduleEntry();
		for(IScheduleEntry entry : _entries)
		{
			IScheduleEntry copyEntry = copyToRoot ? 
										entry.copyToRoot(old2newObjs) : 
										entry.copy(old2newObjs); 
			if(copyEntry != null)
			{
				newEntry._entries.add(copyEntry);
			}
			else
			{
				copyEntry = copyToRoot ? 
						entry.copyToRoot(old2newObjs) : 
						entry.copy(old2newObjs); 				
			}
		}
		return newEntry;
	}
	
	public void shuffle()
	{
		Collections.shuffle(_entries);
	}
	
	public Iterable<Port> getPorts() 
	{
		ArrayList<Port> ports = new ArrayList<Port>();
		for(IScheduleEntry entry : _entries)
		{
			if(entry == null)
			{
				System.out.println(getBigString());
			}
			for(Port port : entry.getPorts())
			{
				ports.add(port);
			}
		}
		return ports;
	}
		
	public String getBigString()
	{
		StringBuilder sb = new StringBuilder();		
		getBigString(sb);
		return sb.toString();
	}
	public void getBigString(StringBuilder sb)
	{
		sb.append(String.format("BatchScheduleEntry  %d entries\n", _entries.size()));
		for(IScheduleEntry entry : _entries)
		{
			sb.append("\t");
			sb.append(entry != null ? entry.toString() : "null");
			sb.append("\n");
		}
	}
	
	public String toString()
	{
		return getBigString();
	}
}
