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

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SampleDataLayer extends DataLayer<Value>
{
	public SampleDataLayer(FactorGraph graph, Constructor<Value> constructor)
	{
		super(graph, constructor);
	}

	public SampleDataLayer(FactorGraph graph)
	{
		this(graph, DenseFactorGraphData.constructorForType(Value.class));
	}
	
	protected SampleDataLayer(SampleDataLayer other)
	{
		super(other);
	}
	
	@Override
	public SampleDataLayer clone()
	{
		return new SampleDataLayer(this);
	}
}
