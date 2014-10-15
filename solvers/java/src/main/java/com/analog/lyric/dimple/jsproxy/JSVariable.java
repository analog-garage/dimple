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
 * 
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
	
	public JSDomain<?> domain()
	{
		return getApplet().domains.wrap(_delegate.getDomain());
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
	
	public @Nullable Object getFixedValue()
	{
		return _delegate.getFixedValueObject();
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
	
	public boolean hasFixedValue()
	{
		return _delegate.hasFixedValue();
	}
	
	public void setFixedValue(@Nullable Object value)
	{
		if (value == null)
		{
			_delegate.setInputObject(null);
		}
		else
		{
			_delegate.setFixedValueObject(value);
		}
	}
	
	public void setInput(JSFactorFunction input)
	{
		_delegate.setInputObject(input._delegate);
	}
	
	public void setInput(double[] input)
	{
		_delegate.setInputObject(input);
	}
}
