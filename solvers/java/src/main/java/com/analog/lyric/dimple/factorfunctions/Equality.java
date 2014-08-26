/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.factorfunctions;

import java.util.Arrays;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.values.Value;

/**
 * Deterministic equality constraint.  Values must be numeric or boolean.
 * 
 * Optional smoothing may be applied, by providing a smoothing value in
 * the constructor. If smoothing is enabled, the distribution is
 * smoothed by exp(-difference^2/smoothing), where difference is the
 * distance between the output value and the deterministic output value
 * for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1...) Arbitrary length list of values (double, integer, or boolean)
 * 
 */
public class Equality extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;

	public Equality() {this(0);}
	public Equality(double smoothing)
	{
		super();
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	if (arguments.length == 0)
    		return 0;
    	
    	final double firstVal = arguments[0].getDouble();

    	if (_smoothingSpecified)
    	{
    		double potential = 0;
    		for (int i = 1; i < arguments.length; i++)
    		{
    			final double diff = firstVal - arguments[i].getDouble();
    			potential += diff*diff;
    		}
        	return potential*_beta;
    	}
    	else
    	{
    		for (int i = 1; i < arguments.length; i++)
    			if (arguments[i].getDouble() != firstVal)
    				return Double.POSITIVE_INFINITY;
        	return 0;
    	}
    }
    
    @Override
    protected IFactorTable createTableForDomains(JointDomainIndexer domains)
    {
    	final DiscreteDomain domain = domains.get(0);
    	for (int i = 1; i < domains.size(); ++ i)
    	{
    		if (!domain.equals(domains.get(i)))
    		{
    			return super.createTableForDomains(domains);
    		}
    	}
    	
    	// Special case for all domains the same

		final int size = domains.size();
		final double[] energies = new double[size];
		final int[] jointIndices = new int[size];
		final int[] indices = new int[domains.size()];

		for (int i = 0; i < size; ++i)
		{
			Arrays.fill(indices, i);
			jointIndices[i] = domains.jointIndexFromIndices(indices);
		}

		IFactorTable table = FactorTable.create(domains);
		table.setEnergiesSparse(jointIndices, energies);
		return table;
    }
    
}
