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
	
	private double _firstVarIndex = 0;
	private ArrayList<VariableBase> _variables = new ArrayList<VariableBase>();
	
	private ArrayList<Integer> _referenceCounts = new ArrayList<Integer>();
	private IDataSource _dataSource = null;
	private ArrayList<VariableStreamSlice> _slices = new ArrayList<VariableStreamSlice>();
	private VariableStreamSlice _slice;
	

	public VariableStreamBase(Domain domain) 
	{
		_domain = domain;
		//setDataSource(dataSource);
		_slice = getSlice(0,Double.POSITIVE_INFINITY);
		
	}
	
	
//	public void setName(String name)
//	{
//		_name = name;
//	}

	public double getLastVarIndex()
	{
		return _firstVarIndex + _variables.size()-1;
	}
	
	public VariableBase getFirstVar() 
	{
		return get(getFirstVarIndex());
	}

	public VariableBase getLastVar() 
	{
		return get(getLastVarIndex());
	}

	public double getFirstVarIndex()
	{
		return _firstVarIndex;
	}

	public VariableStreamSlice getSlice(double start)
	{
		return getSlice(start,Double.POSITIVE_INFINITY);
	}

	public VariableStreamSlice getSlice(double start, double end)
	{
		return getSlice(start,1,end);
	}

	public VariableStreamSlice getSlice(double start, double increment, double end)
	{
		VariableStreamSlice ss = new VariableStreamSlice(start,increment, end,this);
		_slices.add(ss);
		return ss;
	}
	
	
	public void setDataSource(IDataSource source) 
	{
		_dataSource = source;
		
		if (_firstVarIndex != 0)
			throw new DimpleException("can't set data source after we've advanced.  Must reset first");
		

		//TODO: do I need this?
//		for (VariableStreamSlice ss : _slices)
//			ss.reset();
		
		//fill variables with data
		for (VariableBase vb : _variables)
		{
			if (!_dataSource.hasNext())
				throw new DimpleException("not enough data in data source");
			
			vb.setInputObject(_dataSource.getNext());
		}
	}
	
	public ArrayList<VariableBase> release(double index) 
	{
		//if (index != _firstVarIndex)
		//  throw new DimpleException("for now can only release first guy");
		
		if (index < _firstVarIndex)
			throw new DimpleException("The variable for the specified index has already been freed: " + index);
		
		int localIndex = (int)(index-_firstVarIndex);
		
		if (localIndex >= _variables.size())
			throw new DimpleException("out of bounds");
		
		if (_variables.size() < 1)
			throw new DimpleException("no variables to release");
		
		_referenceCounts.set(localIndex,_referenceCounts.get(localIndex)-1);
		
		ArrayList<VariableBase> retval = new ArrayList<VariableBase>();
		
		while (_variables.size() > 0 && _referenceCounts.get(0) == 0)
		{
			_firstVarIndex++;
			retval.add(_variables.get(0));
			_variables.remove(0);
			_referenceCounts.remove(0);
		}
		
		return retval;
	}
	
	abstract protected VariableBase instantiateVariable(Domain domain) ;
	
	protected VariableBase createVariable() 
	{
		VariableBase tmp;
		//TODO: better use of OOP
		
		tmp = instantiateVariable(_domain);
		//tmp.setVariableStream(this);
		
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
		if (index < _firstVarIndex)
		{
			return false;
		}

		double localIndex = index-_firstVarIndex;
		
		
		while (localIndex >= _variables.size() )
		{
			if (_dataSource != null && !_dataSource.hasNext())
			{
				return false;
			}
			
			_variables.add(createVariable());
			_referenceCounts.add(0);
		}
		
		return localIndex < _variables.size();
	}
	
	public VariableBase get(double index) 
	{
		return get(index,false);
	}
	
	public VariableBase get(double index,boolean createIfDoesntExist) 
	{
		if (index < _firstVarIndex)
			throw new DimpleException("that guy is long gone");
		
		int localIndex = (int)(index-_firstVarIndex);
		
		if (!createIfDoesntExist && localIndex >= _variables.size())
			throw new DimpleException("A variable has not yet been instantiated for the specified index: " + index);
		
		while (localIndex >= _variables.size())
		{
			_variables.add(createVariable());
			_referenceCounts.add(0);
		}
		
		return _variables.get(localIndex);

	}
	
	public VariableBase getAndAddReference(double index) 
	{
		VariableBase tmp = get(index,true);
		int localIndex = (int)(index-_firstVarIndex);
		_referenceCounts.set(localIndex,_referenceCounts.get(localIndex)+1);
		return tmp;
	}

	@Override
	public VariableBase getNext()  
	{
		// TODO Auto-generated method stub
		return _slice.getNext();
	}

	@Override
	public ArrayList<VariableBase> releaseFirst()  
	{
		// TODO Auto-generated method stub
		return _slice.releaseFirst();
	}

	@Override
	public boolean hasNext()  
	{
		// TODO Auto-generated method stub
		return _slice.hasNext();
	}
	
	public void backup(double howmuch) 
	{
		_slice.backup(howmuch);
	}
	
	public IVariableStreamSlice copy()
	{
		return _slice.copy();
	}
	
	public VariableStreamBase getStream()
	{
		return this;
	}
	
	public void reset()
	{
		_firstVarIndex = 0;
		for (VariableStreamSlice slice : _slices)
			slice.reset();
	}

}
