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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;


public class STableFactor extends STableFactorBase implements ISolverFactorGibbs
{	
	
	public STableFactor(Factor factor) 
	{
		super(factor);
	}
	

	public void updateEdge(int outPortNum)
	{
		ArrayList<Port> ports = _factor.getPorts();
		FactorTable factorTable = getFactorTable();
	    double[] factorTableWeights = factorTable.getWeights();
	    int numPorts = ports.size();
	    
	    int[] inPortMsgs = new int[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    	inPortMsgs[port] = (Integer)ports.get(port).getInputMsg();
	    
        double[] outputMsgs = (double[])ports.get(outPortNum).getOutputMsg();
    	int outputMsgLength = outputMsgs.length;
		for (int outIndex = 0; outIndex < outputMsgLength; outIndex++)
		{
			inPortMsgs[outPortNum] = outIndex;
			int weightIndex = factorTable.getWeightIndexFromTableIndices(inPortMsgs);
			outputMsgs[outIndex] = -Math.log(factorTableWeights[weightIndex]);
		}
	}
	
	
	public void update()
	{
    	throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}
	

	public Object getDefaultMessage(Port port) 
	{
		// WARNING: This method of initialization doesn't ensure a valid joint
		// value if the joint distribution has any zero-probability values
		return Integer.valueOf(0);
	}

	public double Potential() {return getPotential();}
	public double getPotential()
	{
		ArrayList<Port> ports = _factor.getPorts();
	    int numPorts = ports.size();
	    int[] inPortMsgs = new int[numPorts];
	    for (int port = 0; port < numPorts; port++) inPortMsgs[port] = (Integer)ports.get(port).getInputMsg();
	    
	    return getPotential(inPortMsgs);
	}
	public double getPotential(int[] inputs)
	{
		FactorTable factorTable = getFactorTable();
		int weightIndex = factorTable.getWeightIndexFromTableIndices(inputs);
		return -Math.log(factorTable.getWeights()[weightIndex]);
	}
	
}
