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

package com.analog.lyric.dimple.model.repeated;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.VariableBase;

public abstract class VariableStreamBase implements IVariableStreamSlice
{
	private Domain _domain;
	
	private ArrayList<VariableBase> _variables = new ArrayList<VariableBase>();	
	private IDataSource _dataSource = null;
	private IDataSink _dataSink = null;
	private ArrayList<VariableStreamSlice> _slices = new ArrayList<VariableStreamSlice>();
	private VariableStreamSlice _slice;
	

	public VariableStreamBase(Domain domain) 
	{
		_domain = domain;
		_slice = getSlice(0);
		
	}
	
	public int size()
	{
		return _variables.size();
	}
	
	public VariableBase [] getVariables()
	{
		VariableBase [] vars = new VariableBase[_variables.size()];
		_variables.toArray(vars);
		return vars;
	}
	
	public boolean contains(VariableBase vb)
	{
		return _variables.contains(vb);
	}
	
	public void advanceInputs()
	{
		if (_dataSink != null)
		{
			Object output = _variables.get(0).getBeliefObject();
			_dataSink.push(output);
		}
		
		for (int i = 0; i < _variables.size()-1; i++)
			_variables.get(i).moveInputs(_variables.get(i+1));

		if (_dataSource != null)
		{
			Object input = _dataSource.getNext();
			_variables.get(_variables.size()-1).setInputObject(input);
		}
		else
		{
			_variables.get(_variables.size()-1).setInputObject(null);
		}
	
	}
	
	
	public VariableStreamSlice getSlice(int start)
	{
		VariableStreamSlice ss = new VariableStreamSlice(start,this);
		_slices.add(ss);
		return ss;
	}
	
	public IDataSink getDataSink()
	{
		return _dataSink;
	}
	public IDataSource getDataSource()
	{
		return _dataSource;
	}
	
	public void setDataSink(IDataSink sink)
	{
		_dataSink = sink;
	}
	
	public void setDataSource(IDataSource source) 
	{
		_dataSource = source;
		
		//fill variables with data
		for (VariableBase vb : _variables)
		{
			if (!_dataSource.hasNext())
				throw new DimpleException("not enough data in data source");
			
			vb.setInputObject(_dataSource.getNext());
		}
	}
	
	abstract protected VariableBase instantiateVariable(Domain domain) ;
	
	protected VariableBase createVariable() 
	{
		VariableBase tmp;
		
		tmp = instantiateVariable(_domain);
		
		if (_dataSource != null)
		{
			
			if (!_dataSource.hasNext())
				throw new DimpleException("out of data");
			
			tmp.setInputObject(_dataSource.getNext());
		}
		
		return tmp;

	}
	
	public boolean variableAvailableFor(double index) 
	{
		if (index < 0)
		{
			return false;
		}

		double localIndex = index;
		
		
		while (localIndex >= _variables.size() )
		{
			if (_dataSource != null && !_dataSource.hasNext())
			{
				return false;
			}
			
			_variables.add(createVariable());
		}
		
		return localIndex < _variables.size();
	}
	
	
	public VariableBase get(int index) 
	{
		return get(index,false);
	}
	
	public VariableBase get(int index,boolean createIfDoesntExist) 
	{
		if (index < 0)
			throw new DimpleException("negative indexing not allowed");
		
		int localIndex = (int)(index);
		
		if (!createIfDoesntExist && localIndex >= _variables.size())
			throw new DimpleException("A variable has not yet been instantiated for the specified index: " + index);
		
		while (localIndex >= _variables.size())
		{
			_variables.add(createVariable());
		}
		
		return _variables.get(localIndex);

	}

	public boolean hasNext()  
	{
		if (_dataSource == null)
			return true;
		else
			return _dataSource.hasNext();

	}
	
	public IVariableStreamSlice copy()
	{
		return _slice.copy();
	}
	
	public VariableStreamBase getStream()
	{
		return this;
	}
	

	int indexOf(VariableBase vb)
	{
		return _variables.indexOf(vb);
	}
	
	void cleanupUnusedVariables()
	{

		for(int i =  _variables.size()-1; i >= 0; i--)
		{
			if (_variables.get(i).getParentGraph()==null)
				_variables.remove(i);
		}
	}

}
