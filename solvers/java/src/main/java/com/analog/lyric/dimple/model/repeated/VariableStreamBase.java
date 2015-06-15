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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class VariableStreamBase<V extends Variable> implements IVariableStreamSlice<V>
{
	private Domain _domain;
	
	private ArrayList<V> _variables = new ArrayList<>();
	private @Nullable IDataSource _dataSource = null;
	private @Nullable IDataSink _dataSink = null;
	private ArrayList<VariableStreamSlice<V>> _slices = new ArrayList<>();
	private VariableStreamSlice<V> _slice;
	private final @Nullable String _namePrefix;

	protected VariableStreamBase(Domain domain, @Nullable String namePrefix)
	{
		_domain = domain;
		_slice = getSlice(0);
		_namePrefix = namePrefix;
	}
	
	/**
	 * Return {@link Class} of variable used by this stream.
	 * @since 0.08
	 */
	protected abstract Class<? extends V> variableType();
	
	public int size()
	{
		return _variables.size();
	}
	
	@SuppressWarnings("unchecked")
	public V [] getVariables()
	{
		return _variables.toArray((V[])Array.newInstance(variableType(), _variables.size()));
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
	
	
	public VariableStreamSlice<V> getSlice(int start)
	{
		VariableStreamSlice<V> ss = new VariableStreamSlice<>(start,this);
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

	
	abstract protected V instantiateVariable(Domain domain) ;
	
	protected V createVariable(int index)
	{
		V tmp;
		
		tmp = instantiateVariable(_domain);
		
		if (_namePrefix != null)
		{
			tmp.setName(_namePrefix + index);
		}
		
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

		for (int i = _variables.size(); i <= index; ++i)
		{
			if (!hasNext())
			{
				return false;
			}
			
			_variables.add(createVariable(i));
		}
		
		return index < _variables.size();
	}
	
	
	@NonNull // FIXME - workaround for Eclipse JDT bug (467610?)
	@Override
	public V get(int index)
	{
		return get(index,false);
	}
	
	@NonNull // FIXME - workaround for Eclipse JDT bug (467610?)
	@Override
	public V get(int index,boolean createIfDoesntExist)
	{
		if (index < 0)
			throw new DimpleException("negative indexing not allowed");
		
		if (!createIfDoesntExist && index >= _variables.size())
			throw new DimpleException("A variable has not yet been instantiated for the specified index: " + index);
		
		for (int i = _variables.size(); i <= index; ++i)
		{
			_variables.add(createVariable(i));
		}
		
		return _variables.get(index);

	}

	public boolean hasNext()
	{
		final IDataSource dataSource = _dataSource;
		if (dataSource == null)
			return true;
		else
			return dataSource.hasNext();

	}
	
	public IVariableStreamSlice<V> copy()
	{
		return _slice.copy();
	}
	
	@Override
	public VariableStreamBase<V> getStream()
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
