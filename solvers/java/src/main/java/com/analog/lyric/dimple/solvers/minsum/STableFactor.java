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

package com.analog.lyric.dimple.solvers.minsum;

import java.util.ArrayList;

import com.analog.lyric.cs.Sort;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.core.kbest.IKBestFactor;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorEngine;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorTableEngine;

public class STableFactor extends STableFactorBase implements IKBestFactor
{	
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
    protected double[][] _inPortMsgs = null;
    protected double[][] _outPortMsgs = null;
    protected double [][] _savedOutMsgArray;
    protected double [] _dampingParams;
    private int _k;
    TableFactorEngine _tableFactorEngine;
    KBestFactorEngine _kbestFactorEngine;
    boolean _kIsSmallerThanDomain;
    
    /*
     * We also save the values from the combo table.  This is necessary
     * since the minsum algorithm requires a modified list of values
     */
    //double [] _values = null;

    public STableFactor(Factor factor) 
	{
    	super(factor);
    	
    	//_values = values;
		_dampingParams = new double[_factor.getPorts().size()];
		updateCache();
		
		_tableFactorEngine = new TableFactorEngine(this);
		
		//TODO: should I recheck for factor table every once in a while?
		if (factor.getFactorFunction().factorTableExists(getFactor().getDomains()))
			_kbestFactorEngine = new KBestFactorTableEngine(this);
		else
			_kbestFactorEngine = new KBestFactorEngine(this);
		
		setK(Integer.MAX_VALUE);
		
	}


	public void setK(int k)
	{
		
		_k = k;
		_kbestFactorEngine.setK(k);
		_kIsSmallerThanDomain = false;
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			if (_k < _inPortMsgs[i].length)
			{
				_kIsSmallerThanDomain = true;
				break;
			}
		}
	}
    
	public Object getInitialMsgValue(Port port)
	{
		int domainLength = ((Discrete)port.getConnectedNode()).getDiscreteDomain().getElements().length;
		double[] retVal = new double[domainLength];
		for (int i = 0; i < domainLength; i++) retVal[i] = 0;
		return retVal;
	}

	public void updateEdge(int outPortNum) 
	{
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.updateEdge(outPortNum);
		else
			_tableFactorEngine.updateEdge(outPortNum);

	}
	
	
	@Override
	public void update() 
	{
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.update();
		else
			_tableFactorEngine.update();
	}
	
    public void initialize() 
    {
    	super.initialize();
		//We update the cache here.  This works only because initialize() is called on the variables
		//first.  Updating the cache saves msg in double arrays.  initialize replaces these double arrays
		//with new double arrays.  If we didn't call updateCache on initialize, our cache would point
		//to stale information.
    	updateCache();
    }
    
    private void updateCache()
    {
    	int numPorts = _factor.getPorts().size();
	    _inPortMsgs = new double[numPorts][];
	    _outPortMsgs = new double[numPorts][];
	    _savedOutMsgArray = new double[numPorts][];
	    for (int port = 0; port < numPorts; port++)
	    {
	    	_inPortMsgs[port] = (double[])_factor.getPorts().get(port).getInputMsg();
	    	_outPortMsgs[port] = (double[])_factor.getPorts().get(port).getOutputMsg();
	    	_savedOutMsgArray[port] = new double[_outPortMsgs[port].length];
	    }
    }
    
    
	public void setDamping(int index, double val)
	{
		_dampingParams[index] = val;
	}
	
	public double getDamping(int index)
	{
		return _dampingParams[index];
	}


	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for MinSum");
	}

	@Override
	public ArrayList<Port> getPorts() 
	{
		return getFactor().getPorts();
	}

	@Override
	public FactorFunction getFactorFunction() 
	{
		return getFactor().getFactorFunction();
	}

	@Override
	public double initAccumulator() 
	{
		return 0;
	}

	@Override
	public double accumulate(double oldVal, double newVal) 
	{
		return oldVal + newVal;
	}

	@Override
	public double combine(double oldVal, double newVal) 
	{
		if (oldVal < newVal)
			return oldVal;
		else
			return newVal;
	}

	@Override
	public void normalize(double[] outputMsg) 
	{
		double minVal = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < outputMsg.length; i++)
			if (outputMsg[i] < minVal)
				minVal = outputMsg[i];
		
		for (int i = 0; i < outputMsg.length; i++)
			outputMsg[i] -= minVal;
	}

	@Override
	public double evalFactorFunction(Object[] inputs) 
	{
		return -Math.log(getFactorFunction().eval(inputs));
	}

	@Override
	public void initMsg(double[] msg) 
	{
		for (int i = 0; i < msg.length; i++)
			msg[i] = Double.POSITIVE_INFINITY;
	}

	@Override
	public double getFactorTableValue(int index) 
	{
		return getFactorTable().getPotentials()[index];
	}
	
	@Override
	public int[] findKBestForMsg(double[] msg, int k) 
	{
		return Sort.quickfindFirstKindices(msg, k);
	}
}

