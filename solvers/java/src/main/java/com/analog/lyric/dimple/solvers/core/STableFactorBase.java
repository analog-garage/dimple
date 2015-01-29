/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.options.SolverOptions;

public abstract class STableFactorBase extends SFactorBase
{
	/*-------
	 * State
	 */
	
	private @Nullable IFactorTable _factorTable = null;

	/*--------------
	 * Construction
	 */
	
	public STableFactorBase(Factor factor)
	{
		super(factor);
		
		if (!factor.isDiscrete())
			throw new DimpleException("only discrete factors supported");
		
	}
	
	/*---------------------
	 * SFactorBase methods
	 */
	
    @Override
    public int [][] getPossibleBeliefIndices()
    {
            return getFactorTable().getIndicesSparseUnsafe();
    }
    
    @Override
    public void initialize()
    {
    	super.initialize();
    	if (createFactorTableOnInit())
    	{
    		getFactorTable();
    	}
    }
    
    /*--------------------------
     * STableFactorBase methods
     */
    
    /**
     * Removes current factor table instance.
     * <p>
     * After invoking this, {@link #getFactorTableIfComputed()} will return null.
     * <p>
     * The factor table can be recreated by calling {@link #getFactorTable()}.
     * <p>
     * @since 0.08
     */
    public void clearFactorTable()
    {
    	_factorTable = null;
    }
    
    /**
     * Indicate whether {@link #initialize()} should call {@link #getFactorTable()} to
     * force factor table creation (e.g. to avoid lazy initialization for multithreading
     * solvers.
     * <p>
     * The default implementation returns true if the <em>cardinality</em> of the factor is no greater than
     * {@link SolverOptions#maxAutomaticFactorTableSize}, where the <em>cardinality</em> is computed by multiplying
     * the dimensions of the independent domains of the factor, i.e. the input domains if the factor is
     * deterministic directed and otherwise all of the domains.
     */
    protected boolean createFactorTableOnInit()
    {
		final DomainList<?> domains = getFactor().getDomainList();
		final JointDomainIndexer indexer = domains.asJointDomainIndexer();
		
		if (indexer == null || !indexer.supportsJointIndexing())
		{
			return false;
		}
		
		int maxSize = getOptionOrDefault(SolverOptions.maxAutomaticFactorTableSize);
		if (maxSize <= 0)
		{
			return false;
		}
		
		final int cardinality = indexer.isDirected() && getFactor().getFactorFunction().isDeterministicDirected() ?
			indexer.getInputCardinality() : indexer.getCardinality();
		return cardinality <= getOptionOrDefault(SolverOptions.maxAutomaticFactorTableSize);
    }
    
    /**
     * Returns factor table for this factor, creating it if necessary.
     * <p>
     * Note that creation can be expensive for large joint cardinality of domains since the
     * factor function must be evaluated for all possible combinations of values!
     * 
     * @see #getFactorTableIfComputed()
     */
    public final IFactorTable getFactorTable()
	{
    	IFactorTable factorTable = _factorTable;
		if (factorTable==null)
		{
			factorTable = _factorTable = getFactor().getFactorTable();
			setTableRepresentation(factorTable);
		}
		return factorTable;
	}
    
    /**
     * Returns factor table for this factor if it has been created, else null.
     * <p>
     * @see #getFactorTable()
     * @since 0.08
     */
    public final @Nullable IFactorTable getFactorTableIfComputed()
    {
    	return _factorTable;
    }
    
    /**
     * Returns the dimension of the ith variable, assumed to be discrete
     * @since 0.08
     */
    protected int getSiblingDimension(int i)
    {
    	return requireNonNull(getSibling(i).getDomain().asDiscrete()).size();
    }
    
    /**
     * Sets representation of {@code table} for optimal performance for particular
     * solver implementation.
     * <p>
     * Called by {@link STableFactorBase#getFactorTable()} the first time it is called
     * on this object, and is also called by {@link STableFactorBase#initialize()}.
     * <p>
     * @param table
     */
    protected abstract void setTableRepresentation(IFactorTable table);
}
