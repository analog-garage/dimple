package com.analog.lyric.dimple.solvers.gibbs.customFactors;

import java.util.ArrayList;
import java.util.Collection;

import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.NormalSampler;

public class CustomGamma extends SRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private double[][] _outputMsgs;
	private SRealVariable _alphaVariable;
	private SRealVariable _betaVariable;
	private boolean _hasConstantAlpha;
	private boolean _hasConstantBeta;
	private boolean _hasConstantOutputs;
	private int _numOutputEdges;
	private int _numParameterEdges;
	private int _constantOutputCount;
	private double _constantAlphaValue;
	private double _constantBetaValue;
	private double _constantOutputSum;
	private static final int NUM_PARAMETERS = 2;
	private static final int ALPHA_PARAMETER_INDEX = 0;
	private static final int BETA_PARAMETER_INDEX = 1;
	
	public CustomGamma(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		IRealConjugateSampler conjugateSampler = _conjugateSampler[outPortNum];
		if (conjugateSampler == null)
			super.updateEdgeMessage(outPortNum);
		else if (conjugateSampler instanceof GammaSampler)
		{
			double[] outputMsg = _outputMsgs[outPortNum];
			if (outPortNum >= _numParameterEdges)
			{
				// Output port is directed output of Gamma
				outputMsg[0] = _hasConstantAlpha ? _constantAlphaValue : _alphaVariable.getCurrentSample();
				outputMsg[1] = _hasConstantBeta ? _constantBetaValue : _betaVariable.getCurrentSample();
			}
			else
			{
				// Output port must be the beta-parameter input of Gamma
				// Determine sample mean and precision
				
				// Start with the ports to variable outputs
				ArrayList<INode> siblings = _factor.getSiblings();
				double sum = 0;
				for (int port = _numParameterEdges; port < _numPorts; port++)
					sum += ((SRealVariable)(((VariableBase)siblings.get(port)).getSolver())).getCurrentSample();
				int count = _numOutputEdges;
				
				// Include any constant outputs also
				if (_hasConstantOutputs)
				{
					sum += _constantOutputSum;
					count += _constantOutputCount;
				}
				
				// Get the current alpha value
				double alpha = _hasConstantAlpha ? _constantAlphaValue : _alphaVariable.getCurrentSample();
				
				outputMsg[0] = count * alpha;			// Sample alpha
				outputMsg[1] = sum;						// Sample beta
			}
		}
		else
			super.updateEdgeMessage(outPortNum);
	}
	
	
	@Override
	public Collection<IRealConjugateSamplerFactory> getAvailableSamplers(int portNumber)
	{
		Collection<IRealConjugateSamplerFactory> availableSamplers = new ArrayList<IRealConjugateSamplerFactory>();
		if (!isPortAlphaParameter(portNumber))				// No supported conjugate sampler for alpha parameter
			availableSamplers.add(NormalSampler.factory);	// Either beta parameter or output, which have Gamma distribution
		return availableSamplers;
	}
	
	public boolean isPortAlphaParameter(int portNumber)
	{
		// This doesn't use the state values set up in initialize() since this may be called prior to initialize
		
		// Get the Gamma factor function and related state
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the Gamma factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		Gamma normalFactorFunction = (Gamma)(factorFunction);

		// Test whether or not the specified port is the alpha parameter
		if (normalFactorFunction.hasConstantParameters())
		{
			return false;	// Port must be an output since all parameters are constant
		}
		else if (hasFactorFunctionConstants)
		{
			if (constantFactorFunction.isConstantIndex(ALPHA_PARAMETER_INDEX))
				return false;	// Alpha parameter is constant, so it can't be a port
			else
				return (portNumber == ALPHA_PARAMETER_INDEX);	// Alpha is not constant so true if this is the alpha index
		}
		else if (portNumber == ALPHA_PARAMETER_INDEX)
		{
			return true;	// No parameters are constant, so this is the right port
		}
		return false;		// No constants, but the specified port is not the precision port
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine if any ports can use a conjugate sampler
		_conjugateSampler = new IRealConjugateSampler[_numPorts];
		for (int port = 0; port < _numPorts; port++)
		{
			INode var = _factor.getSiblings().get(port);
			if (var instanceof Real)
				_conjugateSampler[port] = ((SRealVariable)var.getSolver()).getConjugateSampler();
			else if (var instanceof RealJoint)
				_conjugateSampler[port] = ((SRealJointVariable)var.getSolver()).getConjugateSampler();
			else
				_conjugateSampler[port] = null;
		}
		
		
		// Get the Normal factor function and related state
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the Normal factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		Gamma normalFactorFunction = (Gamma)(factorFunction);
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		boolean hasFactorFunctionConstructorConstants = normalFactorFunction.hasConstantParameters();
		if (hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantAlpha = true;
			_hasConstantBeta = true;
			_constantAlphaValue = normalFactorFunction.getAlpha();
			_constantBetaValue = normalFactorFunction.getBeta();
			_numParameterEdges = 0;
		}
		else // Variable or constant parameters
		{
			_numParameterEdges = 0;
			ArrayList<INode> siblings = _factor.getSiblings();
			if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(ALPHA_PARAMETER_INDEX))
			{
				_hasConstantAlpha = true;
				_constantAlphaValue = (Double)constantFactorFunction.getConstantByIndex(ALPHA_PARAMETER_INDEX);
				_alphaVariable = null;
			}
			else
			{
				_hasConstantAlpha = false;
				int alphaEdgeIndex = _numParameterEdges++;
				_alphaVariable = (SRealVariable)(((VariableBase)siblings.get(alphaEdgeIndex)).getSolver());
			}
			if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(BETA_PARAMETER_INDEX))
			{
				_hasConstantBeta = true;
				_constantBetaValue = (Double)constantFactorFunction.getConstantByIndex(BETA_PARAMETER_INDEX);
				_betaVariable = null;
			}
			else
			{
				_hasConstantBeta = false;
				int betaEdgeIndex = _numParameterEdges++;
				_betaVariable = (SRealVariable)(((VariableBase)siblings.get(betaEdgeIndex)).getSolver());
			}
		}
		_numOutputEdges = _numPorts - _numParameterEdges;
		
		
		// Pre-compute statistics associated with any constant output values
		_hasConstantOutputs = false;
		if (hasFactorFunctionConstants)
		{
			Object[] constantValues = constantFactorFunction.getConstants();
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			_constantOutputCount = 0;
			_constantOutputSum = 0;
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					_constantOutputSum += (Double)constantValues[i];
					_constantOutputCount++;
				}
			}
			_hasConstantOutputs = true;
		}
	}
	
	
	@Override
	public void createMessages() 
	{
		super.createMessages();
		_outputMsgs = new double[_numPorts][NUM_PARAMETERS];
	}
	
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsgs[portIndex];
	}

}
