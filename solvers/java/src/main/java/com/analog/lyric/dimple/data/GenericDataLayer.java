/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.data;

import com.analog.lyric.dimple.data.FactorGraphData.Constructor;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class GenericDataLayer extends DataLayer<IDatum>
{
	public GenericDataLayer(FactorGraph graph, Constructor<Variable, IDatum> constructor)
	{
		super(graph, constructor);
	}
	
	public GenericDataLayer(FactorGraph graph, DataDensity density)
	{
		super(graph, density, IDatum.class);
	}
	
	public GenericDataLayer(FactorGraph graph)
	{
		this(graph, SparseFactorGraphData.constructorForType(Variable.class, IDatum.class));
	}
	
	protected GenericDataLayer(GenericDataLayer other)
	{
		super(other);
	}
	
	@Override
	public GenericDataLayer clone()
	{
		return new GenericDataLayer(this);
	}
	
	public static GenericDataLayer dense(FactorGraph graph)
	{
		return new GenericDataLayer(graph, DenseFactorGraphData.constructorForType(Variable.class, IDatum.class));
	}

	public static GenericDataLayer sparse(FactorGraph graph)
	{
		return new GenericDataLayer(graph, SparseFactorGraphData.constructorForType(Variable.class, IDatum.class));
	}
}
