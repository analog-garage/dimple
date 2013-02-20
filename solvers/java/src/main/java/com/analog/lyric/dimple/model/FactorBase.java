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

package com.analog.lyric.dimple.model;


public abstract class FactorBase extends Node
{
	// FIXME: constructors should probably have package access to enforce intention that all
	// subclasses are either Factors or FactorGraphs.
	
	public FactorBase(int id)
	{
		super(id);
	}
	
	public FactorBase()
	{
		super();
	}

	/** {@inheritDoc} If null {@link #asFactorGraph()} will be non-null. */
	@Override
	public Factor asFactor() { return null; }
	
	/** {@inheritDoc} If null {@link #asFactor()} will be non-null. */
	@Override
	public FactorGraph asFactorGraph() { return null; }

	//public abstract void initialize();
}

