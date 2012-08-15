package com.analog.lyric.dimple.solvers.core;

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.util.misc.IndexCounter;

public class KBestFactorEngine 
{
	//TODO: do I have to be careful of caching
	private ArrayList<Port> _ports;
	private int _k;
	private IKBestFactor _kbestFactor;
	private double [][] _outPortMsgs;
	private double [][] _inPortMsgs;
	
	public interface IKBestFactor
	{
		ArrayList<Port> getPorts();
		FactorFunction getFactorFunction();
		double initAccumulator();
		double accumulate(double oldVal,double newVal);
		double combine(double oldVal,double newVal);
		void normalize(double [] outputMsg);
		FactorTable getFactorTable();
		double evalFactorFunction(Object [] inputs);
		void initMsg(double [] msg);
		double getFactorTableValue(int index);
	}
	
	public KBestFactorEngine(IKBestFactor f)
	{
		_kbestFactor = f;
		_ports = f.getPorts();
		
		_inPortMsgs = new double[_ports.size()][];
		_outPortMsgs = new double[_ports.size()][];
		
		for (int i = 0; i < _ports.size(); i++)
		{
			_inPortMsgs[i] = (double[])_ports.get(i).getInputMsg();
			_outPortMsgs[i] = (double[])_ports.get(i).getOutputMsg();
		}
	}
	
	public void update()
	{
		for (int i = 0; i < _ports.size(); i++)
			updateEdge(i);
	}
	
	public void setK(int k)
	{
		_k = k;
	}
	
	
	/*
	 * Code for updating given no factor table but java factor function
	 */
	public void updateEdge(int outPortNum)
	{
		
		//Get the outputPort we're going to update.
		Port outPort = _ports.get(outPortNum);
		
		//Initialize the outputMsg to Infinite potentials.
		double [] outputMsg = (double[])outPort.getOutputMsg();
		_kbestFactor.initMsg(outputMsg);

		//Cache the input messages.
		double [][] inputMsgs = new double[_ports.size()][];
		for (int i = 0; i < _ports.size(); i++)
			inputMsgs[i] = (double[])_ports.get(i).getInputMsg();
		
		//Cache the domains
		Object [][] domains = new Object[_ports.size()][];
		for (int i = 0; i < _ports.size(); i++)
			domains[i] = ((Discrete)_ports.get(i).getConnectedNode()).getDiscreteDomain().getElements();
		
		//We will fill this object with inputs to the factor function later on.
		Object [] ffInput = new Object[_ports.size()];
		
		//We will store the kbest indices in this array
		int [][] domainIndices = new int[_ports.size()][];
		
		//We will store the truncated domainlengths here.
		int [] domainLengths = new int[_ports.size()];
		
		//For each port
		for (int i = 0; i < _ports.size(); i++)
		{
			double [] inPortMsg = inputMsgs[i];

			//If this is the output port, we only store one value at a time.
			if (i == outPortNum)
				domainIndices[i] = new int[]{0};
			else
			{
				//Here we check to see that k is actually less than the domain length
				if (_k < inPortMsg.length)
					domainIndices[i] = com.analog.lyric.cs.Sort.quickfindFirstKindices(inPortMsg, _k);
				else
				{
					//If it's not, we just map indices one to one.
					domainIndices[i] = new int[inPortMsg.length];
					for (int j = 0; j < domainIndices[i].length; j++)
						domainIndices[i][j] = j;
				}
			}
			
			domainLengths[i] = domainIndices[i].length;
		}
		
		//cache the factor function.
		//FactorFunction ff = _kbestFactor.getFactorFunction();
		

		//Used to iterate all combinations of truncated domains.
		IndexCounter ic = new IndexCounter(domainLengths);

		int [] inputIndices = new int[_ports.size()];
		
		//We fill out a value for every value for the output message (no truncating to k)
		for (int outputIndex = 0; outputIndex < outputMsg.length; outputIndex++)
		{
			//Here we set the output port's index appropriately
			domainIndices[outPortNum][0] = outputIndex;
		

			//For all elements of cartesian product			
			for (int [] indices : ic)
			{
				//initialize the sum
				double sum = _kbestFactor.initAccumulator();
				
				for (int i = 0; i < indices.length; i++)
				{
					//Don't count the output port
					if (i != outPortNum)
						//i == port index, indices[i] == which of the truncated domain indices to retrieve
						//domainIndices[i][indices[i]] == the actual index of the input msg.
						sum = _kbestFactor.accumulate(sum, inputMsgs[i][domainIndices[i][indices[i]]]);

					//Here we set the input value for this port
					inputIndices[i] = domainIndices[i][indices[i]];
					//ffInput[i] =  domains[i][domainIndices[i][indices[i]]];
				}
				
				//Evaluate the factor function and add that potential to the sum.
				double result = getFactorFunctionValueForIndices(inputIndices,domains);
				sum = _kbestFactor.accumulate(sum, result);

				outputMsg[outputIndex] = _kbestFactor.combine(outputMsg[outputIndex] , sum);
			}
		}

		_kbestFactor.normalize(outputMsg);
		
	}
	
	protected double getFactorFunctionValueForIndices(int [] inputIndices, Object [][] domains)
	{
		Object [] ffInput = new Object[inputIndices.length];
		for (int i = 0; i < ffInput.length; i++)
			ffInput[i] = domains[i][inputIndices[i]];
		return _kbestFactor.evalFactorFunction(ffInput);		
	}

	protected IKBestFactor getIKBestFactor()
	{
		return _kbestFactor;
	}

}
