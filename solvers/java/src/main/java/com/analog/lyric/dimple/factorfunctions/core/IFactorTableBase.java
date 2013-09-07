package com.analog.lyric.dimple.factorfunctions.core;

import java.io.Serializable;
import java.util.BitSet;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.JointDomainIndexer;
import com.analog.lyric.dimple.model.JointDomainReindexer;

public interface IFactorTableBase extends Cloneable, Serializable, Iterable<FactorTableEntry>
{
	/*------------------
	 * Iterator methods
	 */
	
	/**
	 * Returns an iterator over the non-zero entries in the table in increasing order
	 * of sparse/joint index.
	 */
	@Override
	public abstract FactorTableIterator iterator();
	
	/**
	 * Returns an iterator over the joint indexes in the table in increasing order.
	 */
	public abstract FactorTableIterator fullIterator();
	
	/*-------------
	 * New methods
	 */
	
	/**
	 * Returns a deep copy of this factor table.
	 */
	public abstract IFactorTableBase clone();

	/**
	 * Computes the number of entries in the table with non-zero weight (or non-infinite energy).
	 */
	public int countNonZeroWeights();
	
	/**
	 * Returns a new factor table converted from this one using the specified converter.
	 */
	public IFactorTableBase convert(JointDomainReindexer converter);
	
	/**
	 * That ratio of non-zero weights to {@link #jointSize()}. Will be 1.0 if table contains
	 * no entries with zero weight.
	 */
	public double density();
	
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
	 * The number of dimensions in the table.
	 * <p>
	 * The same as {@link #getDomainIndexer()}.size().
	 */
	public abstract int getDimensions();
	
	/**
	 * If {@link #isDirected()} returns object indicating the indices of the subset of dimensions/domains
	 * that represent inputs or the "from" size of the directionality. Returns null if table is not
	 * directed. The output set is simply the inverse of the input set (i.e. represented by the clear
	 * bits instead of the set bits). The input set should contain at least one set bit and at least one
	 * clear bit.
	 */
	public BitSet getInputSet();
	
	/**
	 * Returns energy for given set of indices assuming that the table has a dense
	 * representation for energies. This provides the fastest possible lookup for energies.
	 * <p>
	 * @throws ArrayIndexOutOfBoundsException may throw if {@code indices} are out of bound
	 * or table does not have dense energies.
	 * @see #hasDenseEnergies()
	 * @see #getEnergyForIndices(int...)
	 * @see #getWeightForIndicesDense(int...)
	 */
	public double getEnergyForIndicesDense(int ... indices);

	/**
	 * Returns weight for given set of indices assuming that the table has a dense
	 * representation for weights. This provides the fastest possible lookup for weights.
	 * <p>
	 * @throws ArrayIndexOutOfBoundsException may throw if {@code indices} are out of bound
	 * or table does not have dense weights.
	 * @see #hasDenseWeights()
	 * @see #getWeightForIndices(int...)
	 * @see #getEnergyForIndicesDense(int...)
	 */
	public double getWeightForIndicesDense(int ... indices);

	public abstract JointDomainIndexer getDomainIndexer();
	
	/**
	 * Returns energy of factor table entry for given {@code elements}.
	 * <p>
	 * @see #getEnergyForIndices(int...)
	 * @see #getWeightForElements(Object...)
	 */
	public abstract double getEnergyForElements(Object ... elements);
	
	public abstract double getEnergyForJointIndex(int jointIndex);

	/**
	 * Returns energy of factor table entry at given {@code sparseIndex}.
	 * <p>
	 * The energy is the same as the negative log of the weight for the same {@code sparseIndex}.
	 * <p>
	 * @param sparseIndex should be value less than {@link #sparseSize()} specifying which
	 * table entry to access.
	 * @throws ArrayIndexOutOfBoundsException if {@code sparseIndex} is not in range [0,{@link #sparseSize}).
	 * @see #getEnergyForIndices(int...)
	 * @see #getWeightForSparseIndex(int)
	 */
	public abstract double getEnergyForSparseIndex(int sparseIndex);

	/**
	 * Returns the energy of factor table entry with given {@code indices}.
	 * <p>
	 * @see #getEnergyForElements(Object...)
	 * @see #getEnergyForSparseIndex(int)
	 * @see #getWeightForIndices(int...)
	 */
	public abstract double getEnergyForIndices(int ... indices);
	
	/**
	 * Returns energy of factor table entry for given {@code elements}.
	 * <p>
	 * @see #getWeightForIndices(int...)
	 * @see #getEnergyForElements(Object...)
	 */
	public abstract double getWeightForElements(Object ... elements);

	public abstract double getWeightForJointIndex(int jointIndex);
	
	/**
	 * Returns weight of factor table entry at given {@code sparseIndex}.
	 * <p>
	 * @param sparseIndex should be value less than {@link #sparseSize()} specifying which
	 * table entry to access.
	 * @throws ArrayIndexOutOfBoundsException if {@code sparseIndex} is not in range [0, {@link #sparseSize}).
	 * @see #getWeightForIndices(int...)
	 * @see #getEnergyForSparseIndex(int)
	 */
	public abstract double getWeightForSparseIndex(int sparseIndex);
	
	/**
	 * Returns the weight of factor table entry with given {@code indices}.
	 * <p>
	 * @see #getWeightForElements(Object...)
	 * @see #getWeightForSparseIndex(int)
	 * @see #getEnergyForIndices(int...)
	 */
	public abstract double getWeightForIndices(int ... indices);

	public abstract boolean hasDenseRepresentation();
	public abstract boolean hasDenseEnergies();
	public abstract boolean hasDenseWeights();
	public abstract boolean hasSparseRepresentation();
	public abstract boolean hasSparseEnergies();
	public abstract boolean hasSparseWeights();
	
	/**
	 * True if table {@link #isDirected()} and has exactly one entry for each combination of
	 * input indices with a non-zero weight.
	 * @see #evalDeterministic(Object[])
	 */
	public abstract boolean isDeterministicDirected();
	
	/**
	 * True if table is in form appropriate for conditional distribution, i.e. if it is directed and the
	 * sum of the weights of entries for any given combination of input elements is a constant.
	 * <p>
	 * @see #isDirected()
	 * @see #normalizeConditional()
	 */
	public abstract boolean isConditional();
	
	/**
	 * True if table has designated input/output domains directed, in which case
	 * {@link #getInputSet()} will be non-null.
	 * <p>
	 * For most applications the table should have weights normalized so that {@link #isConditional()}
	 * also is true.
	 */
	public abstract boolean isDirected();
	
	/**
	 * True if the table is not directed and its weights add up to 1.0
	 * <p>
	 * @see #isDirected()
	 */
	public abstract boolean isNormalized();

	/**
	 * The number of possible combinations of the values of all the domains in this table.
	 * Same as {@link JointDomainIndexer#getCardinality()} of {@link #getDomainIndexer()}.
	 * @see #sparseSize()
	 */
	public abstract int jointSize();
	
	/**
	 * Computes sparse index for the table entry associated with the specified arguments.
	 * <p>
	 * @param elements must have length equal to {@link #getDimensions()} and each argument must
	 * be an element of the corresponding domain.
	 * @see #sparseIndexFromIndices(int ... )
	 * @see #sparseIndexFromElements(Object...)
	 */
	public abstract int sparseIndexFromElements(Object ... elements);
	
	/**
	 * Computes a sparse index for the table entry associated with the specified {@code indices}.
	 * 
	 * @param indices must have length equal to {@link #getDimensions()} and each index must be a non-negative
	 * value less than the corresponding domain size otherwise the function could return an
	 * incorrect result.
	 * @see #sparseIndexFromElements
	 * @see #sparseIndexToIndices
	 */
	public abstract int sparseIndexFromIndices(int... indices);
	
	/**
	 * Converts joint index (oner per valid combination of domain indices) to sparse index.
	 * <p>
	 * @return if {@code joint} has a corresponding table entry its location is returned as
	 * a number in the range [0,{@link #sparseSize}), otherwise it returns -1-{@code location} where
	 * {@code location} is the location where the entry would be if it were in the table.
	 * @see #sparseIndexToJointIndex
	 */
	public abstract int sparseIndexFromJointIndex(int joint);
	
	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * @param sparseIndex index in the range [0,{@link #sparseSize}).
	 * @param elements if this is an array of length {@link #getDimensions()}, the computed values will
	 * be placed in this array, otherwise a new array will be allocated.
	 * @see #sparseIndexToIndices(int, int[])
	 * @see #sparseIndexFromElements(Object...)
	 */
	public abstract Object[] sparseIndexToElements(int sparseIndex, Object[] elements);
	
	/**
	 * Converts sparse index (one per table entry) to joint index (one per valid combination
	 * of domain indices).
	 * <p>
	 * The sparse and joint index values should have the same ordering relationship, so that
	 * <pre>
	 *   sparse1 < sparse2</pre>
	 * implies that
	 * <pre>
	 *    t.sparseIndexToJointIndex(sparse1) < t.sparseIndexToJointIndex(sparse2)
	 * </pre>
	 * <p>
	 * @return joint index in range [0,{@link #jointSize}).
	 * @see #sparseIndexFromJointIndex(int)
	 */
	public abstract int sparseIndexToJointIndex(int sparseIndex);

	/**
	 * Computes domain indices corresponding to given sparse index.
	 * 
	 * @param sparseIndex index in range [0,{@link #sparseSize}).
	 * @param indices if this is an array of length {@link #getDimensions()}, the computed values will
	 * be placed in this array, otherwise a new array will be allocated.
	 * @see #sparseIndexToElements(int, Object[])
	 * @see #sparseIndexFromIndices(int...)
	 */
	public abstract int[] sparseIndexToIndices(int sparseIndex, int[] indices);
	
	public abstract int[] sparseIndexToIndices(int sparseIndex);

	/**
	 * Normalizes the weights/energies of the table by dividing by a constant to ensure that
	 * weights add up to one.
	 * <p>
	 * @throws UnsupportedOperationException if {@link #isDirected()}, use {@link #normalizeConditional()} instead.
	 * <p>
	 * @see #isNormalized()
	 */
	public abstract void normalize();
	
	/**
	 * Normalizes the weights/energies of directed table to ensure that weights applicable to any
	 * combination of input elements add up to one.
	 * <p>
	 * @throws UnsupportedOperationException if not {@link #isDirected()}, use {@link #normalize()} instead.
	 * <p>
	 * @see #isConditional()
	 */
	public abstract void normalizeConditional();

	public void setEnergyForElements(double energy, Object ... elements);
	public void setEnergyForIndices(double energy, int ... indices);
	public void setEnergyForSparseIndex(double energy, int sparseIndex);
	public void setEnergyForJointIndex(double energy, int jointIndex);

	public void setWeightForElements(double weight, Object ... elements);
	public void setWeightForIndices(double weight, int ... indices);
	public void setWeightForSparseIndex(double weight, int sparseIndex);
	public void setWeightForJointIndex(double weight, int jointIndex);
	
	/**
	 * The number of entries in the table that can be accessed by a sparse index.
	 * This can be no larger than {@link #jointSize()} and if smaller, indicates that
	 * the table has a sparse representation that does not include combinations with
	 * zero weight/infinite energy. The actual number of non-zero weight entries may
	 * be less than the sparse size.
	 */
	public abstract int sparseSize();

}