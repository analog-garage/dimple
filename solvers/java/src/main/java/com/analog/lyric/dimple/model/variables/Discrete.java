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

package com.analog.lyric.dimple.model.variables;

import static java.util.Objects.*;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.solvers.interfaces.IDiscreteSolverVariable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;

@SuppressWarnings("deprecation")
public class Discrete extends VariableBase
{
	/*--------------
	 * Construction
	 */
	public Discrete(DiscreteDomain domain)
	{
		this(domain, "Discrete");
	}

	public Discrete(Object... domain)
	{
		this(DiscreteDomain.create(domain),"Discrete");

		if (domain.length < 1)
			throw new DimpleException(String.format("ERROR Variable domain length %d must be at least 2", domain.length));
	}
	
	/**
	 * @deprecated as of release 0.08 use {@link #Discrete(DiscreteDomain)} instead.
	 */
	@Deprecated
	public Discrete(DiscreteDomain domain, String modelerClassName)
	{
		super(domain, modelerClassName);
	}
	
	protected Discrete(Discrete that)
	{
		super(that);
	}

	@Override
    @NonNull
    public Discrete clone()
    {
    	return new Discrete(this);
    }
    
    /*---------------------
     * ISolverNode methods
     */
    
    @Override
    public @Nullable IDiscreteSolverVariable getSolver()
    {
    	return (IDiscreteSolverVariable)super.getSolver();
    }
    
    /*------------------
     * Variable methods
     */
    
    @Override
    public final Discrete asDiscreteVariable()
    {
    	return this;
    }
    
    @Override
	public DiscreteDomain getDomain()
    {
    	return (DiscreteDomain)super.getDomain();
    }
    
    public DiscreteDomain getDiscreteDomain()
    {
    	return getDomain();
    }
    
    @Override
    public @Nullable Integer getFixedValueObject()
    {
    	IDatum datum = getPrior();
    	if (datum instanceof Value)
    	{
    		return ((Value)datum).getIndex();
    	}
    	return null;
    }
    
    
    
    
    /*------------------
     * Discrete methods
     */
    
    public double [] getBelief()
    {
    	return (double[])getBeliefObject();
    }
    
    @Override
	public Object getBeliefObject()
    {
    	final ISolverVariable svar = getSolver();
    	if (svar != null)
    	{
    		final Object belief = svar.getBelief();
    		if (belief != null)
    		{
    			return belief;
    		}
    	}
    	
    	return getInputObject();
    }
    
    public int getGuessIndex()
    {
    	return requireNonNull(getSolver()).getGuessIndex();
    }
    
    public void setGuessIndex(int guess)
    {
    	requireNonNull(getSolver()).setGuessIndex(guess);
    }
    
    public Object getValue()
    {
    	return requireSolver("getValue").getValue();
    }
   
    public int getValueIndex()
    {
    	return ((IDiscreteSolverVariable)requireSolver("getValueIndex")).getValueIndex();
    }
    
	private double [] getDefaultPriors(DiscreteDomain domain)
	{
		final int length = domain.size();
		double [] retval = new double[length];
		double val = 1.0/length;
		for (int i = 0; i < retval.length; i++)
			retval[i] = val;
		return retval;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * All {@code variables} must be of type {@link Discrete}. The domain of the returned
	 * variable will be a {@link JointDiscreteDomain} with the subdomains in the same order
	 * as {@code variables}.
	 */
	@Internal
    @Override
	public Variable createJointNoFactors(Variable ... variables)
    {
    	final boolean thisIsFirst = (variables[0] == this);
    	final int dimensions = thisIsFirst ? variables.length: variables.length + 1;
    	final DiscreteDomain[] domains = new DiscreteDomain[dimensions];
    	final IDatum[] subdomainPriors = new IDatum[dimensions];
    	domains[0] = getDomain();
    	subdomainPriors[0] = getPrior();
    	
    	for (int i = thisIsFirst ? 1 : 0; i < dimensions; ++i)
    	{
    		final Discrete var = variables[i].asDiscreteVariable();
    		domains[i] = var.getDomain();
    		subdomainPriors[i] = var.getPrior();
    	}

    	final JointDiscreteDomain<?> jointDomain = DiscreteDomain.joint(domains);
    	final Discrete jointVar =  new Discrete(jointDomain);
    	jointVar.setPrior(joinPriors(jointDomain ,subdomainPriors));
    	return jointVar;
    }
	
	@Override
	public @Nullable IDatum setPrior(@Nullable Object prior)
	{
		if (prior instanceof double[])
		{
			return setPrior((double[])prior);
		}
		
		if (prior instanceof Value)
		{
			Value value = (Value)prior;
			final DiscreteDomain domain = getDomain();
			
			if (!domain.equals(value.getDomain()))
			{
				// If domain does not match, create a new value with the correct domain. This ensures
				// that indexing operations can be assumed to be correct for this variable.
				prior = Value.create(domain, requireNonNull(value.getObject()));
			}
		}
		
		return super.setPrior(prior);
	}
	
	/**
	 * Sets prior to new {@link DiscreteWeightMessage} with given weights.
	 * @param weights must have same length as variable's domain.
	 * @return previous value of prior
	 * @since 0.08
	 */
	public @Nullable IDatum setPrior(@Nullable double ... weights)
	{
		return setPrior(weights == null ? null : new DiscreteWeightMessage(weights));
	}
	
	/**
	 * Sets prior to a fixed discrete value with given index.
	 * @param index a valid index into the variable's {@linkplain #getDomain() domain}.
	 * @return previous value of prior
	 * @since 0.08
	 */
	public @Nullable IDatum setPriorIndex(int index)
	{
		return setPrior(Value.createWithIndex(getDomain(), index));
	}
	
	/**
	 * If prior is a {@link DiscreteValue}, returns its index, otherwise -1.
	 * @since 0.08
	 */
	public final int getPriorIndex()
	{
		Value value = getPriorValue();
		return value != null ? value.getIndex() : -1;
	}
	
	/*--------------------
	 * Deprecated methods
	 */
	
	@Deprecated
	public double [] getInput()
	{
		return (double[])getInputObject();
	}

	@Deprecated
	@Override
	public Object getInputObject()
	{
		Object input = priorToInput(getPrior());
		
		if (input == null)
		{
			input = getDefaultPriors(getDiscreteDomain());
		}
		
		return input;
	}

	/**
	 * @deprecated use {@link #setPrior(double...)} instead
	 */
	@Deprecated
	public void setInput(@Nullable double ... value)
	{
		setPrior(value);
	}

	/**
	 * @deprecated use {@link #getPriorIndex()} instead
	 */
	@Deprecated
	public final int getFixedValueIndex()
	{
		Integer index = getFixedValueObject();
		if (index == null)
			throw new DimpleException("Fixed value not set");
		
		return index;
	}

	/**
	 * @deprecated use {@link #getPriorValue()} instead
	 */
	@Deprecated
	public final Object getFixedValue()
	{
		Integer index = getFixedValueObject();
		if (index == null)
			throw new DimpleException("Fixed value not set");
		
		return getDomain().getElement(index);
	}
	
	/**
	 * @deprecated use {@link #setPriorIndex(int)} instead.
	 */
	@Deprecated
	public void setFixedValueIndex(int fixedValueIndex)
	{
		setPrior(Value.createWithIndex(getDomain(), fixedValueIndex));
		
	}
	
	/**
	 * @deprecated use {@link #setPrior} instead.
	 */
	@Deprecated
	public void setFixedValue(Object fixedValue)
	{
		setPrior(Value.create(getDomain(), fixedValue));
	}
	
	@Deprecated
	@Override
	public void setFixedValueObject(@Nullable Object value)
	{
		setPrior(value != null ? Value.createWithIndex(getDomain(), (Integer)value) : null);
	}
	
	/*----------------------------
	 * Protected/internal methods
	 */
	
	@Override
	protected @Nullable Object priorToFixedValue(@Nullable IDatum prior)
	{
		return prior instanceof Value ? ((Value)prior).getIndex() : null;
	}

	@Override
	protected @Nullable Object priorToInput(@Nullable IDatum prior)
	{
		if (prior instanceof Value)
		{
			final int index = ((Value)prior).getIndex();
			final double[] input = new double[getDomain().size()];
			input[index] = 1.0;
			return input;
		}
		else if (prior instanceof DiscreteMessage)
		{
			return ((DiscreteMessage)prior).getWeights();
		}
		
		return prior;
	}
	
	/*-----------------
	 * Private methods
	 */

	private @Nullable IDatum joinPriors(JointDiscreteDomain<?> jointDomain, IDatum[] subdomainPriors)
	{
		final JointDomainIndexer domains = jointDomain.getDomainIndexer();
		final int dimensions = jointDomain.getDimensions();
		boolean hasPrior = false;
		int[] fixedIndices = new int[dimensions];
		Arrays.fill(fixedIndices, -1);
		
		for (int i = 0; i < dimensions; ++i)
		{
			DiscreteDomain domain = domains.get(i);
			IDatum prior = subdomainPriors[i];
			if (prior != null)
			{
				hasPrior = true;
				
				if (prior instanceof Value)
				{
					Value value = (Value)prior;
					fixedIndices[i] =
						domain.equals(value.getDomain()) ? value.getIndex() : domain.getIndex(value.getObject());
					subdomainPriors[i] = new DiscreteEnergyMessage(domain, value);
				}
				else
				{
					DiscreteMessage msg = prior instanceof DiscreteMessage ? (DiscreteMessage)prior :
						new DiscreteWeightMessage(domain, prior);
					subdomainPriors[i] = msg;
					fixedIndices[i] = msg.toDeterministicValueIndex();
				}
			}
		}
		
		if (!hasPrior)
		{
			// If none of the component variables has a prior, then neither will the joint variable.
			return null;
		}
		
		boolean hasAllFixedPriors = true;
		for (int i : fixedIndices)
		{
			if (i < 0)
			{
				hasAllFixedPriors = false;
				break;
			}
		}
		
		if (hasAllFixedPriors)
		{
			// Return fixed value with appropriate joint index.
			return Value.createWithIndex(jointDomain, domains.jointIndexFromIndices(fixedIndices));
		}
		
		int cardinality = jointDomain.size();
		
		double[] energies = new double[cardinality];
		
		int inner = 1, outer = cardinality;
		for (int dim = 0; dim < dimensions; ++dim)
		{
			final DiscreteDomain domain = domains.get(dim);
			final DiscreteMessage prior = (DiscreteMessage)subdomainPriors[dim];
			final int size = domain.size();
			int i = 0;
			
			outer /= size;
			
			if (prior != null)
			{
				for (int o = 0; o < outer; ++o)
				{
					for (double energy : prior.getEnergies())
					{
						for (int r = 0; r < inner; ++r)
						{
							energies[i++] += energy;
						}
					}
				}
			}
			
			inner *= size;
		}
		
		return new DiscreteEnergyMessage(energies);
	}
	

}
