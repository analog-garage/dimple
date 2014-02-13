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
	 * Computes map of indexes into {@code bitset} that indicates the nth set or clear bit. If
	 * the value {@code n} of {@code indexMap[i]} is non-negative, then bit {@code i} is the {@code nth}
	 * set bit (counting from zero), and if negative then {@code i} is the {@code (-1-n)th} clear bit.
	 * 
	 * @param bitset
	 * @param indexMap is an array whose length must equal {@code bitset.size()} and whose values
	 * will computed by this function.
	 * @return the number of bits set in {@code bitset}.
	 */
	public static int bitsetToIndexMap(BitSet bitset, int[] indexMap)
	{
		int nTrue = 0;
		for (int i = 0, end = indexMap.length; i < end; ++i)
		{
			indexMap[i] = bitset.get(i) ? nTrue++ : nTrue-i-1;
		}
		return nTrue;
	}

	/**
	 * Sets bit at {@code index} in {@code bits} to zero.
	 * 
	 * @param bits
	 * @param index must be in the range [0,31] identifies which bit to clear.
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
	 */
	public static long clearBit(long bits, int index)
	{
		return bits & ~(1L << index);
	}
	
	public static int clearMask(int bits, int mask)
	{
		return bits & ~mask;
	}
	
	public static long clearMask(long bits, long mask)
	{
		return bits & ~mask;
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
	
	public static boolean isMaskSet(int bits, int mask)
	{
		return (bits & mask) == mask;
	}
	
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
	
	public static int setMask(int bits, int mask)
	{
		return bits | mask;
	}

	public static long setMask(long bits, long mask)
	{
		return bits | mask;
	}
}
