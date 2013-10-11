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

package com.analog.lyric.dimple.model.core;

import java.util.UUID;

public class NodeId
{
	private static UUID _uuid = java.util.UUID.randomUUID();
	private static int nextId = 0;
	
	/*
	 * Generating new UUIDs is somewhat expensive.  For now, simply generate 
	 * one and increment by one as needed.  In the future, when we work on 
	 * serialization, we should re-think this.
	 */
	public static UUID getNextUUID()
	{
		UUID retval = new UUID(_uuid.getMostSignificantBits(),_uuid.getLeastSignificantBits());
		_uuid = new UUID(_uuid.getMostSignificantBits(), _uuid.getLeastSignificantBits()+1);
		return retval;
	}
	
	public static int getNext()
	{
		return nextId++;
	}
	
	public static void initialize()
	{
		nextId = 0;		
	}
}
