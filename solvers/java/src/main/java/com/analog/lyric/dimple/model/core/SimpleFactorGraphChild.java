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

package com.analog.lyric.dimple.model.core;

import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SimpleFactorGraphChild implements IFactorGraphChild, IFactorGraphChildWithSetLocalId
{
	/*-------
	 * State
	 */

	protected int _id;
	protected @Nullable FactorGraph _parentGraph;
	
	/*--------------
	 * Construction
	 */
	
	protected SimpleFactorGraphChild()
	{
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return Ids.defaultNameForLocalId(_id);
	}

	/*------------
	 * Id methods
	 */
	
	@Override
	public long getId()
	{
		return getGlobalId();
	}

	@Override
	public int getLocalId()
	{
		return _id;
	}

	@Override
	public long getGlobalId()
	{
		final FactorGraph parent = _parentGraph;
		return Ids.globalIdFromParts(parent != null ? parent.getGraphId() : 0, _id);
	}

	@Override
	public UUID getUUID()
	{
		final FactorGraph parent = _parentGraph;
		return Ids.makeUUID(parent != null ? parent.getEnvironment().getEnvId() : 0, getGlobalId());
	}

	/*---------------------------
	 * IFactorGraphChild methods
	 */

	@Override
	public long getGraphTreeId()
	{
		final FactorGraph parent = _parentGraph;
		return Ids.graphTreeIdFromParts(parent != null ? parent.getGraphTreeIndex() : 0, _id);
	}

	@Override
	public @Nullable FactorGraph getContainingGraph()
	{
		return _parentGraph;
	}

	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _parentGraph;
	}

	@Override
	public @Nullable FactorGraph getRootGraph()
	{
		FactorGraph parent = _parentGraph;
		return parent != null ? parent.getRootGraph() : null;
	}
	
	/*------------------
	 * Internal methods
	 */
	
	/**
	 * @category internal
	 */
	@Internal
	@Override
	public void setLocalId(int id)
	{
		_id = id;
	}

	protected void setParentGraph(@Nullable FactorGraph parentGraph)
	{
		// TODO: combine with adding to owned list?
		_parentGraph = parentGraph;
	}

}
