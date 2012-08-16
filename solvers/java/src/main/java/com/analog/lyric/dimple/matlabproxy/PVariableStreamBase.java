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

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.IDataSource;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;

public abstract class PVariableStreamBase implements IPVariableStreamSlice
{
	private VariableStreamBase _modelObject;
	
	public PVariableStreamBase(VariableStreamBase varStream)
	{
		_modelObject = varStream;
	}
	
//	public PVariableStreamBase(PDomain domain) 
//	{
//		_modelObject = new VariableStreamBase(domain.getModelerObject());
//	}
	
	public PVariableStreamSlice getSlice(double startVal, double increment,  double endVal)
	{
		return new PVariableStreamSlice(_modelObject.getSlice(startVal,increment, endVal));
	}
	public VariableStreamBase getModelerObject()
	{
		return _modelObject;
	}
	
	public PVariableVector get(int index) 
	{
		VariableBase var = _modelObject.get(index);
		return PHelpers.convertToVariableVector(PHelpers.convertToVariables(new VariableBase[]{var}));
	}
	
	public void setDataSource(IDataSource dataSource) 
	{
		_modelObject.setDataSource(dataSource);
	}
	
	public double getLastVarIndex()
	{
		return _modelObject.getLastVarIndex();
	}
	
	public PVariableVector getFirstVar() 
	{
		return PHelpers.convertToVariableVector(PHelpers.convertToVariables(new VariableBase[]{_modelObject.getFirstVar()}));
	}

	public PVariableVector getLastVar() 
	{
		return PHelpers.convertToVariableVector(PHelpers.convertToVariables(new VariableBase[]{_modelObject.getLastVar()}));
	}

	public double getFirstVarIndex()
	{
		return _modelObject.getFirstVarIndex();
	}
}
