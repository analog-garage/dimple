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

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.IDataSink;
import com.analog.lyric.dimple.model.repeated.IDataSource;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;

public abstract class PVariableStreamBase implements IPVariableStreamSlice
{
	private VariableStreamBase _modelObject;
	
	public PVariableStreamBase(VariableStreamBase varStream)
	{
		_modelObject = varStream;
	}
		
	public PVariableStreamSlice getSlice(int startVal)
	{
		return new PVariableStreamSlice(_modelObject.getSlice(startVal));
	}
	public VariableStreamBase getModelerObject()
	{
		return _modelObject;
	}
	
	public PVariableVector get(int index) 
	{
		VariableBase var = _modelObject.get(index);
		return PHelpers.convertToVariableVector(new VariableBase[]{var});
	}
	
	public void setDataSource(IDataSource dataSource) 
	{
		_modelObject.setDataSource(dataSource);
	}
	public void setDataSink(IDataSink dataSink) 
	{
		_modelObject.setDataSink(dataSink);
	}
	
	public IDataSource getDataSource()
	{
		return _modelObject.getDataSource();
	}
	public IDataSink getDataSink()
	{
		return _modelObject.getDataSink();
	}
	
	
}
