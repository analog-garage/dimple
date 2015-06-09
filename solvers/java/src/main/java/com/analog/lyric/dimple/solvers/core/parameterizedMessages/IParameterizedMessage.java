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

import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.IPrintable;

public interface IParameterizedMessage extends IUnaryFactorFunction, Cloneable, Serializable, IPrintable
{
	/*-----------
	 * Cloneable
	 */
	
	@Override
	public abstract IParameterizedMessage clone();
	
	/*------------------------------
	 * IUnaryFactorFunction methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned energy value may be normalized by subtracting the
	 * {@linkplain #getNormalizationEnergy() normalization energy}.
	 */
	@Override
	public double evalEnergy(Value value);
	
	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * Adds additional energy to the normalization constant returned by {@link #getNormalizationEnergy()}.
	 * <p>
	 * This is equivalent to the following:
	 * <blockquote><pre>
	 * double energy = getNormalizationEnergy() + additionalEnergy;
	 * setNormalizationEnergy(energy);
	 * </pre></blockquote>
	 * <p>
	 * @param additionalEnergy must not be {@link Double#NaN}.
	 * @return the new normalization energy.
	 * @since 0.08
	 */
	public double addNormalizationEnergy(double additionalEnergy);
	
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
	 * Return energy normalization constant based on the parameters.
	 * <p>
	 * This may be subtracted from the energy returned by the {@link #evalEnergy} method to normalize
	 * the value so that it represents a negative log probability.
	 * <p>
	 * If not explicitly set via {@link #setNormalizationEnergy}, this value will be computed based
	 * on the parameters. Implementations should ensure that any changes to the parameters will cause
	 * this value to be recomputed. Once computed, {@link #addNormalizationEnergy} may be used to
	 * add to the normalization energy value. This can be used to transmit normalization information
	 * from other parts of the graph.
	 * <p>
	 * @since 0.08
	 */
	public double getNormalizationEnergy();
	
	/**
	 * Sets the energy normalization constant returned by {@link #getNormalizationEnergy()}.
	 * <p>
	 * @param energy is the new normalization energy. Setting to {@link Double#NaN}, will force
	 * value to be recomputed by {@link #getNormalizationEnergy()}.
	 * @since 0.08
	 */
	public void setNormalizationEnergy(double energy);
	
	/**
	 * True if parameters set to their "null" setting.
	 * @since 0.08
	 * @see #setNull()
	 */
	public boolean isNull();
	
	/**
	 * Sets parameter values from another message.
	 * <p>
	 * @param other a message of the same type as this one (although specific subclasses
	 * could support other message types).
	 * @throws ClassCastException if {@code other} does not have supported class type
	 * @throws IllegalArgumentException if {@code other} is otherwise not compatible
	 * with this message
	 * @since 0.08
	 */
	public void setFrom(IParameterizedMessage other);
	
	/**
	 * Sets message to its "null" form.
	 * <p>
	 * Sets parameters to values that will result in a zero energy for all (legal) inputs.
	 * <p>
	 * For exponential families, this will set the natural parameters to zero.
	 * <p>
	 * Usually, but not always, this means the parameters will be set to represent a weak uniform distribution.
	 * <p>
	 * @see #setUniform()
	 */
	public void setNull();
	
	/**
	 * Sets parameters to approximate a uniform distribution.
	 * <p>
	 * @since 0.08
	 */
	public void setUniform();
}
