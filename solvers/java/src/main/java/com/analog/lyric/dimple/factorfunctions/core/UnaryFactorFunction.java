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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.DataRepresentationType;


/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class UnaryFactorFunction extends FactorFunction implements IUnaryFactorFunction
{
	private static final long serialVersionUID = 1L;
	
	/*--------------
	 * Construction
	 */
	
	// Note: there is no default constructor to make it less likely that implementors
	// of copy constructor in subclass will forget to call super.
	
	protected UnaryFactorFunction(@Nullable String name)
	{
		super(name);
	}
	
	protected UnaryFactorFunction(UnaryFactorFunction other)
	{
		super(other);
	}
	
	@Override
	public abstract UnaryFactorFunction clone();
	
    /*----------------
     * IDatum methods
     */

    /**
     * {@inheritDoc}
     * <p>
     * Returns {@link DataRepresentationType#FUNCTION}.
     */
    @Override
    public DataRepresentationType representationType()
    {
    	return DataRepresentationType.FUNCTION;
    }
}
