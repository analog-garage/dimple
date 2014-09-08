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

package com.analog.lyric.dimple.matlabproxy.repeated;

import com.analog.lyric.dimple.matlabproxy.PHelpers;
import com.analog.lyric.dimple.matlabproxy.PVariableVector;
import com.analog.lyric.dimple.model.repeated.VariableStreamSlice;
import com.analog.lyric.dimple.model.variables.Variable;

public class PVariableStreamSlice implements IPVariableStreamSlice
{
	private VariableStreamSlice [] _modelObjects;
	
	public PVariableStreamSlice(VariableStreamSlice [] slices)
	{
		_modelObjects = slices;
	}
	
	public VariableStreamSlice [] getModelerObjects()
	{
		return _modelObjects;
	}
	
	public PVariableVector get(int index) 
	{
		Variable [] vars = new Variable[_modelObjects.length];
		for (int i = 0; i < vars.length; i++)
			vars[i] = _modelObjects[i].get(index);
		return PHelpers.convertToVariableVector(vars);
	}


	
}
