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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.Arrays;
import java.util.Objects;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.Nullable;

public class SDiscreteVariable extends SDiscreteVariableDoubleArray
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
    double [][] _logInPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    double [][] _savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    double [][] _outPortDerivativeMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    double [] _dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    private boolean _calculateDerivative = false;
	protected boolean _dampingInUse = false;
    @Nullable private double [][][] _outMessageDerivative;

	public SDiscreteVariable(VariableBase var)
    {
		super(var);
		
		if (!var.getDomain().isDiscrete())
			throw new DimpleException("only discrete variables supported");
	}
		
	@Override
	public void initialize()
	{
		super.initialize();

		configureDampingFromOptions();
	}

	public VariableBase getVariable()
	{
		return _var;
	}
	
	public void setCalculateDerivative(boolean val)
	{
		_calculateDerivative = val;
	}


	
	
	@Override
	public double getScore()
	{
		if (!_var.hasFixedValue())
			return -Math.log(_input[getGuessIndex()]);
		else
			return 0;	// If the value is fixed, ignore the guess
	}
	
	@Deprecated
	public void setDamping(int portIndex,double dampingVal)
	{
		double[] params  = SumProductOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && dampingVal != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[portIndex] = dampingVal;
		}
		
		SumProductOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}
	
	public double getDamping(int portIndex)
	{
		if (portIndex >= _dampingParams.length)
			return 0;
		else
			return _dampingParams[portIndex];
	}

    @Override
	protected void doUpdateEdge(int outPortNum)
    {
    	
        final double minLog = -100;
        double[] priors = _input;
        int M = priors.length;
        int D = _var.getSiblingCount();
        double maxLog = Double.NEGATIVE_INFINITY;

        double[] outMsgs = _outputMessages[outPortNum];

        if (_dampingInUse)
        {
        	double damping = _dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		double[] saved = _savedOutMsgArray[outPortNum];
        		for (int i = 0; i < outMsgs.length; i++)
        			saved[i] = outMsgs[i];
        	}
        }

        
        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double out = (prior == 0) ? minLog : Math.log(prior);
        	
	        for (int d = 0; d < D; d++)
	        {
	        	if (d != outPortNum)		// For all ports except the output port
	        	{
	        		double tmp = _inputMessages[d][m];
	        		out += (tmp == 0) ? minLog : Math.log(tmp);
	        	}
	        }
        	if (out > maxLog) maxLog = out;
        	outMsgs[m] = out;
        }
        
        //create sum
        double sum = 0;
        for (int m = 0; m < M; m++)
        {
        	double out = Math.exp(outMsgs[m] - maxLog);
        	outMsgs[m] = out;
        	sum += out;
        }
        
        //calculate message by dividing by sum
        for (int m = 0; m < M; m++)
        	outMsgs[m] /= sum;

        if (_dampingInUse)
        {
        	double damping = _dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		double[] saved = _savedOutMsgArray[outPortNum];
        		for (int m = 0; m < M; m++)
        			outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
        	}
        }
	    
        if (_calculateDerivative)
        	updateDerivative(outPortNum);
        
    }
    

    @Override
	protected void doUpdate()
    {
        final double minLog = -100;
        double[] priors = _input;
        int M = priors.length;
        int D = _var.getSiblingCount();
        
        
        //Compute alphas
        double[] alphas = new double[M];
        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double alpha = (prior == 0) ? minLog : Math.log(prior);

        	for (int d = 0; d < D; d++)
	        {
	        	double tmp = _inputMessages[d][m];
        		double logtmp = (tmp == 0) ? minLog : Math.log(tmp);
        		_logInPortMsgs[d][m] = logtmp;
        		alpha += logtmp;
	        }
	        alphas[m] = alpha;
        }
        
        
        //Now compute output messages for each outgoing edge
	    for (int out_d = 0; out_d < D; out_d++ )
	    {
            double[] outMsgs = _outputMessages[out_d];
            

            if (_dampingInUse)
            {
            	double damping = _dampingParams[out_d];
            	if (damping != 0)
            	{
            		double[] saved = _savedOutMsgArray[out_d];
            		for (int i = 0; i < outMsgs.length; i++)
            			saved[i] = outMsgs[i];
            	}
            }
            
            
            double maxLog = Double.NEGATIVE_INFINITY;
            
            //set outMsgs to alpha - mu_d,m
            //find max alpha
            double[] logInPortMsgsD = _logInPortMsgs[out_d];
            for (int m = 0; m < M; m++)
            {
            	double out = alphas[m] - logInPortMsgsD[m];
                if (out > maxLog) maxLog = out;
                outMsgs[m] = out;
            }
            
            //create sum
            double sum = 0;
            for (int m = 0; m < M; m++)
            {
                double out = Math.exp(outMsgs[m] - maxLog);
                outMsgs[m] = out;
                sum += out;
            }
            
            //calculate message by dividing by sum
            for (int m = 0; m < M; m++)
            {
            	outMsgs[m] /= sum;
            }
            
            
            if (_dampingInUse)
            {
            	double damping = _dampingParams[out_d];
            	if (damping != 0)
            	{
            		double[] saved = _savedOutMsgArray[out_d];
            		for (int m = 0; m < M; m++)
            			outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
            	}
            }
            
	    }
	   
	    if (_calculateDerivative)
		    for (int i = 0; i < _inputMessages.length; i++)
		    	updateDerivative(i);
	    
    }
    
        
    @Override
	public double[] getBelief()
    {

        final double minLog = -100;
        double[] priors = _input;
        int M = priors.length;
        int D = _var.getSiblingCount();
        double maxLog = Double.NEGATIVE_INFINITY;

        
        double[] outBelief = new double[M];

        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double out = (prior == 0) ? minLog : Math.log(prior);
        	
	        for (int d = 0; d < D; d++)
	        {
	        	double tmp = _inputMessages[d][m];
	        	out += (tmp == 0) ? minLog : Math.log(tmp);
	        }
        	if (out > maxLog) maxLog = out;
        	outBelief[m] = out;
        }
        
        //create sum
        double sum = 0;
        for (int m = 0; m < M; m++)
        {
        	double out = Math.exp(outBelief[m] - maxLog);
        	outBelief[m] = out;
        	sum += out;
        }
        
        //calculate belief by dividing by sum
        for (int m = 0; m < M; m++)
        {
        	outBelief[m] /= sum;
        }
        
        
        return outBelief;
    }
    
	public double [] getNormalizedInputs()
	{
		double [] tmp = new double [_input.length];
		double sum = 0;
		for (int i = 0; i < _input.length; i++)
			sum += _input[i];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = _input[i]/sum;
		
		return tmp;
	}

	public double [] getUnormalizedBelief()
	{
		//TODO: log regime
		double [] input = getNormalizedInputs();
		
		double [] retval = new double[_input.length];
		for (int i = 0; i <  retval.length ; i++)
			retval[i] = input[i];
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			for (int j=  0; j < retval.length; j++)
			{
				retval[j] *= _inputMessages[i][j];
			}
		}
		
		return retval;
			
	}
	
	/******************************************************
	 * Energy, Entropy, and derivatives of all that.
	 ******************************************************/
	
	@Override
	public double getInternalEnergy()
	{
		int domainLength = ((DiscreteDomain)_var.getDomain()).size();
		double sum = 0;
		
		double [] belief = getBelief();
		double [] input = _input;
		
		//make sure input is normalized
		double norm = 0;
		for (int i = 0; i < input.length; i++)
			norm += input[i];
		
		for (int i = 0; i < domainLength; i++)
		{
			double tmp = input[i]/norm;
			if (tmp != 0)
				sum += belief[i] * (- Math.log(tmp));
		}
		
		return sum;
	}

	@Override
	public double getBetheEntropy()
	{
		double sum = 0;
		
		double [] belief = getBelief();
		for (int i = 0; i < belief.length; i++)
		{
			if (belief[i] != 0)
				sum -= belief[i] * Math.log(belief[i]);
		}
		
		return sum;
	}
	
	public double calculatedf(double f, int weightIndex, int domain)
	{
		double sum = 0;
		for (int i = 0; i < _inputMessages.length; i++)
		{
			STableFactor sft = (STableFactor)getVariable().getConnectedNodesFlat().getByIndex(i).getSolver();
			double inputMsg = _inputMessages[i][domain];
			double tmp = f / inputMsg;
			@SuppressWarnings("null")
			double der = sft.getMessageDerivative(weightIndex,getVariable())[domain];
			tmp = tmp * der;
			sum += tmp;
		}
		return sum;
	}
	
	
	public double calculateDerivativeOfBelief(int weightIndex, int domain)
	{
		double [] un = getUnormalizedBelief();
		//Calculate unormalized belief
		double f = un[domain];
		double g = 0;
		for (int i = 0; i < _input.length; i++)
			g += un[i];
		
		double df = calculatedf(f,weightIndex,domain);
		double dg = 0;
		for (int i = 0; i < _input.length; i++)
		{
			double tmp = un[i];
			dg += calculatedf(tmp,weightIndex,i);
		}
		
		//return df;
		return (df*g - f*dg)/(g*g);
	}
	
	public double calculateDerivativeOfInternalEnergyWithRespectToWeight(int weightIndex)
	{
		double sum = 0;

		//double [] belief = (double[])getBelief();
		double [] input = getNormalizedInputs();
		
		//for each domain
		for (int d = 0; d < _input.length; d++)
		{
			//calculate belief(d)
			//double beliefd = belief[d];
			
			//calculate input(d)
			double inputd = input[d];
			
			//get derviativebelief(d,weightindex)
			double dbelief = calculateDerivativeOfBelief(weightIndex,d);
			
			sum += dbelief * (-Math.log(inputd));
		}
		return sum;
	}
	public double calculateDerivativeOfBetheEntropyWithRespectToWeight(int weightIndex)
	{
		double sum = 0;
		
		double [] belief = getBelief();
		
		for (int d = 0; d < _input.length; d++)
		{
			double beliefd = belief[d];
			double dbelief = calculateDerivativeOfBelief(weightIndex, d);
			
			sum += dbelief * (Math.log(beliefd)) + dbelief;
		}
		
		return -sum * (_inputMessages.length-1);
	}


    private double calculateProdFactorMessagesForDomain(int outPortNum, int d)
    {
		double f = getNormalizedInputs()[d];
		for (int i = 0; i < _inputMessages.length; i++)
		{
			if (i != outPortNum)
			{
				f *= _inputMessages[i][d];
			}
		}
		return f;
    }
    
    public void updateDerivativeForWeightNumAndDomainItem(int outPortNum, int weight, int d)
    {
    	//calculate f
    	double f = calculateProdFactorMessagesForDomain(outPortNum,d);
    	
    	//calculate g
    	double g = 0;
    	for (int i = 0; i < _input.length; i++)
    		g += calculateProdFactorMessagesForDomain(outPortNum, i);
    	
    	double derivative = 0;
    	if (g != 0)
    	{
	    	//calculate df
	    	double df = calculatedf(outPortNum,f,d,weight);
	    	
	    	//calculate dg
	    	double dg = calculatedg(outPortNum,weight);
	    	
	    	derivative = (df*g - f*dg)/(g*g);
    	}
    	
    	Objects.requireNonNull(_outMessageDerivative)[weight][outPortNum][d] = derivative;
    }
    
    public double calculatedg(int outPortNum,int wn)
    {
    	double sum = 0;
    	for (int d = 0; d < _input.length; d++)
    	{
    		double prod = calculateProdFactorMessagesForDomain(outPortNum, d);
    		sum += calculatedf(outPortNum,prod,d,wn);
    	}
    	
    	return sum;
    }

	public void initializeDerivativeMessages(int weights)
	{
		_outMessageDerivative = new double[weights][_inputMessages.length][_input.length];
	}
    
    public double [] getMessageDerivative(int wn, Factor f)
    {
    	int portNum = getVariable().getPortNum(f);
    	return Objects.requireNonNull(_outMessageDerivative)[wn][portNum];
    }
    
    public double calculatedf(int outPortNum, double f, int d, int wn)
    {
    	double df = 0;
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			if (i != outPortNum)
			{
				double thisMsg = _inputMessages[i][d];
				STableFactor stf = (STableFactor)getVariable().getConnectedNodesFlat().getByIndex(i).getSolver();
				@SuppressWarnings("null")
				double [] dfactor = stf.getMessageDerivative(wn,getVariable());
				
				df += f/thisMsg * dfactor[d];
			}
		}
		return df;
    }
    
    public void updateDerivativeForWeightNum(int outPortNum, int weight)
    {
    	for (int d = 0; d < _input.length; d++)
    	{
    		updateDerivativeForWeightNumAndDomainItem(outPortNum,weight,d);
    	}
    }
    
    public void updateDerivative(int outPortNum)
    {
    	SFactorGraph sfg = (SFactorGraph)getRootGraph();
    	@SuppressWarnings("null")
		int numWeights = sfg.getCurrentFactorTable().sparseSize();
    	
    	for (int wn = 0; wn < numWeights; wn++)
    	{
    		updateDerivativeForWeightNum(outPortNum, wn);
    	}
    }

	@Override
	public Object [] createMessages(ISolverFactor factor)
	{
		Object [] retval = super.createMessages(factor);
		int portNum = _var.getPortNum(Objects.requireNonNull(factor.getModelObject()));
		int newArraySize = _inputMessages.length;
		_logInPortMsgs = Arrays.copyOf(_logInPortMsgs, newArraySize);
		_logInPortMsgs[portNum] = new double[_inputMessages[portNum].length];
		
//		if (_dampingInUse)
//		{
//			_savedOutMsgArray = Arrays.copyOf(_savedOutMsgArray,newArraySize);
//			_savedOutMsgArray[portNum] = new double[_inputMessages[portNum].length];
//		}
//
//		_dampingParams = Arrays.copyOf(_dampingParams, newArraySize);
		
		return retval;
	}
	
	@Override
	public double[] resetInputMessage(Object message)
	{
    	double [] retval = (double[])message;
    	int domainLength = retval.length;
    	double val = 1.0/domainLength;
    	
    	Arrays.fill(retval, val);
    	return retval;
	}

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPortNum)
	{
		super.moveMessages(other, portNum, otherPortNum);
		
		SDiscreteVariable sother = (SDiscreteVariable)other;
		_logInPortMsgs[portNum] = sother._logInPortMsgs[otherPortNum];
		
		if (_dampingInUse)
		{
			_savedOutMsgArray[portNum] = sother._savedOutMsgArray[otherPortNum];
		}
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected DiscreteWeightMessage cloneMessage(int edge)
	{
		return new DiscreteWeightMessage(_outputMessages[edge]);
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}
	
	/*-----------------
	 * Private methods
	 */
	
    private void configureDampingFromOptions()
    {
     	final int size = getSiblingCount();
    	
    	_dampingParams =
    		getReplicatedNonZeroListFromOptions(SumProductOptions.nodeSpecificDamping, SumProductOptions.damping,
    			size, _dampingParams);
 
    	if (_dampingParams.length > 0 && _dampingParams.length != size)
    	{
			// TODO: use a logging API instead?
			System.err.format("ERROR: %s has wrong number of parameters for %s\n",
				SumProductOptions.nodeSpecificDamping, this);
    		_dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	}
    	
    	_dampingInUse = _dampingParams.length > 0;
    	
    	if (!_dampingInUse)
    	{
    		_savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    	}
    	else if (_savedOutMsgArray.length != size)
    	{
    		_savedOutMsgArray = new double[size][];
    		for (int i = 0; i < size; i++)
    	    {
    			_savedOutMsgArray[i] = new double[_inputMessages[i].length];
    	    }
    	}
    }
}
