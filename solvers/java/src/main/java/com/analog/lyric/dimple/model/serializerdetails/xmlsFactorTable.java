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

package com.analog.lyric.dimple.model.serializerdetails;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

class xmlsFactorTable
{
	public String _functionName;
	public int _ephemeralId;
	public @Nullable int[][] _indices;
	public @Nullable int[] _jointIndices;
	public double[] _weights;
	public @Nullable JointDomainIndexer _domains;
	
	public xmlsFactorTable(
		String functionName, int ephemeralId, int[] jointIndices, double[] weights, JointDomainIndexer domains)
	{
		_functionName = functionName;
		_ephemeralId = ephemeralId;
		_jointIndices = jointIndices;
		_weights = weights;
		_domains = domains;
	}

	public xmlsFactorTable(
		String functionName, int ephemeralId, int[][] indices, double[] weights)
	{
		_functionName = functionName;
		_ephemeralId = ephemeralId;
		_indices = indices;
		_weights = weights;
	}

	public xmlsFactorTable(String functionName, int ephemeralId, IFactorTable factorTable)
	{
		_functionName = functionName;
		_ephemeralId = ephemeralId;
		_domains = factorTable.getDomainIndexer();

		final boolean sparse = factorTable.hasSparseRepresentation();
		final int size = sparse? factorTable.sparseSize() : factorTable.jointSize();
		
		final int[] jointIndices = new int[size];
		final double[] weights = new double[size];
		
		if (sparse)
		{
			for (int si = 0; si < size; ++si)
			{
				jointIndices[si] = factorTable.sparseIndexToJointIndex(si);
				weights[si] = factorTable.getWeightForSparseIndex(si);
			}
		}
		else
		{
			for (int ji = 0; ji < size; ++ji)
			{
				jointIndices[ji] = ji;
				weights[ji] = factorTable.getWeightForJointIndex(ji);
			}
		}
		
		_jointIndices = jointIndices;
		_weights = weights;
	}

	public IFactorTable makeTable()
	{
		IFactorTable table = FactorTable.create(Objects.requireNonNull(_domains));
		table.setWeightsSparse(Objects.requireNonNull(_jointIndices), _weights);
		return table;
	}
	
	public IFactorTable makeTable(DiscreteDomain[] domains)
	{
		final int[] jointIndices = _jointIndices;
		if (jointIndices != null)
		{
			IFactorTable table = FactorTable.create(domains);
			table.setWeightsSparse(jointIndices, _weights);
			return table;
		}
		else
		{
			return FactorTable.create(Objects.requireNonNull(_indices), _weights, domains);
		}
	}
}

