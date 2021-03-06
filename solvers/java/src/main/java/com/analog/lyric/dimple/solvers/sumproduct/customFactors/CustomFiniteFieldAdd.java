/*******************************************************************************
*   Copyright 2012-2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.sumproduct.SFiniteFieldFactor;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscreteEdge;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;

@SuppressWarnings("deprecation") // TODO remove when SFiniteFieldFactor removed
public class CustomFiniteFieldAdd extends SFiniteFieldFactor
{
	
	public CustomFiniteFieldAdd(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		
		if (factor.hasConstants())
			throw new SolverFactorCreationException("%s does not support constant arguments",
				getClass().getSimpleName());
		
		if (factor.getArgumentCount() != 3)
			throw new SolverFactorCreationException("%s expects 3 arguments",
				getClass().getSimpleName());

	}

	@Override
	@SuppressWarnings("null")
	public void doUpdateEdge(int outPortNum)
	{
		double [] inputs1 = null;
		double [] inputs2 = null;
		double [] outputs = null;
				

		for (int i = 0; i < 3; i++)
		{
			final SumProductDiscreteEdge edge = getSiblingEdgeState(i);
			if (outPortNum == i)
			{
				outputs = edge.factorToVarMsg.representation();
			}
			else
			{
				if (inputs1 == null)
					inputs1 = edge.varToFactorMsg.representation();
				else
					inputs2 = edge.varToFactorMsg.representation();
			}
		}

		inputs1 = inputs1.clone();
		inputs2 = inputs2.clone();
		
		//TODO: fix this.
		int n = (int) (Math.log(inputs1.length)/Math.log(2));

		double [] tmp1 = new double[inputs1.length];
		double [] tmp2 = new double[inputs2.length];
		double [] tmp3 = new double[outputs.length];

		//Fast hadamard input 1 probs
		fast_hadamard(n,inputs1, tmp1);
		fast_hadamard(n,inputs2, tmp2);

		//Point-wise multiply values
		for (int i = 0; i < outputs.length; i++)
		{
			tmp3[i] = tmp1[i]*tmp2[i];
		}


		//Fast hadamard result
		fast_hadamard(n,tmp3,outputs);
		double sum = 0;
		for (int i = 0; i < outputs.length; i++)
			sum += outputs[i];
		for (int i = 0; i < outputs.length; i++)
		{
			if (outputs[i] < 0)
				outputs[i] = 0;
			else
				outputs[i] /= sum;
		}
		
	}


	//TODO: can we do this in place?
	public static void fast_hadamard(int n, double [] in, double [] out)
	{
		int i, bit, flip_bit;
		int leftmask, rightmask, leftshifted, ind0, ind1;
		double [] tmp;
		for (bit=0;bit<n;bit++)
		{
			flip_bit=1<<bit;
			
			for (leftmask=0;leftmask< (1<<(n-bit-1));leftmask++)
			{
				leftshifted=leftmask<<(bit+1);
				for (rightmask=0;rightmask< (1<<bit); rightmask++)
				{
					ind0=leftshifted | rightmask;
					ind1=leftshifted | rightmask | flip_bit;
					out[ind0]=in[ind0]+in[ind1];
					out[ind1]=in[ind0]-in[ind1];
				}
			}
			
			tmp=in;
			in=out;
			out=tmp;
		}

		/* If "n" is even, then we need to copy "out" to "in" to make the
		     output appear in the correct array. */
		if ((n&1) == 0){
			for (i=0;i< (1<<n);i++){
				out[i]=in[i];
			}
		}

	}


}
