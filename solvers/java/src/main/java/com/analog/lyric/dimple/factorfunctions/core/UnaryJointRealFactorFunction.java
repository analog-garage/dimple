/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.RealJointComponentValue;
import com.analog.lyric.dimple.model.values.RealJointValue;
import com.analog.lyric.dimple.model.values.Value;

import net.jcip.annotations.NotThreadSafe;

/**
 * A unary real-joint factor function based on unary real factor functions for each dimension.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public class UnaryJointRealFactorFunction extends UnaryFactorFunction
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final IUnaryFactorFunction[] _realFunctions;
	
	/*--------------
	 * Construction
	 */
	
	public UnaryJointRealFactorFunction(IUnaryFactorFunction ... functions)
	{
		super((String)null);
		_realFunctions = functions.clone();
	}
	
	protected UnaryJointRealFactorFunction(UnaryJointRealFactorFunction other)
	{
		super(other);
		final int n = other._realFunctions.length;
		_realFunctions = new IUnaryFactorFunction[n];
		for (int i = _realFunctions.length; --i>=0;)
			_realFunctions[i] = other._realFunctions[i].clone();
	}
	
	@Override
	public UnaryFactorFunction clone()
	{
		return new UnaryJointRealFactorFunction(this);
	}
	
	/*------------------------------
	 * IUnaryFactorFunction methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
			return true;
		
		if (other instanceof UnaryJointRealFactorFunction)
		{
			UnaryJointRealFactorFunction that = (UnaryJointRealFactorFunction)other;
			if (_realFunctions.length == that._realFunctions.length)
			{
				for (int i = _realFunctions.length; --i >= 0;)
				{
					if (!_realFunctions[i].objectEquals(that._realFunctions[i]))
					{
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public double evalEnergy(Value[] args)
	{
		double energy = 0;
		
		final int n = _realFunctions.length;

		RealJointComponentValue value = null;
		
		outer:
		for (Value arg : args)
		{
			if (!(arg instanceof RealJointValue))
			{
			}
			
			RealJointValue values = (RealJointValue)arg;
    		if (values.size() != n)
	    		throw new DimpleException("Dimension of variable does not equal to the dimension of joint function.");
			
    		if (value == null)
    		{
    			value = new RealJointComponentValue(values);
    		}
    		else
    		{
    			value.setRealJoint(values);
    		}
    		
    		for (int i = 0; i < n; ++i)
    		{
    			value.setComponentIndex(i);
    			energy += _realFunctions[i].evalEnergy(value);
    			if (Double.isInfinite(energy))
    				break outer;
    		}
		}
		
		return energy;
	}

	/*---------------
	 * Local methods
	 */
	
	public final List<IUnaryFactorFunction> realFunctions()
	{
		return Arrays.asList(_realFunctions);
	}
}
