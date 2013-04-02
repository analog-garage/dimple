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

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class BlastFromThePastFactor extends Factor 
{
	
	private Port _portForOtherVariable;
	private VariableBase _variableConnectedToBlast;
	
	public BlastFromThePastFactor(int id, VariableBase varConnectedToBlast, 
			Port portForOtherVar) 
	{
		super(id,((Factor)portForOtherVar.getSibling()).getFactorFunction(),new VariableBase[]{varConnectedToBlast});
		
		_portForOtherVariable = portForOtherVar;
		_variableConnectedToBlast = varConnectedToBlast;
	}

	public void createSolverObject(ISolverFactorGraph factorGraph) 
	{
		_variables = null;
		_solverFactor = factorGraph.createBlastFromThePast(this);
		((ISolverBlastFromThePastFactor)_solverFactor).createMessages(
				_variableConnectedToBlast,
				_portForOtherVariable);
	}
	
	
	public void advance()
	{
		((ISolverBlastFromThePastFactor)_solverFactor).advance();
	}
		
	
}
