package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;
import java.util.Random;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.JointDiscreteDomain;
import com.analog.lyric.dimple.model.JointDomainIndexer;
import com.analog.lyric.dimple.model.JointDomainReindexer;


public interface IFactorTable extends IFactorTableBase
{
	@Override
	public IFactorTable clone();
	
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
	public IFactorTable convert(JointDomainReindexer converter);
	
	public void copy(IFactorTable that);

	public IFactorTable createTableWithNewVariables(DiscreteDomain[] newDomains);

	public FactorTableRepresentation getRepresentation();

	/**
	 * Creates a new factor table that replaces two or more of its domains with
	 * a joint domain.
	 * <p>
	 * @param varIndices contains the indices of the domains to be joined. Must have at least two entries
	 * in the range [0,{@link #getDimensions()}-1].
	 * @param indexToJointIndex specifies the order in which the joined domains are to be incorporated
	 * into the new joint domain.
	 * @param allDomains is the list of all domains before joining
	 * @param jointDomain is the new joined domain. Its size must match the product of the sizes of the
	 * joined domains, and it is expected to be of type {@link JointDiscreteDomain}. It will be the
	 * last domain in the new table's domain list.
	 */
	// Only used by DiscreteFactor.replaceVariablesWithJoint
	public IFactorTable joinVariablesAndCreateNewTable(
		int [] varIndices,
		int [] indexToJointIndex,
		DiscreteDomain [] allDomains,
		DiscreteDomain jointDomain); // REFACTOR: keep (for now)

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
	
	public boolean hasSparseIndices();
	
	public void replaceEnergiesSparse(double[] energies);
	public void replaceWeightsSparse(double[] weights);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#DENSE_ENERGY} with
	 * provided energies.
	 * @param energies specifies the energies of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainIndexer()}.getCardinality().
	 */
	public void setEnergiesDense(double[] energies);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#DENSE_WEIGHT} with
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
	public void makeConditional(BitSet outputSet);
	
	public void randomizeWeights(Random rand);

	public void serializeToXML(String serializeName, String targetDirectory);
	
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
	
	public void setRepresentation(FactorTableRepresentation representation);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_ENERGY} with
	 * provided energies for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param energies specifies the energies of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setEnergiesSparse(int[] jointIndices, double[] energies);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_WEIGHT} with
	 * provided weights for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 * @see #setWeightsSparse(int[][], double[])
	 */
	public void setWeightsSparse(int[] jointIndices, double[] weights);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_WEIGHT_WITH_INDICES} with
	 * provided weights for each set of indices.
	 * <p>
	 * @param indices are the combined element indices of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code indices}.
	 * @throws IllegalArgumentException if {@code indices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 * @see #setWeightsSparse(int[], double[])
	 */
	public void setWeightsSparse(int[][] indices, double[] weights);
}
