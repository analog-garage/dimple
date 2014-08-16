/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.learning;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

public class BasicTrainingSet implements ITrainingSet
{
	/*-------
	 * State
	 */
	
	private final @Nullable ITrainingSample _commonAssignments;
	private final Iterable<ITrainingSample> _samples;

	/*--------------
	 * Construction
	 */
	
	public BasicTrainingSet(@Nullable ITrainingSample commonAssignments, Iterable<ITrainingSample> samples)
	{
		_commonAssignments = commonAssignments;
		_samples = samples;
	}
	
	public BasicTrainingSet(Iterable<ITrainingSample> samples)
	{
		this(null, samples);
	}

	/*----------------------
	 * ITrainingSet methods
	 */
	
	@Override
	public Iterator<ITrainingSample> iterator()
	{
		return _samples.iterator();
	}

	@Override
	public @Nullable ITrainingSample getCommonAssignments()
	{
		return _commonAssignments;
	}
}
