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

import static com.analog.lyric.math.Utilities.*;
import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Internal;

/**
 * Solver variable for Discrete variables under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductDiscrete extends SDiscreteVariableDoubleArray
{
	/*-------
	 * State
	 */
	
    private boolean _calculateDerivative = false;
    @Nullable private double [][][] _outMessageDerivative;
	protected @Nullable double[] _dampingParams = null;
	protected double[][] _inMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	protected double[][] _outMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;

	// TODO = move into flags
	private boolean _normalizeMessages = true;
	
    /*--------------
     * Construction
     */
    
	public SumProductDiscrete(Discrete var, ISolverFactorGraph parent)
    {
		super(var, parent);
	}
		
	@Override
	public void initialize()
	{
		super.initialize();

		final int nEdges = _model.getSiblingCount();
		if (nEdges != _inMsgs.length)
		{
			_inMsgs = new double[nEdges][];
			_outMsgs = new double[nEdges][];
		}
		for (int i = 0; i < nEdges; ++i)
		{
			SumProductDiscreteEdge edge = getSiblingEdgeState(i);
			_inMsgs[i] = edge.factorToVarMsg.representation();
			_outMsgs[i] = edge.varToFactorMsg.representation();
		}
		
		configureDampingFromOptions();
		_normalizeMessages = BPOptions.normalizeMessages.getOrDefault(this);
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
	public void setDamping(int siblingNumber,double dampingVal)
	{
		double[] params  = BPOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && dampingVal != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[siblingNumber] = dampingVal;
		}
		
		BPOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}
	
	public double getDamping(int siblingNumber)
	{
		final double[] dampingParams = _dampingParams;
		return dampingParams != null ? dampingParams[siblingNumber] : 0.0;
	}

    @Override
	protected void doUpdateEdge(int outPortNum)
    {
    	
        final double minLog = Double.NEGATIVE_INFINITY;//-100;
        double[] priors = _input;
        final int M = priors.length;
        final int D = _model.getSiblingCount();
        double maxLog = Double.NEGATIVE_INFINITY;

		final double[] outMsgs = _outMsgs[outPortNum];
		final double[][] inMsgs = _inMsgs;
		final double[] dampingParams = _dampingParams;
		final double damping = dampingParams != null ? dampingParams[outPortNum] : 0.0;

		DiscreteMessage outMsg = getSiblingEdgeState(outPortNum).varToFactorMsg;
		double denormalizer = outMsg.getEnergyDenormalizer();
		for (int d = 0; d < D; ++d)
		{
			if (d != outPortNum)
			{
				denormalizer += getSiblingEdgeState(d).factorToVarMsg.getEnergyDenormalizer();
			}
		}

		if (damping != 0.0)
		{
			// Save previous output for damping
			final double[] savedOutMsgArray = DimpleEnvironment.doubleArrayCache.allocateAtLeast(M);
			System.arraycopy(outMsgs,  0, savedOutMsgArray, 0, M);
 
			for (int m = M; --m>=0;)
			{
				double prior = priors[m];
				double out = (prior == 0) ? minLog : Math.log(prior);

				int d = D;
				while (--d > outPortNum)
				{
					double tmp = inMsgs[d][m];
					out += (tmp == 0) ? minLog : Math.log(tmp);
				}
				while (--d >= 0)
				{
					double tmp = inMsgs[d][m];
					out += (tmp == 0) ? minLog : Math.log(tmp);
				}
				maxLog = Math.max(maxLog, out);
				outMsgs[m] = out;
			}

			// convert from log domain
			double sum = 0.0;
			for (int m = M; --m>=0;)
			{
				double out = Math.exp(outMsgs[m] - maxLog);
				outMsgs[m] = out;
				sum += out;
			}

			if (_normalizeMessages)
			{
				// normalize
				denormalizer += weightToEnergy(sum);
				for (int m = M; --m>=0;)
				{
					outMsgs[m] /= sum;
				}
			}
			
			// Apply damping
			final double inverseDamping = 1.0 - damping;
    		for (int m = M; --m>=0;)
    			outMsgs[m] = outMsgs[m]*inverseDamping + savedOutMsgArray[m]*damping;
    		
    		// Release temp array
    		DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
		}
		else
		{
			for (int m = M; --m>=0;)
			{
				double prior = priors[m];
				double out = (prior == 0) ? minLog : Math.log(prior);

				int d = D;
				while (--d > outPortNum)
				{
					double tmp = inMsgs[d][m];
					out += (tmp == 0) ? minLog : Math.log(tmp);
				}
				while (--d >= 0)
				{
					double tmp = inMsgs[d][m];
					out += (tmp == 0) ? minLog : Math.log(tmp);
				}
				maxLog = Math.max(maxLog, out);
				outMsgs[m] = out;
			}

			// Convert from log domain
			double sum = 0.0;
			for (int m = M; --m>=0;)
			{
				double out = Math.exp(outMsgs[m] - maxLog);
				outMsgs[m] = out;
				sum += out;
			}

			if (_normalizeMessages)
			{
				denormalizer += weightToEnergy(sum);
				for (int m = M; --m>=0;)
				{
					outMsgs[m] /= sum;
				}
			}
		}

		getSiblingEdgeState(outPortNum).varToFactorMsg.setEnergyDenormalizer(denormalizer);

		if (_calculateDerivative)
        {
        	updateDerivative(outPortNum);
        }
    }

    @Override
	protected void doUpdate()
    {
        final double minLog = -100;
        final double[] priors = _input;
        final int M = priors.length;
        final int D = _model.getSiblingCount();
        
        //Compute alphas
        final double[][] inMsgs = _inMsgs;
        final double[] logInPortMsgs = DimpleEnvironment.doubleArrayCache.allocateAtLeast(M*D);
        final double[] alphas = DimpleEnvironment.doubleArrayCache.allocateAtLeast(M);
        for (int m = M; --m>=0;)
        {
        	double prior = priors[m];
        	double alpha = (prior == 0) ? minLog : Math.log(prior);

        	for (int d = 0, i = m; d < D; d++, i += M)
	        {
	        	double tmp = inMsgs[d][m];
        		double logtmp = (tmp == 0) ? minLog : Math.log(tmp);
        		logInPortMsgs[i] = logtmp;
        		alpha += logtmp;
	        }
	        alphas[m] = alpha;
        }
        
		final double[] dampingParams = _dampingParams;
		
		if (dampingParams != null)
		{
			final double[] savedOutMsgArray = DimpleEnvironment.doubleArrayCache.allocateAtLeast(M);
			
			for (int out_d = 0, dm = 0; out_d < D; out_d++, dm += M )
			{
				final double[] outMsgs = _outMsgs[out_d];

				final double damping = dampingParams[out_d];
				
				if (damping != 0)
				{
					System.arraycopy(outMsgs, 0, savedOutMsgArray, 0, M);
				}

				double maxLog = Double.NEGATIVE_INFINITY;

				//set outMsgs to alpha - mu_d,m
				//find max alpha
				for (int m = M; --m>=0;)
				{
					final double out = alphas[m] - logInPortMsgs[dm + m];
					maxLog = Math.max(maxLog, out);
					outMsgs[m] = out;
				}

				// convert from log domain
				for (int m = M; --m>=0;)
				{
					double out = Math.exp(outMsgs[m] - maxLog);
					outMsgs[m] = out;
				}

				// normalize
				if (_normalizeMessages)
				{
					getSiblingEdgeState(out_d).varToFactorMsg.normalize();
				}
				
				if (damping != 0)
				{
					final double inverseDamping = 1.0 - damping;
					for (int m = M; --m>=0;)
					{
						outMsgs[m] = outMsgs[m]*inverseDamping + savedOutMsgArray[m]*damping;
					}
				}

			}

			DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
		}
		else
		{
			for (int out_d = 0, dm = 0; out_d < D; out_d++, dm += M )
			{
				final double[] outMsgs = _outMsgs[out_d];

				double maxLog = Double.NEGATIVE_INFINITY;

				//set outMsgs to alpha - mu_d,m
				//find max alpha
				for (int m = M; --m>=0;)
				{
					final double out = alphas[m] - logInPortMsgs[dm + m];
					maxLog = Math.max(maxLog, out);
					outMsgs[m] = out;
				}

				// convert from log domain
				for (int m = M; --m>=0;)
				{
					double out = Math.exp(outMsgs[m] - maxLog);
					outMsgs[m] = out;
				}

				// normalize
				if (_normalizeMessages)
				{
					getSiblingEdgeState(out_d).varToFactorMsg.normalize();
				}
			}
		}
		
	    DimpleEnvironment.doubleArrayCache.release(logInPortMsgs);
	    DimpleEnvironment.doubleArrayCache.release(alphas);
	   
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
	        	double tmp = getSiblingEdgeState(d).factorToVarMsg.getWeight(m);
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
			final double[] inMsg = getSiblingEdgeState(i).factorToVarMsg.representation();
			for (int j =  0; j < retval.length; j++)
			{
				retval[j] *= inMsg[j];
		}
		}
		
		return retval;
	}

	/**
	 * experimental
	 * @category internal
	 * @since 0.08
	 */
	@Internal
	public double computeUnnormalizedLogLikelihood()
	{
		if (_model.hasFixedValue())
		{
			return 0.0;
		}
		
		double [] retval = _input.clone();
		
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			DiscreteMessage inMsg = getSiblingEdgeState(i).factorToVarMsg;
			for (int j =  0; j < retval.length; j++)
			{
				retval[j] *= inMsg.getUnnormalizedWeight(j);
			}
		}
		
		double sum = 0.0;
		
		for (int j = 0; j < retval.length; ++j)
		{
			sum += retval[j];
		}
		
		return weightToEnergy(sum);
		
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
			final EdgeState edge = _model.getSiblingEdgeState(i);
			SumProductTableFactor sft = (SumProductTableFactor)getSibling(i);
			double inputMsg = getSiblingEdgeState(i).factorToVarMsg.getWeight(domain);
			double tmp = f / inputMsg;
			@SuppressWarnings("null")
			double der = sft.getMessageDerivative(weightIndex, edge.getFactorToVariableEdgeNumber())[domain];
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
				f *= getSiblingEdgeState(i).factorToVarMsg.getWeight(d);
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
    
	/**
	 * @deprecated instead use {@link #getMessageDerivative(int, int)}.
	 */
	@Deprecated
    public double [] getMessageDerivative(int wn, Factor f)
    {
    	return getMessageDerivative(wn, _model.findSibling(f));
    }
    
	@Internal
    public double[] getMessageDerivative(int wn, int edgeNumber)
    {
    	return requireNonNull(_outMessageDerivative)[wn][edgeNumber];
    }
    
    public double calculatedf(int outPortNum, double f, int d, int wn)
    {
    	double df = 0;
		
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			if (i != outPortNum)
			{
				final EdgeState edge = _model.getSiblingEdgeState(i);
				double thisMsg = getSiblingEdgeState(i).factorToVarMsg.getWeight(d);
				SumProductTableFactor stf = (SumProductTableFactor)getSibling(i);
				@SuppressWarnings("null")
				double [] dfactor = stf.getMessageDerivative(wn, edge.getFactorToVariableEdgeNumber());
				
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
	protected double[] createDefaultMessage()
	{
    	final double [] retval = super.createDefaultMessage();
    	Arrays.fill(retval, 1.0 / retval.length);
    	return retval;
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
    	
    	double[] dampingParams = _dampingParams =
    		getReplicatedNonZeroListFromOptions(BPOptions.nodeSpecificDamping, BPOptions.damping, size, _dampingParams);
 
    	if (dampingParams.length > 0 && dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				BPOptions.nodeSpecificDamping, this);
			_dampingParams = null;
    	}
    	
    	if (dampingParams.length == 0)
    	{
    		_dampingParams = null;
    	}
    }

    @Override
	@SuppressWarnings("null")
	public SumProductDiscreteEdge getSiblingEdgeState(int siblingIndex)
	{
		return (SumProductDiscreteEdge)getSiblingEdgeState_(siblingIndex);
	}
}
