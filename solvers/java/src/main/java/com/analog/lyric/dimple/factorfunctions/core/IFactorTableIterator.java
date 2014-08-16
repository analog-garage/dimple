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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Iterator over entries in a {@link IFactorTable}.
 * <p>
 * Supports several styles of iteration. The first uses the traditional {@link Iterator} approach:
 * <pre>
 *     while (iter.hasNext())
 *     {
 *        FactorTableEntry entry = iter.next();
 *        ... entry.weight() ...
 *     }
 * </pre>
 * You can also iterate simply by calling {@link #next()} and testing for null:
 * <pre>
 *     for (FactorTableEntry entry; (entry = iter.next()) != null;)
 *     {
 *        ... entry.weight() ...
 *     }
 * </pre>
 * If you want to avoid allocating a new entry object for each iteration, you can instead use
 * {@link #advance()} and get the current attributes directly from the iterator:
 * <pre>
 *     while (iter.advance())
 *     {
 *        ... iter.weight() ...
 *     }
 * </pre>
 * 
 * Iterators can be obtained from {@link IFactorTable#iterator()} and {@link IFactorTable#fullIterator()}.
 * 
 * @since 0.05
 */
@NotThreadSafe
public interface IFactorTableIterator extends Iterator<FactorTableEntry>
{
	/*------------------
	 * Iterator methods
	 */
	
	/**
	 * Returns the next entry in the iteration or else null.
	 * It is not necessary to invoke {@link #hasNext()} before this method.
	 * 
	 * @see #advance()
	 */
	@Override
	public abstract @Nullable FactorTableEntry next();
	
	/**
	 * Method not supported.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public abstract void remove();

	/*------------------------------
	 * IFactorTableIterator methods
	 */

	/**
	 * Advance to the next entry.
	 * 
	 * @return false if this hit the end of iteration.
	 * @see #next()
	 */
	public abstract boolean advance();

	/**
	 * Domains of underlying factor table.
	 */
	public abstract JointDomainIndexer domains();
	
	/**
	 * Returns current entry or null either if end of iteration has been reached or
	 * neither {@link #advance()} nor {@link #next()} has been called at least once.
	 */
	public abstract @Nullable FactorTableEntry getEntry();

	/**
	 * Returns the energy for the entry at the current iteration.
	 * <p>
	 * 
	 */
	public abstract double energy();

	/**
	 * Returns domain indices corresponding to the table entry at the current iteration in newly allocated array.
	 * 
	 * @see #indices(int[])
	 * @see #indicesUnsafe()
	 */
	public abstract int[] indices();
	
	/**
	 * Returns domain indices corresponding to the table entry at the current iteration using provided
	 * {@code indices} array if it is non-null and of the correct length.
	 * 
	 * @see #indices()
	 * @see #indicesUnsafe()
	 */
	public abstract int[] indices(@Nullable int[] array);

	/**
	 * Returns domain indices corresponding to the table entry at the current iteration in read-only array.
	 * <p>
	 * The returned array may return an internal representation and must not be modified!
	 * 
	 * @see #indices()
	 * @see #indices(int[])
	 */
	public abstract int[] indicesUnsafe();
	
	/**
	 * Returns the joint index for the entry at the current iteration or -1 if
	 * joint indices are not supported for this iterator.
	 */
	public abstract int jointIndex();

	/**
	 * Indicates whether the iterator will skip over table entries that have a zero weight
	 * (or infinite energy).
	 */
	public abstract boolean skipsZeroWeights();
	
	/**
	 * Returns the sparse index for the entry at the current iteration.
	 */
	public abstract int sparseIndex();

	/**
	 * Returns the weight for the entry at the current iteration.
	 */
	public abstract double weight();

}