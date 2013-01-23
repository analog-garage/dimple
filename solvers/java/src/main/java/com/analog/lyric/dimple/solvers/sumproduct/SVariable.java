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

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;

public class SVariable extends SDiscreteVariableBase
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */	
    double [][] _inPortMsgs;
    double [][] _logInPortMsgs;
    double [][] _outMsgArray;
    double [][] _savedOutMsgArray;    
    double [][] _outPortDerivativeMsgs;
    double [] _dampingParams = new double[0];
    protected double [] _input;
    private boolean _calculateDerivative = false;
	protected boolean _dampingInUse = false;

	public SVariable(VariableBase var)  
    {
		super(var);
		
		if (!var.getDomain().isDiscrete())
			throw new DimpleException("only discrete variables supported");
		initializeInputs();
		
	}
	
	public void initializeInputs()
	{
		_input = (double[])getDefaultMessage(null);
		
	}
	
	public VariableBase getVariable()
	{
		return _var;
	}
	
	public void setCalculateDerivative(boolean val)
	{
		_calculateDerivative = val;
	}

	public Object getDefaultMessage(Port port) 
	{
		//TODO: both variable and factor do this.  Why doesn't factor just ask variable?
		int domainLength = ((DiscreteDomain)_var.getDomain()).size();
    	double[] retVal = new double[domainLength];
    	double val = 1.0/domainLength;
    	Arrays.fill(retVal, val);
    	return retVal;
    }
	
	public double getScore()
	{
		return -Math.log(_input[getGuessIndex()]);
	}
	
    public void setInput(Object priors) 
    {
    	double[] vals = (double[])priors;
    	if (vals.length != ((DiscreteDomain)_var.getDomain()).size())
    		throw new DimpleException("length of priors does not match domain");
    	
    	_input = vals;
    	
    }
	public void setDamping(int portIndex,double dampingVal)
	{
		if (portIndex >= _dampingParams.length)
		{
			double [] tmp = new double [portIndex+1];
			for (int i = 0; i < _dampingParams.length; i++)				
				tmp[i] = _dampingParams[i];
			
			_dampingParams = tmp;
		}

		_dampingParams[portIndex] = dampingVal;
		
		if (dampingVal != 0)
			_dampingInUse = true;
	}
	
	public double getDamping(int portIndex)
	{
		if (portIndex >= _dampingParams.length)
			return 0;
		else
			return _dampingParams[portIndex];
	}
	

    public void updateEdge(int outPortNum) 
    {
    	ensureCacheUpdated();
    	
        final double minLog = -100;
        double[] priors = (double[])_input;
        int M = priors.length;
        int D = _var.getPorts().size();
        double maxLog = Double.NEGATIVE_INFINITY;

        double[] outMsgs = _outMsgArray[outPortNum];

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
	        		double tmp = _inPortMsgs[d][m];
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
    

    public void update() 
    {
    	ensureCacheUpdated();

        final double minLog = -100;
        double[] priors = (double[])_input;
        int M = priors.length;
        int D = _var.getPorts().size();
        
        
        //Compute alphas
        double[] alphas = new double[M];
        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double alpha = (prior == 0) ? minLog : Math.log(prior);

        	for (int d = 0; d < D; d++)
	        {
	        	double tmp = _inPortMsgs[d][m];
        		double logtmp = (tmp == 0) ? minLog : Math.log(tmp);
        		_logInPortMsgs[d][m] = logtmp;
        		alpha += logtmp;
	        }
	        alphas[m] = alpha;
        }
        
        //Now compute output messages for each outgoing edge
	    for (int out_d = 0; out_d < D; out_d++ )
	    {
            double[] outMsgs = _outMsgArray[out_d];
            

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
		    for (int i = 0; i < _inPortMsgs.length; i++)
		    	updateDerivative(i);
    }
    
        
    public Object getBelief()
    {
    	ensureCacheUpdated();

        final double minLog = -100;
        double[] priors = (double[])_input;
        int M = priors.length;
        int D = _var.getPorts().size();
        double maxLog = Double.NEGATIVE_INFINITY;

        double[] outBelief = new double[M];

        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double out = (prior == 0) ? minLog : Math.log(prior);
        	
	        for (int d = 0; d < D; d++)
	        {
	        	double tmp = _inPortMsgs[d][m];
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
        	outBelief[m] /= sum;
        
        return outBelief;
    }
    
    

    
    protected void updateMessageCache()
    {
    	int D = _var.getPorts().size();    	
		int M = ((double[])_input).length;
					
		_inPortMsgs = new double[D][];
		_logInPortMsgs = new double[D][M];
		_outMsgArray = new double[D][];
		if (_dampingInUse)
			_savedOutMsgArray = new double[D][];

	    if (_dampingParams.length != D)
	    {
	    	double [] tmp = new double[D];
	    	for (int i = 0; i < _dampingParams.length; i++)
	    	{
	    		if (i < tmp.length)
	    			tmp[i] = _dampingParams[i];
	    	}
	    	_dampingParams = tmp;
	    }
	    		    
	    for (int d = 0; d < D; d++) 
	    	_inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();
		
        //Now compute output messages for each outgoing edge
	    for (int out_d = 0; out_d < D; out_d++ )
	    {
            _outMsgArray[out_d] = (double[])_var.getPorts().get(out_d).getOutputMsg();
			if (_dampingInUse)
				_savedOutMsgArray[out_d] = new double[_outMsgArray[out_d].length];
	    }
	    
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
		
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			for (int j=  0; j < retval.length; j++)
			{
				retval[j] *= _inPortMsgs[i][j];
			}
		}
		
		return retval;
			
	}
	
	/******************************************************
	 * Energy, Entropy, and derivatives of all that. 
	 ******************************************************/
	
	public double getInternalEnergy()
	{
		int domainLength = ((DiscreteDomain)_var.getDomain()).size();
		double sum = 0;
		
		double [] belief = (double[])getBelief();
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

	public double getBetheEntropy()
	{
		double sum = 0;
		
		double [] belief = (double[])getBelief();
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
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			STableFactor sft = (STableFactor)getVariable().getPorts().get(i).getConnectedNode().getSolver();
			double inputMsg = _inPortMsgs[i][domain];
			double tmp = f / inputMsg;
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
		
		double [] belief = (double[])getBelief();
		
		for (int d = 0; d < _input.length; d++)
		{
			double beliefd = belief[d];
			double dbelief = calculateDerivativeOfBelief(weightIndex, d);
			
			sum += dbelief * (Math.log(beliefd)) + dbelief;
		}
		
		return -sum * (_inPortMsgs.length-1);
	}


    private double calculateProdFactorMessagesForDomain(int outPortNum, int d)
    {
		double f = getNormalizedInputs()[d];
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			if (i != outPortNum)
			{
				f *= _inPortMsgs[i][d];
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
    	
    	_outMessageDerivative[weight][outPortNum][d] = derivative;
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

    double [][][] _outMessageDerivative;

	public void initializeDerivativeMessages(int weights)
	{
		_outMessageDerivative = new double[weights][_inPortMsgs.length][_input.length];
	}
    
    public double [] getMessageDerivative(int wn, Factor f)
    {
    	int portNum = getVariable().getPortNum(f);
    	return _outMessageDerivative[wn][portNum];
    }
    
    public double calculatedf(int outPortNum, double f, int d, int wn)
    {
    	double df = 0;
		
		int j = 0;
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			if (i != outPortNum)
			{
				double thisMsg = _inPortMsgs[i][d];
				STableFactor stf = (STableFactor)getVariable().getPorts().get(j).getConnectedNode().getSolver();
				double [] dfactor = stf.getMessageDerivative(wn,getVariable());
				
				df += f/thisMsg * dfactor[d]; 
			}
			j++;
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
    	int numWeights = sfg.getCurrentFactorTable().getWeights().length;
    	
    	for (int wn = 0; wn < numWeights; wn++)
    	{
    		updateDerivativeForWeightNum(outPortNum, wn);
    	}
    }
	
}
