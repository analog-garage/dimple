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

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.util.misc.Matlab;
import com.analog.lyric.util.misc.Nullable;

/**
 * Base class for Dimple MATLAB proxy objects.
 */
@Matlab
public abstract class PObject
{
	/**
	 * Returns the modeler object wrapped by this proxy or null
	 * if not applicable for this type of object.
	 */
	public @Nullable Object getModelerObject() { return null; }

	/**
	 * True if object applies only to discrete variables.
	 * Implies not {@link #isReal} and not {@link #isJoint}.
	 */
	public boolean isDiscrete() { return false; }
	
	/**
	 * True if object is a type of {@link PDomain}.
	 */
	public boolean isDomain() { return false; }
	
	/**
	 * True of object is a {@link PFactorGraphVector}.
	 */
	public boolean isGraph() { return false; }
	
	/**
	 * True if object is a {@link PFactorVector}.
	 */
	public boolean isFactor() { return false; }
	
	/**
	 * True if object is a {@link PFactorFunction}
	 */
	public boolean isFactorFunction() { return false; }
	
	/**
	 * True if object is a {@link PFactorTable}
	 */
	public boolean isFactorTable() { return false; }
	
	/**
	 * True if object applies only to {@link RealJoint} variables.
	 * Implies not {@link #isDiscrete} and not {@link #isReal}.
	 */
	public boolean isJoint() { return false; }
	/**
	 * True if object applies only to real variables.
	 * Implies not {@link #isDiscrete} and not {@link #isJoint}.
	 */
	public boolean isReal() { return false; }
	
	/**
	 * True if object is a {@link PVariableVector}.
	 */
	public boolean isVariable() { return false; }
	
	/**
	 * True if object is a type of {@link PNodeVector}.
	 */
	public boolean isVector() { return false; }
}
