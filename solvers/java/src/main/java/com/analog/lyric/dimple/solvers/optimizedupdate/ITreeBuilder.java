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

package com.analog.lyric.dimple.solvers.optimizedupdate;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.util.misc.Internal;

/**
 * Used along with TreeWalker<T>.
 * 
 * @param <T> The type that represents a factor table within the optimized update tree. One
 *        object of this type is created to represent the root of the update tree, and others
 *        are created for each auxiliary table produced at the other nodes of the tree.
 * @since 0.06
 * @author jking
 */
@Internal
interface ITreeBuilder<T>
{
	/**
	 * Creates the T for the root of the optimized update tree.
	 * 
	 * @param factorTable The factor's original factor table.
	 * @since 0.06
	 */
	T createRootT(IFactorTable factorTable);

	/**
	 * Builds a marginalization step.
	 * 
	 * @param f The T from which a dimension is being marginalized out.
	 * @param portNum The port number for the dimension being marginalized out, in the original
	 *        factor. Note that for any layer below the root, the port number may not match the
	 *        local dimension.
	 * @param localDimension The dimension that is being marginalized out of f.
	 * @return A new T for the table that is produced by marginalizing localDimension out of f.
	 * @since 0.06
	 */
	T buildMarginalize(T f, final int portNum, final int localDimension);

	/**
	 * Builds an output step.
	 * 
	 * @param f The T holding the output message.
	 * @param portNum The port number for the dimension being output, in the original factor.
	 * @since 0.06
	 */
	void buildOutput(T f, final int portNum);
}
