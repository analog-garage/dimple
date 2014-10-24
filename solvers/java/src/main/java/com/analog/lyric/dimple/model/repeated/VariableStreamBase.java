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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class VariableStreamBase implements IVariableStreamSlice
{
	private Domain _domain;
	
	private ArrayList<Variable> _variables = new ArrayList<Variable>();
	private @Nullable IDataSource _dataSource = null;
	private @Nullable IDataSink _dataSink = null;
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
	
	public Variable [] getVariables()
	{
		Variable [] vars = new Variable[_variables.size()];
		_variables.toArray(vars);
		return vars;
	}
	
	public boolean contains(Variable vb)
	{
		return _variables.contains(vb);
	}
	
	public void advanceState()
	{
		final IDataSink dataSink = _dataSink;
		if (dataSink != null)
		{
			Object output = _variables.get(0).getBeliefObject();
			dataSink.push(Objects.requireNonNull(output));
		}
		
		for (int i = 0; i < _variables.size()-1; i++)
		{
			_variables.get(i).moveInputs(_variables.get(i+1));
			final ISolverVariable otherVar = requireNonNull(_variables.get(i+1).getSolver());
			requireNonNull(_variables.get(i).getSolver()).moveNonEdgeSpecificState(otherVar);
		}
		requireNonNull(_variables.get(_variables.size()-1).getSolver()).createNonEdgeSpecificState();

		final IDataSource dataSource = _dataSource;
		if (dataSource != null)
		{
			Object input = dataSource.getNext();
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
	
	public @Nullable IDataSink getDataSink()
	{
		return _dataSink;
	}
	public @Nullable IDataSource getDataSource()
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
		for (Variable vb : _variables)
		{
			if (!source.hasNext())
				throw new DimpleException("not enough data in data source");
			
			vb.setInputObject(source.getNext());
		}
	}
	
	/**
	 * The variable domain of the stream.
	 * @since 0.07
	 */
	public Domain getDomain()
	{
		return _domain;
	}

	
	abstract protected Variable instantiateVariable(Domain domain) ;
	
	protected Variable createVariable()
	{
		Variable tmp;
		
		tmp = instantiateVariable(_domain);
		
		final IDataSource dataSource = _dataSource;
		if (dataSource != null)
		{
			
			if (!dataSource.hasNext())
				throw new DimpleException("out of data");
			
			tmp.setInputObject(dataSource.getNext());
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
			if (!hasNext())
			{
				return false;
			}
			
			_variables.add(createVariable());
		}
		
		return localIndex < _variables.size();
	}
	
	
	@Override
	public Variable get(int index)
	{
		return get(index,false);
	}
	
	@Override
	public Variable get(int index,boolean createIfDoesntExist)
	{
		if (index < 0)
			throw new DimpleException("negative indexing not allowed");
		
		int localIndex = (index);
		
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
		final IDataSource dataSource = _dataSource;
		if (dataSource == null)
			return true;
		else
			return dataSource.hasNext();

	}
	
	public IVariableStreamSlice copy()
	{
		return _slice.copy();
	}
	
	@Override
	public VariableStreamBase getStream()
	{
		return this;
	}
	

	int indexOf(Variable vb)
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
