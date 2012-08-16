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

import java.util.HashMap;

import com.analog.lyric.dimple.model.FactorGraph;

public abstract class ScheduleBase implements ISchedule 
{
	protected FactorGraph _factorGraph;
	
	
	/*
	 * This method is called when setSchedule is called on the FactorGraph.
	 */
	public void attach(FactorGraph factorGraph) 
	{
		_factorGraph = factorGraph;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _factorGraph;
	}

	public ISchedule copy(HashMap<Object,Object> old2newObjs) 
	{
		return null;
	}
	public ISchedule copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return null;
	}
}
