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

import java.util.BitSet;
import java.util.Random;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.values.Value;
import org.eclipse.jdt.annotation.Nullable;

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

	/**
	 * Constructs a new factor table by conditioning one or more dimensions
	 * conditioned on specified values.
	 * <p>
	 * The new table will not be directed and will have the same representation as the original
	 * table except that {@link FactorTableRepresentation#DETERMINISTIC} will become
	 * {@link FactorTableRepresentation#SPARSE_ENERGY}.
	 * <p>
	 * @param valueIndices is an array of length {@link #getDimensions()} that specifies which
	 * dimensions are to be conditioned away. Each entry in the array should either be
	 * a negative value if the dimension is to be retained, or a non-negative value in
	 * the range [0, <i>dimension-size</i> - 1].
	 * 
	 * @since 0.05
	 */
	public IFactorTable createTableConditionedOn(int[] valueIndices);

	/**
	 * Constructs a new factor table by appending dimensions for the specified {@code newDomains}.
	 */
	public IFactorTable createTableWithNewVariables(DiscreteDomain[] newDomains);

	/**
	 * Returns an enum indicating the underlying representation of the factor table.
	 * <p>
	 * The representation may be changed via {@link #setRepresentation(FactorTableRepresentation)} but
	 * can also be changed implicitly as a side effect of certain other methods:
	 * <ul>
	 * <li>{@link #getEnergiesSparseUnsafe()}
	 * <li>{@link #getEnergyForSparseIndex(int)}
	 * <li>{@link #getIndicesSparseUnsafe()}
	 * <li>{@link #getWeightForSparseIndex(int)}
	 * <li>{@link #getWeightsSparseUnsafe()}
	 * <li>{@link #setEnergyForJointIndex(double, int)}
	 * <li>{@link #setEnergyForSparseIndex(double, int)}
	 * <li>{@link #setWeightForJointIndex(double, int)}
	 * <li>{@link #setWeightForSparseIndex(double, int)}
	 * <li>{@link #sparseIndexFromJointIndex(int)}
	 * <li>{@link #sparseIndexToJointIndex(int)}
	 * </ul>
	 */
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

	/**
	 * True if table currently has a deterministic directed representation (either
	 * {@link FactorTableRepresentation#DETERMINISTIC} or {@link FactorTableRepresentation#DETERMINISTIC_WITH_INDICES}).
	 * Unlike invoking {@link #isDeterministicDirected()}, this will not check the values of the table
	 * or change the representation.
	 */
	public boolean hasDeterministicRepresentation();
	
	/**
	 * Returns the underlying array of sparse energies without copying for speed.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 * <p>
	 * If necessary, this method will implicitly modify the representation to include sparse energies
	 * (@link {@link #hasSparseEnergies()}).
	 * <p>
	 * @see #getWeightsSparseUnsafe()
	 */
	public double[] getEnergiesSparseUnsafe();
	
	/**
	 * Returns the underlying array of dense energies without copying for speed.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 * <p>
	 * If necessary, this method will implicitly modify the representation to include dense weights
	 * <p>
	 * @since 0.07
	 */
	public double[] getEnergiesDenseUnsafe();

	/**
	 * Returns an array of energies for the {@code sliceDimension} of the factor table with all other
	 * dimensions fixed to provided values.
	 * <p>
	 * Same as {@link #getEnergySlice(double[], int, int[])} but always allocates a new array.
	 * <p>
	 * @see #getWeightSlice(int, int[])
	 */
	public double[] getEnergySlice(int sliceDimension, int ... indices);
	
	public double[] getEnergySlice(int sliceDimension, Value ... values);
	
	/**
	 * Returns an array of energies for the {@code sliceDimension} of the factor table with all other
	 * dimensions fixed to provided values.
	 * 
	 * @param sliceDimension is an integer in the range [0, {@link #getDimensions()}-1] that identifies which
	 * domain the slice is for.
	 * @param indices specifies the element indices that are to be fixed. The element index at position
	 * {@code sliceDimension} will be ignored.
	 * @param slice if non-null and large enough to accommodate the values will be used as the return value,
	 * otherwise a new array will be allocated.
	 * @return an array of energy values with size equal to the size of the {@code sliceDimension} of
	 * the table (i.e. {@code getDomainIndexer().getDomainSize(sliceDimension)}) holding the energy values
	 * from the table
	 * <p>
	 * @see #getEnergySlice(int, int[])
	 * @see #getWeightSlice(double[], int, int[])
	 */
	public double[] getEnergySlice(@Nullable double[] slice, int sliceDimension, int ... indices);
	
	public double[] getEnergySlice(@Nullable double[] slize, int sliceDimension, Value ... values);
	
	/**
	 * Returns the underlying array of sparse element indices.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 * <p>
	 * If necessary, this method will implicitly modify the representation to include sparse indices
	 * (see {@link #hasSparseIndices()}) and if the table did not previously only had a dense value
	 * representation (see {@link #hasDenseRepresentation()}) it will implicitly add a sparse
	 * representation introducing sparse weights if the table had dense weights and otherwise introducing
	 * sparse energies.
	 */
	public int[][] getIndicesSparseUnsafe();
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If necessary, this method will implicitly modify the representation to include a sparse
	 * form if it previously only held dense values; in this case, if the table only held dense energies, then
	 * sparse energies will be added, otherwise sparse weights will be added.
	 */
	@Override
	public double getWeightForSparseIndex(int sparseIndex);
	
	/**
	 * Returns the underlying array of sparse weights without copying for speed.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 * <p>
	 * If necessary, this method will implicitly modify the representation to include sparse weights
	 * (@link {@link #hasSparseEnergies()}).
	 * <p>
	 * @see #getEnergiesSparseUnsafe()
	 */
	public double[] getWeightsSparseUnsafe();

	/**
	 * Returns the underlying array of dense weights without copying for speed.
	 * <p>
	 * <b>IMPORTANT</b>: modifying the contents of the array may put the factor table into
	 * an invalid state. This should be treated as a read-only value.
	 * <p>
	 * If necessary, this method will implicitly modify the representation to include dense weights
	 * <p>
	 * @since 0.06
	 */
	public double[] getWeightsDenseUnsafe();
	
	/**
	 * Returns an array of weights for the {@code sliceDimension} of the factor table with all other
	 * dimensions fixed to provided values.
	 * <p>
	 * Same as {@link #getWeightSlice(double[], int, int[])} but always allocates a new array.
	 * <p>
	 * @see #getEnergySlice(int, int[])
	 */
	public double[] getWeightSlice(int sliceDimension, int ... indices);
	
	public double[] getWeightSlice(int sliceDimension, Value ... values);
	
	/**
	 * Returns an array of weights for the {@code sliceDimension} of the factor table with all other
	 * dimensions fixed to provided values.
	 * 
	 * @param sliceDimension is an integer in the range [0, {@link #getDimensions()}-1] that identifies which
	 * domain the slice is for.
	 * @param indices specifies the element indices that are to be fixed. The element index at position
	 * {@code sliceDimension} will be ignored.
	 * @param slice if non-null and large enough to accommodate the values will be used as the return value,
	 * otherwise a new array will be allocated.
	 * @return an array of weight values with size equal to the size of the {@code sliceDimension} of
	 * the table (i.e. {@code getDomainIndexer().getDomainSize(sliceDimension)}) holding the weight values
	 * from the table
	 * <p>
	 * @see #getWeightSlice(int, int[])
	 * @see #getEnergySlice(double[], int, int[])
	 */
	public double[] getWeightSlice(@Nullable double[] slice, int sliceDimension, int ... indices);
	
	public double[] getWeightSlice(@Nullable double[] slice, int sliceDimension, Value ... values);
	
	/**
	 * True if the current factor table representation supports {@link #getIndicesSparseUnsafe}.
	 */
	public boolean hasSparseIndices();
	
	/**
	 * Replace the energies/weights of the table from the given array of {@code energies} in
	 * order of sparse index. This does not alter the sparse to joint index mapping.
	 * <p>
	 * @see #replaceWeightsSparse(double[])
	 */
	public void replaceEnergiesSparse(double[] energies);
	
	/**
	 * Replace the energies/weights of the table from the given array of {@code weights} in
	 * order of sparse index. This does not alter the sparse to joint index mapping.
	 * <p>
	 * @see #replaceEnergiesSparse(double[])
	 */
	public void replaceWeightsSparse(double[] weights);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#DENSE_ENERGY} with
	 * provided energies replacing the existing contents of the table.
	 * @param energies specifies the energies of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainIndexer()}.getCardinality().
	 */
	public void setEnergiesDense(double[] energies);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#DENSE_WEIGHT} with
	 * provided weights replacing the existing contents of the table.
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

	/**
	 * Randomize the weights of the table depending on underlying representation.
	 * <p>
	 * If table {@link #hasDenseRepresentation()} this will randomly assign weights/energies
	 * to entries for all joint indexes, otherwise it will only assign values for all sparse
	 * indexes. Weights will be assigned uniformly from the range (0,1].
	 */
	public void randomizeWeights(Random rand);

	@Deprecated
	public void serializeToXML(String serializeName, String targetDirectory);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#DETERMINISTIC} with given set of
	 * outputs.
	 * <p>
	 * @param outputIndices any array mapping input indices, representing the joint value of all input
	 * values, to output indices, representing the joint value of all outputs. The length of the array
	 * must be equal to the value of {@link JointDomainIndexer#getInputCardinality()} on {@link #getDomainIndexer()}.
	 * @throws UnsupportedOperationException if not {@link #isDirected()}.
	 */
	public void setDeterministicOutputIndices(int[] outputIndices);

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
	public void setDirected(@Nullable BitSet outputSet);
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If the representation started out as deterministic ({@link #hasDeterministicRepresentation()})
	 * and the energy changed, it will be
	 * implicitly converted to {@link FactorTableRepresentation#DENSE_ENERGY} and if
	 * {@link FactorTableRepresentation#DETERMINISTIC_WITH_INDICES} it will be converted to
	 * {@link FactorTableRepresentation#ALL_ENERGY_WITH_INDICES}.
	 */
	@Override
	public void setEnergyForJointIndex(double energy, int jointIndex);
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If the representation started out as deterministic ({@link #hasDeterministicRepresentation()})
	 * and the energy changed, it will be converted to sparse energies (retaining sparse indices if they exist).
	 */
	@Override
	public void setEnergyForSparseIndex(double energy, int sparseIndex);
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If the representation started out as {@link FactorTableRepresentation#DETERMINISTIC}, it will be
	 * and the weight changed, it will be implicitly converted to {@link FactorTableRepresentation#DENSE_WEIGHT} and if
	 * {@link FactorTableRepresentation#DETERMINISTIC_WITH_INDICES} it will be converted to
	 * {@link FactorTableRepresentation#ALL_WEIGHT_WITH_INDICES}.
	 */
	@Override
	public void setWeightForJointIndex(double weight, int jointIndex);
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If the representation started out as deterministic ({@link #hasDeterministicRepresentation()})
	 * and the weight changed, it will be converted to sparse weights (retaining sparse indices if they exist).
	 */
	@Override
	public void setWeightForSparseIndex(double weight, int sparseIndex);

	/**
	 * Sets the underlying representation of the table to the specified value.
	 * <p>
	 * @throws DimpleException If setting representation to a deterministic representation, this method will throw an
	 * exception if the table cannot be represented as deterministic (see {@link #isDeterministicDirected()}) or
	 * table does not support joint indexing and a dense representation is requested
	 * (see {@link #supportsJointIndexing()}.
	 */
	public void setRepresentation(FactorTableRepresentation representation);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_ENERGY} with
	 * provided energies for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param energies specifies the energies of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 * @see #setEnergiesSparse(int[][], double[])
	 * @see #setWeightsSparse(int[], double[])
	 */
	public void setEnergiesSparse(int[] jointIndices, double[] energies);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_ENERGY} with
	 * provided weights for each set of indices.
	 * <p>
	 * @param indices are the combined element indices of the entries to put in the table.
	 * @param energies specifies the energies of the table in the same order as {@code indices}.
	 * @throws IllegalArgumentException if {@code indices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 * @see #setEnergiesSparse(int[], double[])
	 * @see #setWeightsSparse(int[][], double[])
	 * @since 0.05
	 */
	public void setEnergiesSparse(int[][] indices, double[] energies);

	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_WEIGHT} with
	 * provided weights for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code weights} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 * @see #setWeightsSparse(int[][], double[])
	 * @see #setEnergiesSparse(int[], double[])
	 */
	public void setWeightsSparse(int[] jointIndices, double[] weights);
	
	/**
	 * Sets representation to {@link FactorTableRepresentation#SPARSE_WEIGHT} with
	 * provided weights for each set of indices.
	 * <p>
	 * @param indices are the combined element indices of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code indices}.
	 * @throws IllegalArgumentException if {@code indices} and {@code weights} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 * @see #setWeightsSparse(int[], double[])
	 * @see #setEnergiesSparse(int[][], double[])
	 */
	public void setWeightsSparse(int[][] indices, double[] weights);
}
