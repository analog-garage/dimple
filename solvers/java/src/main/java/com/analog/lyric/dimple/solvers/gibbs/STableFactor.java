/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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
import java.util.Arrays;

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
	    int[][] table = getFactorTable().getIndices();
	    double[] values = getFactorTable().getWeights();
	    int tableLength = table.length;
	    int numPorts = ports.size();
	    int[] inPortMsgs = new int[numPorts];
	    for (int port = 0; port < numPorts; port++) inPortMsgs[port] = (Integer)ports.get(port).getInputMsg();
	    
        double[] outputMsgs = (double[])ports.get(outPortNum).getOutputMsg();
    	int outputMsgLength = outputMsgs.length;
        for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;
        
    	// TODO: This should be a direct look-up from the input values, not a search; perhaps a hash table
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	int[] tableRow = table[tableIndex];
        	if (equalsWithExclusion(inPortMsgs, tableRow, outPortNum))
        	{
        		double prob = values[tableIndex];
        		int outputIndex = tableRow[outPortNum];
        		outputMsgs[outputIndex] = prob;
        		
//        		// For finding the minimum energy estimate
//        		if (inPortMsgs[outPortNum] == tableRow[outPortNum])
//        			_currentProb = prob;
        	}
        }
		// NOTE: outputMsgs is not a normalized distribution, but this is fine
		// since Variable doesn't require normalized input messages
	}
	
	
	public void update()
	{
		// TODO: This should throw the exception, but that would propagate to
		// the base class and all other derived classes, this is the quick and dirty way.
    	new DimpleException("Method not supported in Gibbs sampling solver.").printStackTrace();
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
	    int[][] table = getFactorTable().getIndices();
	    double[] values = getFactorTable().getWeights();
	    int tableLength = table.length;
	    
	    double prob = 0;
    	// TODO: This should be a direct look-up from the input values, not a search; perhaps a hash table
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	int[] tableRow = table[tableIndex];
        	if (Arrays.equals(inputs, tableRow))
        	{
        		prob = values[tableIndex];
        		break;
        	}
        }		
		
		return -Math.log(prob);
	}
	

	
    /**
     * Modified version of Arrays.equals that excludes the specified index from the comparison
     * 
     * Arrays.equals:
     * Returns <tt>true</tt> if the two specified arrays of ints are
     * <i>equal</i> to one another.  Two arrays are considered equal if both
     * arrays contain the same number of elements, and all corresponding pairs
     * of elements in the two arrays are equal.  In other words, two arrays
     * are equal if they contain the same elements in the same order.  Also,
     * two array references are considered equal if both are <tt>null</tt>.<p>
     *
     * @param a one array to be tested for equality
     * @param a2 the other array to be tested for equality
     * @return <tt>true</tt> if the two arrays are equal
     */
	public static boolean equalsWithExclusion(int[] a, int[] a2, int exclusionIndex) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
        	if (i != exclusionIndex)	// Added line for exclusion
        		if (a[i] != a2[i])
        			return false;

        return true;
    }

	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for gibbs");
	}


}
