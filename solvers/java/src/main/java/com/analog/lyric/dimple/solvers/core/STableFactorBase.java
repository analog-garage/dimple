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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.Factor;
import org.eclipse.jdt.annotation.Nullable;

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
    
    /**
     * Indicate whether {@link #initialize()} should call {@link #getFactorTable()} to
     * force factor table creation (e.g. to avoid lazy initialization for multithreading
     * solvers.
     * <p>
     * The default implementation returns true.
     */
    protected boolean createFactorTableOnInit()
    {
    	return true;
    }

    /*--------------------------
     * STableFactorBase methods
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
    
    protected final @Nullable IFactorTable getFactorTableIfComputed()
    {
    	return _factorTable;
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
