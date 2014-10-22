/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.jsproxy;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * Javascript API representation of a Java variable
 * <p>
 * This can be used to set observed "fixed" values and to query the results of
 * inference for this variable.
 * <p>
 * This wraps an underlying Dimple {@link Variable} object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSVariable extends JSNode<Variable>
{
	JSVariable(JSFactorGraph parent, Variable var)
	{
		super(parent, var);
	}
	
	/*---------------------
	 * JSProxyNode methods
	 */
	
	@Override
	public JSNode.Type getNodeType()
	{
		return JSNode.Type.VARIABLE;
	}
	
	@Override
	public JSFactorGraph getParent()
	{
		return requireNonNull(super.getParent());
	}
	
	/*--------------------
	 * JSVariable methods
	 */
	
	/**
	 * Describes the domain of the variable.
	 * @since 0.07
	 */
	public JSDomain<?> domain()
	{
		return getDomainFactory(getApplet()).wrap(_delegate.getDomain());
	}
	
	private JSDomainFactory getDomainFactory(@Nullable DimpleApplet applet)
	{
		return applet != null ? applet.domains : new JSDomainFactory();
	}
	
	/**
	 * Returns all saved samples for this variable, if available.
	 * <p>
	 * When used with a sample-based solver (i.e. Gibbs), which is configured to save samples (e.g. by
	 * setting {@link GibbsOptions#saveAllSamples} option), this will return all the samples that were
	 * generated for this variable. Otherwise returns null.
	 * @since 0.07
	 */
	public @Nullable Object getAllSamples()
	{
		ISolverVariableGibbs svar = _delegate.getSolverIfType(ISolverVariableGibbs.class);
		if (svar != null)
		{
			return svar.getAllSamples();
		}
		
		return null;
	}
	
	/**
	 * Returns a representation of the marginal beliefs of this variable.
	 * <p>
	 * For discrete variables this will be an array of doubles describing the normalized probabilities
	 * of each possible discrete variable. The value will only be valid after inference ({@link JSFactorGraph#solve})
	 * has been run on the graph. Note that for MAP based solvers such as MinSum and JunctionTreeMAP these beliefs
	 * will be the max marginal beliefs and not normal marginal probabilities.
	 * <p>
	 * @since 0.07
	 */
	public @Nullable Object getBelief()
	{
		return _delegate.getBeliefObject();
	}
	
	/**
	 * Returns the current sample value if available.
	 * <p>
	 * When used with a sample-based solver (i.e. Gibbs), this will be the current sample value of this
	 * variable. Otherwise null.
	 * <p>
	 * @since 0.07
	 * @see #getAllSamples()
	 */
	public @Nullable Object getCurrentSample()
	{
		ISolverVariableGibbs svar = _delegate.getSolverIfType(ISolverVariableGibbs.class);
		if (svar != null)
		{
			return svar.getCurrentSampleValue().getObject();
		}
		
		return null;
	}
	
	/**
	 * Returns the fixed (observed) value of the variable or null if not fixed.
	 * @since 0.07
	 * @see #setFixedValue(Object)
	 */
	public @Nullable Object getFixedValue()
	{
		return _delegate.getFixedValueAsObject();
	}
	
	/**
	 * Get object representing input distribution for variable or null.
	 * <p>
	 * For discrete variables, this will be an array of weights, otherwise will
	 * be a {@link JSFactorFunction}.
	 * <p>
	 * @since 0.07
	 */
	public @Nullable Object getInput()
	{
		return _delegate.getInputObject();
	}
	
	/**
	 * Returns the value associated with the maximum belief, if available.
	 * <p>
	 * The meaning of this value will depend on what solver is used. For MAP solvers -- such
	 * as Min-Sum, LP, or JunctionTreeMAP -- this will be the value of the variable that is
	 * part of the most likely joint assignment of all non-fixed variables in the graph. Otherwise
	 * this will be the most likely marginal value of the variable.
	 * <p>
	 * Returns null if not supported by the current solver (e.g. the Gibbs solver does
	 * not support this for real variables).
	 * @since 0.07
	 */
	public @Nullable Object getMaxBeliefValue()
	{
		try
		{
			ISolverVariable svar = _delegate.getSolver();
			if (svar != null)
			{
				return svar.getValue();
			}
		}
		catch (Exception ex)
		{
		}

		return null;
	}
	
	/**
	 * True if variable has a fixed (observed) value
	 * @since 0.07
	 * @see #getFixedValue()
	 */
	public boolean hasFixedValue()
	{
		return _delegate.hasFixedValue();
	}
	
	/**
	 * Sets the fixed (observed) value of the variable.
	 * @param value is either a valid value for the variable's domain or is null indicating that
	 * the fixed value should be cleared.
	 * @since 0.07
	 * @see #getFixedValue()
	 */
	public void setFixedValue(@Nullable Object value)
	{
		if (value == null)
		{
			_delegate.setInputObject(null);
		}
		else
		{
			_delegate.setFixedValueFromObject(value);
		}
	}
	
	/**
	 * Sets input distribution for variable.
	 * <p>
	 * @param input is a factor function that can be used with a single edge. If
	 * {@linkplain JSFactorFunction#isParametric() parametric}, the function must
	 * {@linkplain JSFactorFunction#hasParameters() have internal parameters}.
	 * @since 0.07
	 * @see #setInput(double[])
	 */
	public void setInput(JSFactorFunction input)
	{
		_delegate.setInputObject(input._delegate);
	}
	
	/**
	 * Sets input distribution for discrete variable.
	 * @param input is an array of weights/probabilities with size matching the dimensions of the
	 * discrete domain of the variable.
	 * @since 0.07
	 * @see #setInput(JSFactorFunction)
	 */
	public void setInput(@Nullable double[] input)
	{
		_delegate.setInputObject(input);
	}
}
