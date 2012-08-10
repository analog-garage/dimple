package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.VariableStreamSlice;

public class PVariableStreamSlice implements IPVariableStreamSlice
{
	private VariableStreamSlice _modelObject;
	
	public PVariableStreamSlice(VariableStreamSlice slice)
	{
		_modelObject = slice;
	}
	
	public VariableStreamSlice getModelerObject()
	{
		return _modelObject;
	}
	
	public PVariableVector getNext() 
	{
		VariableBase var = _modelObject.getNext();
		return PHelpers.convertToVariableVector(PHelpers.convertToVariables(new VariableBase[]{var}));

	}
	
	public boolean hasNext() 
	{
		return _modelObject.hasNext();
	}
}
