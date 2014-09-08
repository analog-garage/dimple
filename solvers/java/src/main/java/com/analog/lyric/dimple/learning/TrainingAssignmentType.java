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

import com.analog.lyric.dimple.model.variables.Variable;


/**
 * Enumerates type of information for a particular variable in a training sample {@link ITrainingSample}:
 * 
 * <dl>
 * <dt>MISSING
 * <dd>There is no information about the variable in the sample.
 * <dt>VALUE
 * <dd>The training sample contains a value for the variable.
 * <dt>INPUTS
 * <dd>The variable has value for its inputs ({@link Variable#getInputObject()}).
 * <dt>FIXED
 * <dd>The variable has a fixed value across the training set. Fixed values are represented in the {@link ITrainingSet}
 * and not directly in the sample.
 * </dl>
 */
public enum TrainingAssignmentType
{
	MISSING,
	VALUE,
	INPUTS,
	FIXED;
}
