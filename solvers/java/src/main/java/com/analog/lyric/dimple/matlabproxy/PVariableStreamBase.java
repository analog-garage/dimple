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
