package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;

import com.analog.lyric.cs.Sort;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
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
	double [][] _inPortMsgs;
	double [][] _outMsgArray;
	double [][] _savedOutMsgArray;
	double [] _dampingParams;
	boolean _initCalled = true;
	TableFactorEngine _tableFactorEngine;
	KBestFactorEngine _kbestFactorEngine;
	private int _k;
	private boolean _kIsSmallerThanDomain = false;
	

	public STableFactor(Factor factor)  
	{
		super(factor);
		_dampingParams = new double[_factor.getPorts().size()];
		_tableFactorEngine = new TableFactorEngine(this);
			
		//TODO: should I recheck for factor table every once in a while?
		if (factor.getFactorFunction().factorTableExists(getFactor().getDomains()))
			_kbestFactorEngine = new KBestFactorTableEngine(this);
		else
			_kbestFactorEngine = new KBestFactorEngine(this);
		
		setK(Integer.MAX_VALUE);

	}
	
	public void setDamping(int index, double val)
	{
		_dampingParams[index] = val;
	}
	
	public double getDamping(int index)
	{
		return _dampingParams[index];
	}
	
	public void setK(int k)
	{
		updateCache();
		
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

	public double getEnergy()
	{
		int [] indices = new int[_factor.getPorts().size()];
		
		for (int i = 0; i < indices.length; i++)
		{
			SVariable tmp = (SVariable)((VariableBase)_factor.getPorts().get(i).getConnectedNode()).getSolver();
			indices[i] = tmp.getGuessIndex();
		}
		
		 int[][] table = getFactorTable().getIndices();
	     double[] values = getFactorTable().getWeights();
	     double maxValue = Double.NEGATIVE_INFINITY;
	     double retVal = Double.POSITIVE_INFINITY;

	     
		 for (int i = 0; i < table.length; i++)
		 {
			 boolean match = true;
			 
			 for (int j = 0; j < indices.length; j++)
			 {
				 if (indices[j] != table[i][j])
				 {
					 match = false;
					 break;
				 }
			 }
			 
			 if (match)
			 {
				 retVal = -Math.log(values[i]);
			 }
			 
			 if (values[i] > maxValue)
				 maxValue = values[i];
		 }		
		 
		 if (maxValue > 0)
			 retVal -= -Math.log(maxValue);
		
		return retVal;
		
	}
	
	public void updateEdge(int outPortNum) 
	{
		updateCache();
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.updateEdge(outPortNum);
		else
			_tableFactorEngine.updateEdge(outPortNum);

	}
	
	
	@Override
	public void update() 
	{		
		updateCache();	
		if (_kIsSmallerThanDomain)
			//TODO: damping
			_kbestFactorEngine.update();
		else
			_tableFactorEngine.update();
		
	}
	
	@Override
	public void connectPort(Port p)  
	{
		// TODO Auto-generated method stub
		_initCalled = true;
		
	}

	private void updateCache()
	{
		if (_initCalled)
		{
			_initCalled = false;
			int numPorts = _factor.getPorts().size();
			
		    _inPortMsgs = new double[numPorts][];
		    
		    for (int port = 0; port < numPorts; port++) 
		    	_inPortMsgs[port] = (double[])_factor.getPorts().get(port).getInputMsg();
		    
		    _outMsgArray = new double[numPorts][];
		    _savedOutMsgArray = new double[numPorts][];
		    for (int port = 0; port < numPorts; port++)
		    {
		    	_outMsgArray[port] = (double[])_factor.getPorts().get(port).getOutputMsg();
		    	_savedOutMsgArray[port] = new double[_outMsgArray[port].length];
		    }
		}
	}
	
	public void initialize()
	{
		//We update the cache here.  This works only because initialize() is called on the variables
		//first.  Updating the cache saves msg in double arrays.  initialize replaces these double arrays
		//with new double arrays.  If we didn't call updateCache on initialize, our cache would point
		//to stale information.
		_initCalled = true;
	}
	
	
	public double [] getBelief() 
	{
		updateCache();
		
		//throw new DimpleException("not supported");
		int [][] table = getFactorTable().getIndices();
		double [] values = getFactorTable().getWeights();
		double [] retval = new double[table.length];
		
		double sum = 0;
		
		for (int i = 0; i < table.length; i++)
		{
			retval[i] = values[i];
			for (int j = 0; j < table[i].length; j++)
			{
				retval[i] *= _inPortMsgs[j][table[i][j]];
			}
			sum += retval[i];
		}
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] /= sum;
		}
		
		return retval;
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
		return 1;
	}

	@Override
	public double accumulate(double oldVal, double newVal) 
	{
		return oldVal*newVal;
	}

	@Override
	public double combine(double oldVal, double newVal) 
	{
		// TODO Auto-generated method stub
		return oldVal+newVal;
	}

	@Override
	public void normalize(double[] outputMsg) 
	{
		double sum = 0;
		for (int i = 0; i < outputMsg.length; i++)
			sum += outputMsg[i];
		
		if (sum == 0)
			throw new DimpleException("Update failed in SumProduct Solver.  All probabilities were zero when calculating message for port " 
					+ " on factor " +_factor.getLabel());

    	for (int i = 0; i < outputMsg.length; i++) 
    		
    		outputMsg[i] /= sum;
	}

	@Override
	public double evalFactorFunction(Object[] inputs) 
	{
		return getFactor().getFactorFunction().eval(inputs);
	}

	@Override
	public void initMsg(double[] msg) 
	{
		for (int i = 0; i < msg.length;i++)
			msg[i] = 0;
	}

	@Override
	public double getFactorTableValue(int index) 
	{
		return getFactorTable().getWeights()[index];
	}

	@Override
	public int[] findKBestForMsg(double[] msg, int k) 
	{
		return Sort.quickfindLastKindices(msg, k);
	}

}
