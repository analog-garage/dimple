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

package com.analog.lyric.collect;

import java.util.BitSet;

public class BitSetUtil
{

	/**
	 * Constructs a new {@link BitSet} of given {@code size} whose set bits are specified
	 * by {@code indices}.
	 * 
	 * @throws IndexOutOfBoundsException if any index is negative or not less than {@code size}.
	 * @throws IllegalArgumentException if any index is specified more than once.
	 * @see #bitsetToIndices
	 */
	public static BitSet bitsetFromIndices(int size, int ... indices)
	{
		final BitSet bitset = new BitSet(size);
		
		for (int index : indices)
		{
			if (index < 0 || index >= size)
			{
				throw new IndexOutOfBoundsException(String.format("Index %d out of range [0, %d]", index, size - 1));
			}
		
			if (bitset.get(index))
			{
				throw new IllegalArgumentException(String.format("Duplicate index %d", index));
			}
		
			bitset.set(index);
		}
		
		return bitset;
	}
	
	/**
	 * Returns a newly constructed array containing the indices of the non-zero bits.
	 * @since 0.07
	 * @see #bitsetFromIndices
	 */
	public static int[] bitsetToIndices(BitSet bits)
	{
		final int[] indices = new int[bits.cardinality()];
		for (int i = 0, index = -1, end = indices.length; i < end; ++i)
		{
			index = bits.nextSetBit(++index);
			indices[i] = index;
		}
		return indices;
	}

	/**
	 * Sets bit at {@code index} in {@code bits} to zero.
	 * 
	 * @param bits
	 * @param index must be in the range [0,31] identifies which bit to clear.
	 * @since 0.05
	 */
	public static int clearBit(int bits, int index)
	{
		return bits & ~(1 << index);
	}
	
	/**
	 * Sets bit at {@code index} in {@code bits} to zero.
	 * 
	 * @param bits
	 * @param index must be in the range [0,63] identifies which bit to clear.
	 * @since 0.05
	 */
	public static long clearBit(long bits, int index)
	{
		return bits & ~(1L << index);
	}
	
	/**
	 * @since 0.05
	 */
	public static int clearMask(int bits, int mask)
	{
		return bits & ~mask;
	}
	
	/**
	 * @since 0.05
	 */
	public static long clearMask(long bits, long mask)
	{
		return bits & ~mask;
	}
	
	/**
	 * Returns masked bits of value (i.e. "bitwise and" of two values)
	 * 
	 * @param word is the original word.
	 * @param mask specifies the bits of {@code word} to be returned.
	 *
	 * @since 0.06
	 */
	public static int getMaskedValue(int word, int mask)
	{
		return word & mask;
	}
	
	/**
	 * Returns masked bits of value (i.e. "bitwise and" of two values)
	 * 
	 * @param word is the original word.
	 * @param mask specifies the bits of {@code word} to be returned.
	 *
	 * @since 0.06
	 */
	public static long getMaskedValue(long word, long mask)
	{
		return word & mask;
	}
	
	/**
	 * True if bit at {@code index} in {@code bits} is one.
	 * 
	 * @param bits
	 * @param index must be in the range [0,31] identifies which bit to clear.
	 */
	public static boolean isBitSet(int bits, int index)
	{
		return (bits & (1 << index)) != 0;
	}
	
	/**
	 * True if bit at {@code index} in {@code bits} is one.
	 * 
	 * @param bits
	 * @param index must be in the range [0,63] identifies which bit to clear.
	 */
	public static boolean isBitSet(long bits, int index)
	{
		return (bits & (1L << index)) != 0;
	}
	
	/**
	 * True if all bits in {@code mask} are set in {@code bits}.
	 */
	public static boolean isMaskSet(int bits, int mask)
	{
		return (bits & mask) == mask;
	}
	
	/**
	 * True if all bits in {@code mask} are set in {@code bits}.
	 */
	public static boolean isMaskSet(long bits, long mask)
	{
		return (bits & mask) == mask;
	}
	
	/**
	 * Sets bit at {@code index} in {@code bits} to one.
	 * 
	 * @param bits
	 * @param index must be in the range [0,31] identifies which bit to clear.
	 */
	public static int setBit(int bits, int index)
	{
		return bits | (1 << index);
	}

	/**
	 * Sets bit at {@code index} in {@code bits} to one.
	 * 
	 * @param bits
	 * @param index must be in the range [0,63] identifies which bit to clear.
	 */
	public static long setBit(long bits, int index)
	{
		return bits | (1L << index);
	}
	
	/**
	 * Sets all bits in {@code mask} in {@code bits} (i.e. "bitwise or" of two values).
	 */
	public static int setMask(int bits, int mask)
	{
		return bits | mask;
	}

	/**
	 * Sets all bits in {@code mask} in {@code bits} (i.e. "bitwise or" of two values).
	 */
	public static long setMask(long bits, long mask)
	{
		return bits | mask;
	}
	
	/**
	 * Sets a range of bits to specified value.
	 * 
	 * @param word is the original word.
	 * @param mask specifies which bits in {@code word} will be set.
	 * @param value must only use bits that are set in {@code mask} or else an incorrect value will be returned.
	 *
	 * @since 0.06
	 */
	public static int setMaskedValue(int word, int mask, int value)
	{
		return (word & ~mask) | value;
	}
	
	/**
	 * Sets a range of bits to specified value.
	 * 
	 * @param word is the original word.
	 * @param mask specifies which bits in {@code word} will be set.
	 * @param value must only use bits that are set in {@code mask} or else an incorrect value will be returned.
	 *
	 * @since 0.06
	 */
	public static long setMaskedValue(long word, long mask, long value)
	{
		return (word & ~mask) | value;
	}
}
