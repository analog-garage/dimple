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

package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.Binomial;
import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.CategoricalEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.CategoricalUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransition;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.ExchangeableDirichlet;
import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.Multinomial;
import com.analog.lyric.dimple.factorfunctions.MultinomialEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.MultinomialUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.Multiplexer;
import com.analog.lyric.dimple.factorfunctions.NegativeExpGamma;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Poisson;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.CustomFactors;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBernoulli;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBeta;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBinomial;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomCategorical;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomCategoricalUnnormalizedOrEnergyParameters;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomDirichlet;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomDiscreteTransition;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomDiscreteTransitionUnnormalizedOrEnergyParameters;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomExchangeableDirichlet;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomGamma;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomLogNormal;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomMultinomial;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomMultinomialUnnormalizedOrEnergyParameters;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomMultiplexer;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomNegativeExpGamma;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomNormal;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomPoisson;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class GibbsCustomFactors	extends CustomFactors<ISolverFactorGibbs, GibbsSolverGraph>
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public GibbsCustomFactors()
	{
		super(ISolverFactorGibbs.class, GibbsSolverGraph.class);
	}
	
	protected GibbsCustomFactors(GibbsCustomFactors other)
	{
		super(other);
	}
	
	@Override
	public CustomFactors<ISolverFactorGibbs, GibbsSolverGraph> clone()
	{
		return new GibbsCustomFactors(this);
	}
	
	/*-----------------------
	 * CustomFactors methods
	 */

	@Override
	public void addBuiltins()
	{
		add(Beta.class, CustomBeta.class);
		add(Bernoulli.class, CustomBernoulli.class);
		add(Binomial.class, CustomBinomial.class);
		add(Categorical.class, CustomCategorical.class);
		add(CategoricalEnergyParameters.class, CustomCategoricalUnnormalizedOrEnergyParameters.class);
		add(CategoricalUnnormalizedParameters.class, CustomCategoricalUnnormalizedOrEnergyParameters.class);
		add(Dirichlet.class, CustomDirichlet.class);
		add(DiscreteTransition.class, CustomDiscreteTransition.class);
		add(DiscreteTransitionEnergyParameters.class, CustomDiscreteTransitionUnnormalizedOrEnergyParameters.class);
		add(DiscreteTransitionUnnormalizedParameters.class, CustomDiscreteTransitionUnnormalizedOrEnergyParameters.class);
		add(ExchangeableDirichlet.class, CustomExchangeableDirichlet.class);
		add(Gamma.class, CustomGamma.class);
		add(NegativeExpGamma.class, CustomNegativeExpGamma.class);
		add(LogNormal.class, CustomLogNormal.class);
		add(Multinomial.class, CustomMultinomial.class);
		add(MultinomialEnergyParameters.class, CustomMultinomialUnnormalizedOrEnergyParameters.class);
		add(MultinomialUnnormalizedParameters.class, CustomMultinomialUnnormalizedOrEnergyParameters.class);
		add(Multiplexer.class, CustomMultiplexer.class);
		add(Normal.class, CustomNormal.class);
		add(Poisson.class, CustomPoisson.class);
	}
	
	@Override
	public ISolverFactorGibbs createDefault(Factor factor, GibbsSolverGraph sgraph)
	{
		if (factor.isDiscrete())
		{
			@SuppressWarnings("deprecation") // FIXME remove when STableFactor removed
			STableFactor sfactor = new STableFactor(factor, sgraph);
			return sfactor;
		}
		else
		{
			return new GibbsRealFactor(factor, sgraph);
		}
	}
}
