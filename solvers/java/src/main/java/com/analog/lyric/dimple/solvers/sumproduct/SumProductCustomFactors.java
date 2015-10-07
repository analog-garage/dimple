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

package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.factorfunctions.ComplexNegate;
import com.analog.lyric.dimple.factorfunctions.ComplexSubtract;
import com.analog.lyric.dimple.factorfunctions.ComplexSum;
import com.analog.lyric.dimple.factorfunctions.FiniteFieldAdd;
import com.analog.lyric.dimple.factorfunctions.FiniteFieldMult;
import com.analog.lyric.dimple.factorfunctions.FiniteFieldProjection;
import com.analog.lyric.dimple.factorfunctions.LinearEquation;
import com.analog.lyric.dimple.factorfunctions.MatrixRealJointVectorProduct;
import com.analog.lyric.dimple.factorfunctions.Multiplexer;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.factorfunctions.Negate;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Product;
import com.analog.lyric.dimple.factorfunctions.RealJointNegate;
import com.analog.lyric.dimple.factorfunctions.RealJointSubtract;
import com.analog.lyric.dimple.factorfunctions.RealJointSum;
import com.analog.lyric.dimple.factorfunctions.Subtract;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariablePredicates;
import com.analog.lyric.dimple.solvers.core.CustomFactors;
import com.analog.lyric.dimple.solvers.core.ISolverFactorCreator;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomComplexGaussianPolynomial;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldAdd;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldConstantMult;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldMult;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldProjection;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianLinear;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianLinearEquation;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianNegate;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianProduct;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianSubtract;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianSum;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultiplexer;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianNegate;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianProduct;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianSubtract;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianSum;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateNormalConstantParameters;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomNormalConstantParameters;
import com.analog.lyric.dimple.solvers.sumproduct.sampledfactor.SampledFactor;
import com.google.common.collect.Iterables;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SumProductCustomFactors extends CustomFactors<ISolverFactor, SumProductSolverGraph>
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public SumProductCustomFactors()
	{
		super(ISolverFactor.class, SumProductSolverGraph.class);
	}
	
	protected SumProductCustomFactors(SumProductCustomFactors other)
	{
		super(other);
	}
	
	@Override
	public SumProductCustomFactors clone()
	{
		return new SumProductCustomFactors(this);
	}
	
	/*-----------------------
	 * CustomFactors methods
	 */

	@Override
	public void addBuiltins()
	{
		add(ComplexNegate.class, CustomMultivariateGaussianNegate.class);
		add(ComplexSubtract.class, CustomMultivariateGaussianSubtract.class);
		add(ComplexSum.class, CustomMultivariateGaussianSum.class);
		add(FiniteFieldAdd.class, CustomFiniteFieldAdd.class);
		add(FiniteFieldMult.class, CustomFiniteFieldConstantMult.class);
		add(FiniteFieldMult.class, CustomFiniteFieldMult.class);
		add(FiniteFieldProjection.class, CustomFiniteFieldProjection.class);
		add(LinearEquation.class, CustomGaussianLinearEquation.class);
		add(MatrixRealJointVectorProduct.class, CustomMultivariateGaussianProduct.class);
		add(Multiplexer.class, CustomMultiplexer.class);
		add(MultivariateNormal.class, CustomMultivariateNormalConstantParameters.class);
		add(Negate.class, CustomGaussianNegate.class);
		add(Normal.class, CustomNormalConstantParameters.class);
		add(Product.class, CustomGaussianProduct.class);
		add(RealJointNegate.class, CustomMultivariateGaussianNegate.class);
		add(RealJointSubtract.class, CustomMultivariateGaussianSubtract.class);
		add(RealJointSum.class, CustomMultivariateGaussianSum.class);
		add(Subtract.class, CustomGaussianSubtract.class);
		add(Sum.class, CustomGaussianSum.class);
		
		// Backwards compatibility
		add("add", new ISolverFactorCreator<ISolverFactor, SumProductSolverGraph>() {
			@Override
			public ISolverFactor create(Factor factor, SumProductSolverGraph sgraph)
			{
				// We don't need to implement this using a single creator, but this way we can produce
				// a better error message.
				if (Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedReal()))
					return new CustomGaussianSum(factor, sgraph);
				if (Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedRealJoint()))
					return new CustomMultivariateGaussianSum(factor, sgraph);

				throw new SolverFactorCreationException("Variables must be unbounded and all Real or all RealJoint");
			}
		});
		add("constmult", CustomGaussianProduct.class);
		add("constmult", CustomMultivariateGaussianProduct.class);
		add("finiteFieldAdd", CustomFiniteFieldAdd.class);
		add("finiteFieldMult", CustomFiniteFieldConstantMult.class);
		add("finiteFieldMult", CustomFiniteFieldMult.class);
		add("finiteFieldProjection", CustomFiniteFieldProjection.class);
		add("linear", CustomGaussianLinear.class);
		add("multiplexerCPD", CustomMultiplexer.class);
		add("polynomial", CustomComplexGaussianPolynomial.class);
	}
	
	@Override
	public ISolverFactor createDefault(Factor factor, SumProductSolverGraph sgraph)
	{
		if (factor.isDiscrete())
		{
			@SuppressWarnings("deprecation") // FIXME remove when STableFactor removed
			ISolverFactor sfactor = new STableFactor(factor, sgraph);
			return sfactor;
		}
		else
		{
			// For non-discrete factor that doesn't have a custom factor, create a sampled factor
			return new SampledFactor(factor, sgraph);
		}
	}
	
}
