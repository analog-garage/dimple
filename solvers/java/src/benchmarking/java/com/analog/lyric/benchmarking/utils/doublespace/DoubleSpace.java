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

package com.analog.lyric.benchmarking.utils.doublespace;

import java.util.Collection;
import java.util.Set;

import com.analog.lyric.benchmarking.utils.functional.BinaryOp;
import com.analog.lyric.benchmarking.utils.functional.FoldFunction;
import com.analog.lyric.benchmarking.utils.functional.IterFunctionWithCoordinates;
import com.analog.lyric.benchmarking.utils.functional.TransformFunction;

/**
 * Represents a multidimensional array of doubles.
 */
public interface DoubleSpace
{
	/**
	 * Adds each entry in another DoubleSpace to those in this DoubleSpace. The
	 * two objects must have domains of the same size.
	 * 
	 * @param that
	 *            Another DoubleSpace, whose values are added to this.
	 * @return Returns this.
	 */
	public abstract DoubleSpace add(DoubleSpace that);

	/**
	 * Applies a BinaryOp to each element of this DoubleSpace and another,
	 * placing the result in this DoubleSpace. The two object smust have domains
	 * of the same size.
	 * 
	 * @param that
	 *            Another DoubleSpace.
	 * @param op
	 *            The operation to apply to each element.
	 * @return Returns this.
	 */
	public abstract DoubleSpace binaryOp(DoubleSpace that, BinaryOp op);

	/**
	 * @return Gets a set containing the coordinates of every entry in this
	 *         collection.
	 */
	public abstract Set<int[]> coordinatesSet();

	/**
	 * @return Gets a set containing an Entry for each entry in this collection.
	 */
	public abstract Set<Entry> entrySet();

	/**
	 * Performs a fold over the entries in this collection.
	 * 
	 * @param init
	 *            The initial accumulator value.
	 * @param fn
	 *            The fold operation.
	 * @return The final value of the accumulator.
	 */
	public abstract <T> T fold(T init, FoldFunction<T> fn);

	/**
	 * Gets the value stored at a given coordinate position.
	 * 
	 * @param coordinates
	 *            The coordinates of the value to get.
	 * @return The value stored at the given coordinates.
	 */
	public abstract double get(int... coordinates);

	/**
	 * Gets the quantity of entries in this collection.
	 */
	public abstract int getCardinality();

	/**
	 * Gets an array containing all of this container's dimensions' orders.
	 */
	public abstract int[] getDimensions();

	/**
	 * Gets the quantity of dimensions of this collection.
	 */
	public abstract int getDimensionsCount();

	/**
	 * Gets the Indexer for a given dimension.
	 * 
	 * @param dimension
	 *            A zero-based dimension index.
	 * @return The requested Indexer.
	 */
	public abstract Indexer getIndexer(int dimension);

	/**
	 * Iterates a function over all elements in this collection.
	 * 
	 * @param fn
	 *            The iteration function to apply to the elements.
	 */
	public abstract void iter(IterFunctionWithCoordinates fn);

	/**
	 * Puts a value into this collection at the supplied coordinates.
	 * 
	 * @param value
	 *            The value to store.
	 * @param coordinates
	 *            The coordinates at which to store the value.
	 */
	public abstract void put(double value, int... coordinates);

	/**
	 * Replaces each entry in this collection with the value computed by a
	 * transformation function.
	 * 
	 * @param transformer
	 *            The transform to apply to each element's value.
	 * @return Returns this object.
	 */
	public abstract DoubleSpace transform(TransformFunction transformer);

	/**
	 * Gets a collection of all values contained by this collection. As this is
	 * not a Set, duplicates may occur.
	 */
	public abstract Collection<Double> values();

	/**
	 * Gets a DoubleSpace that accesses a portion of this one. The returned
	 * object shares the same underlying values as this one, so changes in one
	 * are observable in the other.
	 * 
	 * The provided indexers apply, in order, to the dimensions of this
	 * collection.
	 * 
	 * If a provided indexer has order of 1, then the returned DoubleSpace does
	 * not represent that dimension. For example, accessing a 2-dimensional
	 * doublespace with view(just(2), range(0, 3)) yields a vector.
	 * 
	 * If the provided indexers are fewer than this collection's dimensions,
	 * then the missing indexers take on the domain of the corresponding
	 * dimensions.
	 * 
	 * If an indexer repeats an index (for example, list(1, 1)), then the same
	 * entries in this collection appear in multiple locations in the returned
	 * collection. If a mutating operation, such as modify, is then applied to
	 * the returned collection, then the modification is applied multiple times.
	 * 
	 * @param indexers
	 *            An array of indexers.
	 * @return A view of a portion of this collection's entries.
	 */
	public abstract DoubleSpace view(Indexer... indexers);

	/**
	 * Represents a pair of element coordinates and value. Yielded by entrySet.
	 */
	public final class Entry
	{
		private final int[] _coordinates;

		private final DoubleSpace _doubleSpace;

		/**
		 * Constructs an entry. The value reflects and effects changes in the
		 * underlying collection.
		 * 
		 * @param doubleSpace
		 *            The DoubleSpace that contains this entry.
		 * @param coordinates
		 *            The coordinates of this entry.
		 */
		public Entry(DoubleSpace doubleSpace, int... coordinates)
		{
			_doubleSpace = doubleSpace;
			_coordinates = coordinates;
		}

		/**
		 * @return The coordinates of this entry.
		 */
		public int[] getCoordinates()
		{
			return _coordinates.clone();
		}

		/**
		 * @return The current value of this entry's coordinates in the
		 *         underlying collection.
		 */
		public double getValue()
		{
			return _doubleSpace.get(_coordinates);
		}

		/**
		 * @param value
		 *            The value to be stored in this entry's coordinates in the
		 *            underlying collection.
		 */
		public void setValue(double value)
		{
			_doubleSpace.put(value, _coordinates);
		}
	}

}
