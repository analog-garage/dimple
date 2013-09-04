package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;

import com.analog.lyric.dimple.model.JointDomainIndexer;
import com.analog.lyric.dimple.model.JointDomainReindexer;


public interface INewFactorTable extends INewFactorTableBase, IFactorTable
{
	/**
	 * Removes entries from sparse representation of table that have zero weights.
	 * <p>
	 * This may affect the {@link #sparseSize()} and the relationship between sparse and
	 * joint indexes.
	 * <p>
	 * @return the number of sparse entries that were removed.
	 */
	public int compact();
	
	@Override
	public INewFactorTable convert(JointDomainReindexer converter);
	
	public NewFactorTableRepresentation getRepresentation();

	public boolean hasDeterministicRepresentation();
	
	/**
	 * Returns the underlying array of sparse energies without copying for speed.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 */
	public double[] getEnergiesSparseUnsafe();

	/**
	 * Returns the underlying array of sparse weights without copying for speed.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 */
	public double[] getWeightsSparseUnsafe();
	
	/**
	 * Returns the underlying array of sparse element indices.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 */
	public int[][] getIndicesSparseUnsafe();
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DENSE_ENERGY} with
	 * provided energies.
	 * @param energies specifies the energies of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainIndexer()}.getCardinality().
	 */
	public void setEnergiesDense(double[] energies);
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DENSE_WEIGHT} with
	 * provided weights.
	 * @param weights specifies the weights of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainIndexer()}.getCardinality().
	 */
	public void setWeightsDense(double[] weights);

	/**
	 * Makes table directed and verifies that its weights are normalized correctly.
	 * <p>
	 * Similar to {@link #setDirected(BitSet)} but does not allow a null argument and
	 * will throw an exception if not {@link #isConditional()}.
	 */
	public void setConditional(BitSet outputSet);
	
	/**
	 * Makes table directed and normalizes its weights to conditional form if necessary.
	 * <p>
	 * Equivalent to calling {@link #setDirected(BitSet)} followed by {@link #normalizeConditional()}
	 * but requires that {@code outputSet} is non-null.
	 */
	public void setConditionalAndNormalize(BitSet outputSet);
	
	/**
	 * Designates directionality of factor table by specifying which of its domains are outputs.
	 * <p>
	 * Note that this may change the indexing of the table's values in both sparse and dense formats.
	 * You can only rely on the indexing not to change if all output domains are at the front of the
	 * domain list (i.e. {@link JointDomainIndexer#hasCanonicalDomainOrder()}).
	 * <p>
	 * @param outputSet if null, makes the table undirected otherwise this should turn on bits corresponding
	 * to the domains that are to be designated as outputs. The highest bit must be less than the number of
	 * domains ({@link #getDimensions()}).
	 */
	public void setDirected(BitSet outputSet);
	
	public void setRepresentation(NewFactorTableRepresentation representation);
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#SPARSE_ENERGY} with
	 * provided energies for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param energies specifies the energies of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setEnergiesSparse(int[] jointIndices, double[] energies);
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#SPARSE_WEIGHT} with
	 * provided weights for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setWeightsSparse(int[] jointIndices, double[] weights);
}
