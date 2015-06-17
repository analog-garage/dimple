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
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * A dense {@link DataLayerBase} that can only contain {@link Value} objects.
 * <p>
 * Can be used to represent samples, MAP assignments, conditioning, and for computing
 * likelihood of given assignment.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class ValueDataLayer extends DataLayer<Value>
{
	public ValueDataLayer(FactorGraph graph, Constructor<Variable,Value> constructor)
	{
		super(graph, constructor);
	}

	public ValueDataLayer(FactorGraph graph, DataDensity density)
	{
		super(graph, density, Value.class);
	}
	
	public ValueDataLayer(FactorGraph graph)
	{
		this(graph, DataDensity.DENSE);
	}
	
	protected ValueDataLayer(ValueDataLayer other)
	{
		super(other);
	}
	
	@Override
	public ValueDataLayer clone()
	{
		return new ValueDataLayer(this);
	}
	
	public static ValueDataLayer dense(FactorGraph graph)
	{
		return new ValueDataLayer(graph, DenseFactorGraphData.constructorForType(Variable.class, Value.class));
	}

	public static ValueDataLayer sparse(FactorGraph graph)
	{
		return new ValueDataLayer(graph, SparseFactorGraphData.constructorForType(Variable.class, Value.class));
	}
}
