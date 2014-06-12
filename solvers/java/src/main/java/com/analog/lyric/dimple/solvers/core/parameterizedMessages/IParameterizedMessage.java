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

package com.analog.lyric.dimple.solvers.core.parameterizedMessages;

import java.io.Serializable;

import com.analog.lyric.util.misc.IPrintable;

public interface IParameterizedMessage extends Cloneable, Serializable, IPrintable
{
	public abstract IParameterizedMessage clone();
	
		/**
	 * Computes the KL divergence of this message with respect to a
	 * another message of the same type. I.e. it should compute:
	 * <blockquote>
	 *    D<sub>KL</sub>(P || Q)
	 * </blockquote>
	 * where P refers to the distribution described by this message, and Q to the distribution
	 * described by {@code that} message.
	 * 
	 * @param that another message with a compatible type with this message. Typically this means
	 * that the class type must match. See specific subclass implementations for details.
	 * @return computed KL divergence in units of <A href="http://en.wikipedia.org/wiki/Nat_(unit)">nats</a>,
	 * @throws IllegalArgumentException if {@code that} is not compatible with {@code this}.
	 * @since 0.06
	 */
	public double computeKLDivergence(IParameterizedMessage that);
	
	/**
	 * Sets message to its "null" form.
	 * <p>
	 * Usually this means the parameters will be set to represent a weak uniform distribution.
	 */
	public void setNull();
}
