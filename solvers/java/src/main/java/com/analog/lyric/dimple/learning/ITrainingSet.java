/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;



/**
 * Represents an ordered collection of training samples for parameter learning.
 * <p>
 * This interface implements {@link Iterable} instead of a some sort of collection
 * interface in anticipation of very large training sets that may not be convenient
 * to implement as an in-memory collection.
 */
public interface ITrainingSet extends Iterable<ITrainingSample>
{
	/**
	 * Returns object containing variable assignments that are common across
	 * the data set. This should contain only assignments of type {@link TrainingAssignmentType#FIXED}
	 * and {@link TrainingAssignmentType#INPUTS}. May be null if there are no common
	 * assignments in the training set.
	 */
	public @Nullable ITrainingSample getCommonAssignments();
}
