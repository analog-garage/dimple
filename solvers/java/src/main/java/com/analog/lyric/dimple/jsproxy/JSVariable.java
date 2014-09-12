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
	
	public @Nullable Object getBelief()
	{
		return _delegate.getBeliefObject();
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
