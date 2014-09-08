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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.matlabproxy.PHelpers;
import com.analog.lyric.dimple.matlabproxy.PNodeVector;
import com.analog.lyric.dimple.matlabproxy.PVariableVector;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.repeated.IDataSink;
import com.analog.lyric.dimple.model.repeated.IDataSource;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;
import com.analog.lyric.dimple.model.repeated.VariableStreamSlice;
import com.analog.lyric.dimple.model.variables.Variable;

public abstract class PVariableStreamBase implements IPVariableStreamSlice
{
	private VariableStreamBase [] _modelObjects;
	
	public PVariableStreamBase(Domain domain, int numVars)
	{
		_modelObjects = new VariableStreamBase[numVars];
		for (int i = 0; i < numVars; i++)
			_modelObjects[i] = createVariable(domain);
	}
	
	protected abstract VariableStreamBase createVariable(Domain domain);
	
	public PVariableStreamSlice getSlice(int startVal)
	{
		VariableStreamSlice [] vars = new VariableStreamSlice[_modelObjects.length];
		for (int i = 0; i < vars.length; i++)
			vars[i] = _modelObjects[i].getSlice(startVal);

		return new PVariableStreamSlice(vars);
	}
	
	public VariableStreamBase [] getModelerObjects()
	{
		return _modelObjects;
	}
	
	public PVariableVector getVariables()
	{
		PVariableVector retval = new PVariableVector();
		
		for (int i = 0; i < _modelObjects.length; i++)
		{
			PVariableVector pvarvector = PHelpers.convertToVariableVector(_modelObjects[i].getVariables());
			retval = (PVariableVector)retval.concat(new PNodeVector[]{pvarvector});
		}
		
		//This will ensure that it is cast to the right type
		return PHelpers.convertToVariableVector(retval.getVariableArray());
	}
	
	public int size()
	{
		return _modelObjects[0].size();
	}
	
	public PVariableVector get(int index) 
	{
		Variable [] vars = new Variable[_modelObjects.length];
		for (int i = 0; i < vars.length; i++)
			vars[i] = _modelObjects[i].get(index);
		return PHelpers.convertToVariableVector(vars);
	}
	
	public void setDataSource(IPDataSource dataSource) 
	{
		IDataSource [] ds = dataSource.getModelObjects();
		if (ds.length != _modelObjects.length)
			throw new DimpleException("DataSource size does not match");
		
		for (int i = 0; i < ds.length; i++)
			_modelObjects[i].setDataSource(ds[i]);
	}
	public void setDataSink(IPDataSink dataSink) 
	{
		IDataSink [] ds = dataSink.getModelObjects();
		if (ds.length != _modelObjects.length)
			throw new DimpleException("DataSource size does not match");
		
		for (int i = 0; i < ds.length; i++)
			_modelObjects[i].setDataSink(ds[i]);
	}
	

	public IPDataSource getDataSource() 
	{
		return PHelpers.getDataSources(getModelerObjects());
	}

	public IPDataSink getDataSink() 
	{
		return PHelpers.getDataSinks(getModelerObjects());
	}	
}
