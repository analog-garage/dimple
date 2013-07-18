package com.analog.lyric.dimple.FactorFunctions.core;

import java.io.Serializable;
import java.util.BitSet;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;

public interface INewFactorTableBase extends Cloneable, Serializable
{
	/**
	 * Returns a deep copy of this factor table.
	 */
	public abstract INewFactorTableBase clone();

	/**
	 * Deterministically computed output arguments from input arguments.
	 * <p>
	 * If table {@link #isDeterministicDirected()}, this method looks at the input arguments
	 * designated by the set bits of {@link #getInputSet()} and sets the remaining output arguments
	 * from them.
	 * @throws DimpleException not {@link #isDeterministicDirected()}.
	 */
	public abstract void evalDeterministic(Object[] arguments);
	
	/**
	 * Returns energy of factor table entry for given {@code arguments}.
	 * <p>
	 * @see #getEnergy(int...)
	 * @see #evalWeight(Object...)
	 */
	public abstract double evalEnergy(Object ... arguments);
	
	/**
	 * Returns energy of factor table entry for given {@code arguments}.
	 * <p>
	 * @see #getWeight(int...)
	 * @see #evalEnergy(Object...)
	 */
	public abstract double evalWeight(Object ... arguments);

	/**
	 * If {@link #isDirected()} returns object indicating the indices of the subset of dimensions/domains
	 * that represent inputs or the "from" size of the directionality. Returns null if table is not
	 * directed. The output set is simply the inverse of the input set (i.e. represented by the clear
	 * bits instead of the set bits). The input set should contain at least one set bit and at least one
	 * clear bit.
	 */
	public BitSet getInputSet();
	
	/**
	 * The number of domains defining the dimensions of this table.
	 * @see #getDomain(int)
	 * @see #getDomainSize(int)
	 */
	public abstract int getDomainCount();
	
	/**
	 * Returns the ith domain for this table.
	 * @param i must be non-negative and less than {@link #getDomainCount()}.
	 * @throws ArrayIndexOutOfBoundsException if {@code i} is out of range.
	 */
	public abstract DiscreteDomain getDomain(int i);
	
	/**
	 * Shorthand for {@link #getDomain}(i).size().
	 */
	public abstract int getDomainSize(int i);

	/**
	 * Returns energy of factor table entry at given {@code location}.
	 * <p>
	 * The energy is the same as the negative log of the weight for the same {@code location}.
	 * <p>
	 * @param location should be value less than {@link #size()} specifying which
	 * table entry to access.
	 * @return energy for entry if {@code location} is non-negative, otherwise returns positive infinity.
	 * @throws ArrayIndexOutOfBoundsException if {@code location} is not less than {@link #size()}.
	 * @see #getEnergy(int...)
	 * @see #getWeight(int)
	 */
	public abstract double getEnergy(int location);

	/**
	 * Returns the energy of factor table entry with given {@code indices}.
	 * <p>
	 * @see #evalEnergy(Object...)
	 * @see #getEnergy(int)
	 * @see #getWeight(int...)
	 */
	public abstract double getEnergy(int ... indices);
	
	/**
	 * Returns weight of factor table entry at given {@code location}.
	 * <p>
	 * @param location should be value less than {@link #size()} specifying which
	 * table entry to access.
	 * @return weight for entry if {@code location} is non-negative, otherwise returns zero.
	 * @throws ArrayIndexOutOfBoundsException if {@code location} is not less than {@link #size()}.
	 * @see #getWeight(int...)
	 * @see #getEnergy(int)
	 */
	public abstract double getWeight(int location);
	
	/**
	 * Returns the weight of factor table entry with given {@code indices}.
	 * <p>
	 * @see #evalWeight(Object...)
	 * @see #getWeight(int)
	 * @see #getEnergy(int...)
	 */
	public abstract double getWeight(int ... indices);

	/**
	 * True if table {@link #isDirected()} and has exactly one entry for each combination of
	 * input indices with a non-zero weight.
	 * @see #evalDeterministic(Object[])
	 */
	public abstract boolean isDeterministicDirected();
	
	/**
	 * True if table is directed, in which case {@link #getInputSet()} will be non-null.
	 */
	public abstract boolean isDirected();
	
	/**
	 * True if the table has been normalized by {@link #normalize()} or the equivalent.
	 */
	public abstract boolean isNormalized();

	/**
	 * Computes a unique joint index for the table entry associated with the specified arguments.
	 * <p>
	 * @param arguments must have length equal to {@link #getDomainCount()} and each argument must
	 * be an element of the corresponding domain.
	 * @see #jointIndexFromIndices(int ... )
	 * @see #jointIndexToArguments(int, Object[])
	 * @see #locationFromArguments(Object...)
	 */
	public abstract int jointIndexFromArguments(Object ... arguments);

	/**
	 * Computes a unique joint index for the table entry associated with the specified {@code indices}.
	 * 
	 * @param indices must have length equal to {@link #getDomainCount()} and each index must be a non-negative
	 * value less than the corresponding {@link #getDomainSize(int)} otherwise the function could return an
	 * incorrect result.
	 * @see #jointIndexFromArguments
	 * @see #jointIndexToIndices
	 * @see #locationFromIndices
	 */
	public abstract int jointIndexFromIndices(int ... indices);
	
	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * @param joint a unique joint table index in the range [0,{@link #jointSize()}).
	 * @param arguments an array of length {@link #getDomainCount()} into which the computed domain values
	 * will be written.
	 * @see #jointIndexToIndices(int, int[])
	 * @see #jointIndexFromArguments(Object...)
	 * @see #locationToArguments(int, Object[])
	 */
	public abstract void jointIndexToArguments(int joint, Object[] arguments);
	
	/**
	 * Computes domain indices corresponding to given joint index.
	 * <p>
	 * @param joint a unique joint table index in the range [0,{@link #jointSize()}).
	 * @param indices an array of length {@link #getDomainCount()} into which the computed indices
	 * will be written.
	 * @see #jointIndexToArguments(int, Object[])
	 * @see #jointIndexFromIndices(int...)
	 * @see #locationToIndices(int, int[])
	 */
	public abstract void jointIndexToIndices(int joint, int[] indices);
	
	/**
	 * The number of possible combinations of the values of all the domains in this table.
	 * Equivalent to the product of all of the {@link #getDomainSize(int)} values for this table.
	 * @see #size()
	 */
	public abstract int jointSize();
	
	/**
	 * Computes location index for the table entry associated with the specified arguments.
	 * <p>
	 * @param arguments must have length equal to {@link #getDomainCount()} and each argument must
	 * be an element of the corresponding domain.
	 * @see #locationFromIndices(int ... )
	 * @see #jointIndexToArguments(int, Object[])
	 * @see #locationFromArguments(Object...)
	 */
	public abstract int locationFromArguments(Object ... arguments);
	
	/**
	 * Computes a location index for the table entry associated with the specified {@code indices}.
	 * 
	 * @param indices must have length equal to {@link #getDomainCount()} and each index must be a non-negative
	 * value less than the corresponding {@link #getDomainSize(int)} otherwise the function could return an
	 * incorrect result.
	 * @see #locationFromArguments
	 * @see #locationToIndices
	 * @see #jointIndexFromIndices
	 */
	public abstract int locationFromIndices(int... indices);
	
	/**
	 * Converts joint index (oner per valid combination of domain indices) to location index
	 * (one per table entry.
	 * <p>
	 * @return if {@code joint} has a corresponding table entry its location is returned as
	 * a number in the range [0,{@link #size}), otherwise it returns -1-{@code location} where
	 * {@code location} is the location where the entry would be if it were in the table.
	 * @see #locationToJointIndex
	 */
	public abstract int locationFromJointIndex(int joint);
	
	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * @param location index in the range [0,{@link #size}).
	 * @param arguments an array of length {@link #getDomainCount()} into which the computed domain values
	 * will be written.
	 * @see #locationToIndices(int, int[])
	 * @see #locationFromArguments(Object...)
	 * @see #jointIndexToArguments(int, Object[])
	 */
	public abstract void locationToArguments(int location, Object[] arguments);
	
	/**
	 * Converts location index (one per table entry) to joint index (one per valid combination
	 * of domain indices).
	 * 
	 * @return joint location index in range [0,{@link #jointSize}).
	 * @see #locationFromJointIndex(int)
	 */
	public abstract int locationToJointIndex(int location);

	/**
	 * Computes domain indices corresponding to given location index.
	 * 
	 * @param location index in range [0,{@link #size}).
	 * @param indices an array of length {@link #getDomainCount()} into which the computed indices
	 * will be written.
	 * @see #locationToArguments(int, Object[])
	 * @see #locationFromIndices(int...)
	 * @see #jointIndexToIndices(int, int[])
	 */
	public abstract void locationToIndices(int location, int[] indices);

	/**
	 * Normalizes the weights/energies of the table.
	 * <p>
	 * If not {@link #isDirected()}, then this simply modifies the weights/energies of the
	 * table so that the weights add up to one. If table is directed then this instead
	 * makes sure that the weights of all entries with the same set of input indices
	 * adds up to one.
	 */
	public abstract void normalize();

	/**
	 * The number of entries in the table that can be accessed by a location index.
	 * This can be no larger than {@link #jointSize()} and if smaller, indicates that
	 * the table has a sparse representation that does not include combinations with
	 * zero weight/infinite energy.
	 * <p>
	 * When this value is the same as {@link #jointSize()} then it is expected that
	 * all of the {@code location*} methods will behave the same as the correspondingly
	 * named {@code jointIndex*} methods.
	 */
	public abstract int size();

}