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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverTableFactor;

public abstract class STableFactorBase extends SFactorBase implements ISolverTableFactor
{
	//protected Factor _factor;
	//protected TableFactor _tableFactor;
	private FactorTable _factorTable = null;

	public STableFactorBase(Factor factor) 
	{
		super(factor);
		
		if (!factor.isDiscrete())
			throw new DimpleException("only discrete factors supported");
		
		//_factorTable = factor.getFactorFunction().getFactorTable(factor.getDomains());

		//if (!factor.isDiscreteFactor())
		//	throw new Exception
		
		//_tableFactor = (TableFactor)factor;
	}
	
	public final FactorTable getFactorTable()
	{
		if (_factorTable==null)
			_factorTable = getFactor().getFactorFunction().getFactorTable(getFactor().getDomains());
		return this._factorTable;
	}

	@Override
	public int [][] getPossibleBeliefIndices() 
	{
		return getFactorTable().getIndices();
	}

}
