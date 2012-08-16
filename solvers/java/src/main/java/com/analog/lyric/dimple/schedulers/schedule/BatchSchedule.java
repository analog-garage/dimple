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

package com.analog.lyric.dimple.schedulers.schedule;

import java.util.Collection;
import java.util.HashMap;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BatchScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.util.misc.Misc;

public class BatchSchedule extends FixedSchedule 
{
	public BatchSchedule() 
	{
		super();
		check();
	}

	public BatchSchedule(IScheduleEntry[] entries) 
	{
		super(entries);
		check();
	}

	public BatchSchedule(Collection<IScheduleEntry> entries)  
	{
		super(entries);
		check();
	}

	public BatchSchedule(ISchedule s) 
	{
		super(s);
		check();
	}	
	
	void check() 
	{
		for(IScheduleEntry entry: _schedule)
		{
			if(!(entry instanceof BatchScheduleEntry))
			{
				throw new DimpleException(String.format("Entry [%s] is [%s] not [%s]"
						,entry
						,entry.getClass()
						,BatchScheduleEntry.class));
			}
		}		
	}
	
	public void newBatch()
	{
		addBatch(new BatchScheduleEntry());
	}
	
	public void addBatch(BatchScheduleEntry batch)
	{
		_schedule.add(batch);
	}
	
	public BatchScheduleEntry getCurrentBatch()
	{
		BatchScheduleEntry entry = null;
		int lastIdx = _schedule.size() - 1; 
		if(lastIdx >= 0)
		{
			entry = (BatchScheduleEntry) _schedule.get(lastIdx);
		}
		return entry;
	}
	
	public void addToCurrentBatch(IScheduleEntry entry) 
	{
		getCurrentBatch().add(entry);
	}
	
	public void shuffle()
	{
		for(IScheduleEntry entry : _schedule)
		{
			BatchScheduleEntry batchEntry = (BatchScheduleEntry) entry;
			batchEntry.shuffle();
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(String.format("BatchSchedule %d batches\n", _schedule.size()));
		for(IScheduleEntry entry : _schedule)
		{
			sb.append("\t" + entry.toString() + "\n");
		}
		return sb.toString();
		
	}

	public ISchedule copy(HashMap<Object,Object> old2newObjs, boolean copyToRoot) 
	{
		FixedSchedule copy = (FixedSchedule) super.copy(old2newObjs, copyToRoot);
		return Misc.trivialBatchify(copy);
		//return new BatchSchedule(copy.getSchedule());		
	}
}

