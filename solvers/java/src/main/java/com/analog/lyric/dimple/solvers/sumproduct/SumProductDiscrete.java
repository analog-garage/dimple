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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * Solver variable for Discrete variables under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductDiscrete extends SDiscreteVariableDoubleArray
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
    double [] _dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    private boolean _calculateDerivative = false;
	protected boolean _dampingInUse = false;
    @Nullable private double [][][] _outMessageDerivative;

	public SumProductDiscrete(Discrete var, ISolverFactorGraph parent)
    {
		super(var, parent);
	}
		
	@Override
	public void initialize()
	{
		super.initialize();

		configureDampingFromOptions();
	}

	// FIXME - reset edge messages from graph methods, not node
	@Override
	public void resetEdgeMessages(int portNum)
	{
		final SumProductDiscreteEdge edge = getEdge(portNum);
		edge.reset();
	}
	
	public Variable getVariable()
	{
		return _model;
	}
	
	public void setCalculateDerivative(boolean val)
	{
		_calculateDerivative = val;
	}


	
	
	@Override
	public double getScore()
	{
		if (!_model.hasFixedValue())
			return -Math.log(_input[getGuessIndex()]);
		else
			return 0;	// If the value is fixed, ignore the guess
	}
	
	@Deprecated
	public void setDamping(int portIndex,double dampingVal)
	{
		double[] params  = BPOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && dampingVal != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[portIndex] = dampingVal;
		}
		
		BPOptions.nodeSpecificDamping.set(this, params);
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
        int D = _model.getSiblingCount();
        double maxLog = Double.NEGATIVE_INFINITY;

		final SumProductDiscreteEdge edge = getEdge(outPortNum);

		final double[] outMsgs = edge.varToFactorMsg.representation();

        double[] savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY;
        	
        if (_dampingInUse)
        {
        	savedOutMsgArray = DimpleEnvironment.doubleArrayCache.allocate(M);
        	double damping = _dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		for (int m = 0; m < outMsgs.length; m++)
        			savedOutMsgArray[m] = outMsgs[m];
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
	        		double tmp = getEdge(d).factorToVarMsg.getWeight(m);
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
        		for (int m = 0; m < M; m++)
        			outMsgs[m] = outMsgs[m]*(1-damping) + savedOutMsgArray[m]*damping;
        	}
        	
        	DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
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
        int D = _model.getSiblingCount();
        
        
        //Compute alphas
        final double[] logInPortMsgs = DimpleEnvironment.doubleArrayCache.allocate(M*D);
        final double[] alphas = DimpleEnvironment.doubleArrayCache.allocate(M);
        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double alpha = (prior == 0) ? minLog : Math.log(prior);

        	for (int d = 0, i = m; d < D; d++, i += M)
	        {
	        	double tmp = getEdge(d).factorToVarMsg.getWeight(m);
        		double logtmp = (tmp == 0) ? minLog : Math.log(tmp);
        		logInPortMsgs[i] = logtmp;
        		alpha += logtmp;
	        }
	        alphas[m] = alpha;
        }
        
        
        //Now compute output messages for each outgoing edge
        final double[] savedOutMsgArray =
        	_dampingInUse ? DimpleEnvironment.doubleArrayCache.allocate(M) : ArrayUtil.EMPTY_DOUBLE_ARRAY;
	    for (int out_d = 0, dm = 0; out_d < D; out_d++, dm += M )
	    {
            final double[] outMsgs = getEdge(out_d).varToFactorMsg.representation();
            

            if (_dampingInUse)
            {
            	double damping = _dampingParams[out_d];
            	if (damping != 0)
            	{
            		for (int m = 0; m < M; m++)
            			savedOutMsgArray[m] = outMsgs[m];
            	}
            }
            
            
            double maxLog = Double.NEGATIVE_INFINITY;
            
            //set outMsgs to alpha - mu_d,m
            //find max alpha
            for (int m = 0; m < M; m++)
            {
            	double out = alphas[m] - logInPortMsgs[dm + m];
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
            		for (int m = 0; m < M; m++)
            			outMsgs[m] = outMsgs[m]*(1-damping) + savedOutMsgArray[m]*damping;
            	}
            }
            
	    }
	    
	    DimpleEnvironment.doubleArrayCache.release(logInPortMsgs);
	    DimpleEnvironment.doubleArrayCache.release(alphas);
	    if (savedOutMsgArray.length > 0)
	    {
	    	DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
	    }
	   
	    if (_calculateDerivative)
	    {
		    for (int i = 0; i < D; i++)
		    	updateDerivative(i);
	    }
	    
    }
    
        
    @Override
	public double[] getBelief()
    {

        final double[] priors = _input;
        final int M = priors.length;
        final int D = _model.getSiblingCount();

        final double minLog = -100;
        double maxLog = Double.NEGATIVE_INFINITY;
        
        double[] outBelief = new double[M];

        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double out = (prior == 0) ? minLog : Math.log(prior);
        	
	        for (int d = 0; d < D; d++)
	        {
	        	double tmp = getEdge(d).factorToVarMsg.getWeight(m);
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
		
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			final double[] inMsg = getEdge(i).factorToVarMsg.representation();
			for (int j=  0; j < retval.length; j++)
			{
				retval[j] *= inMsg[j];
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
		int domainLength = _model.getDomain().size();
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
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			SumProductTableFactor sft = (SumProductTableFactor)getSibling(i);
			double inputMsg = getEdge(i).factorToVarMsg.getWeight(domain);
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
		
		return -sum * (getSiblingCount()-1);
	}


    private double calculateProdFactorMessagesForDomain(int outPortNum, int d)
    {
		double f = getNormalizedInputs()[d];
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			if (i != outPortNum)
			{
				f *= getEdge(i).factorToVarMsg.getWeight(d);
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
		_outMessageDerivative = new double[weights][getSiblingCount()][_input.length];
	}
    
    public double [] getMessageDerivative(int wn, Factor f)
    {
    	int portNum = getVariable().getPortNum(f);
    	return Objects.requireNonNull(_outMessageDerivative)[wn][portNum];
    }
    
    public double calculatedf(int outPortNum, double f, int d, int wn)
    {
    	double df = 0;
		
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			if (i != outPortNum)
			{
				double thisMsg = getEdge(i).factorToVarMsg.getWeight(d);
				SumProductTableFactor stf = (SumProductTableFactor)getSibling(i);
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
    	SumProductSolverGraph sfg = (SumProductSolverGraph)getRootSolverGraph();
    	@SuppressWarnings("null")
		int numWeights = sfg.getCurrentFactorTable().sparseSize();
    	
    	for (int wn = 0; wn < numWeights; wn++)
    	{
    		updateDerivativeForWeightNum(outPortNum, wn);
    	}
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
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		super.moveMessages(other, portNum, otherPort);

		SumProductDiscrete sother = (SumProductDiscrete)other;
		
		if (_dampingInUse)
		{
			_dampingParams[portNum] = sother._dampingParams[otherPort];
		}
	}

	/*---------------
	 * SNode methods
	 */
	
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
    		getReplicatedNonZeroListFromOptions(BPOptions.nodeSpecificDamping, BPOptions.damping,
    			size, _dampingParams);
 
    	if (_dampingParams.length > 0 && _dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				BPOptions.nodeSpecificDamping, this);
    		_dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	}
    	
    	_dampingInUse = _dampingParams.length > 0;
    }

    @Override
	@SuppressWarnings("null")
	protected SumProductDiscreteEdge getEdge(int siblingIndex)
	{
		return (SumProductDiscreteEdge)super.getEdge(siblingIndex);
	}
}
