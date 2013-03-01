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

package com.analog.lyric.dimple.FactorFunctions.core;

import com.analog.lyric.dimple.model.Domain;

public abstract class FactorFunctionBase 
{
	private String _name;

	public FactorFunctionBase(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	
	// WARNING WARNING WARNING
	// At least one or the other of these must be overridden in a derived class.
	// SHOULD override evalEnergy instead of eval, but for now can override one or the other.
	// TODO: Eventually eval should be made final and so that only evalEnergy can be overridden.
	public double eval(Object ... input)
	{
		return Math.exp(-evalEnergy(input));
	}
	public double evalEnergy(Object ... input)
	{
		return -Math.log(eval(input));
	}
	
	
	public abstract FactorTable getFactorTable(Domain [] domainList);
	
	
	// For directed factors...
	// Inherently directed factor functions should override this and return true
	public boolean isDirected() {return false;}
	
	// Inherently directed factor functions should override this and return the output index (or indices)
	public int[] getDirectedToIndices() {return null;}
	
	// TODO: This doesn't appear to be used
	public boolean verifyValidForDirectionality(int [] directedTo, int [] directedFrom)
	{
		return true;
	}
	
	// For deterministic directed factors...
	// This means that for any given input, only one of its outputs has non-zero value (equivalently, finite energy)
	// Inherently deterministic directed factors should override this and return true
	public boolean isDeterministicDirected() {return false;}
	
	// For deterministic directed factors, evaluate the deterministic function output(s) given only the inputs
	// The arguments are in the same order as eval and evalEnergy, but in this case the output values should be overridden by new values
	public void evalDeterministicFunction(Object ... input){ }

}
