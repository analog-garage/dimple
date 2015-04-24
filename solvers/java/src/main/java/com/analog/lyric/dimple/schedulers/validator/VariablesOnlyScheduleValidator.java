/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.schedulers.validator;

import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class VariablesOnlyScheduleValidator extends InGraphScheduleValidator
{
	@SuppressWarnings("deprecation")
	@Override
	public void validateNext(IScheduleEntry entry) throws ScheduleValidationException
	{
		super.validateNext(entry);
		
		switch (entry.type())
		{
		case NODE:
			if (!((NodeScheduleEntry)entry).getNode().isVariable())
			{
				throw notAllowed(entry);
			}
			break;
		case EDGE:
			throw notAllowed(entry);
			
		case SUBSCHEDULE:
			for (IScheduleEntry subentry :
				((com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry)entry).getSubschedule())
			{
				validateNext(subentry);
			}
			break;
			
		case CUSTOM:
			throw notAllowed(entry);
			
		default: // ok
			break;
		}
	}
	
	
	private ScheduleValidationException notAllowed(IScheduleEntry entry)
	{
		return new ScheduleValidationException("%s not allowed in schedule", entry);
	}
}
