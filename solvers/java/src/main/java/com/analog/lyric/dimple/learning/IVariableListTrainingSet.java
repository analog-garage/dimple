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

import java.util.List;
import java.util.RandomAccess;

import com.analog.lyric.dimple.model.variables.Variable;

/**
 * A training set that provides an ordered list of all variables used in the samples.
 * This allows the training data to be represented efficiently as an array or sequence
 * in the same order as the variable list.
 */
public interface IVariableListTrainingSet extends ITrainingSet
{
	/**
	 * @return ordered fast random-access list of variables that are to be included in
	 * training samples in this set. The returned list is expected to implement the
	 * {@link RandomAccess} interface.
	 */
	public List<Variable> getVariableList();
}
