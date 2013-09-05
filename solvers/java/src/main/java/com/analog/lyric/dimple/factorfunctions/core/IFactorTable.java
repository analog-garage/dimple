package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Random;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.JointDiscreteDomain;

// REFACTOR: replace with INewFactorTable and rename that when done. See comments below for each method.
public interface IFactorTable
{
	//==============
	// Old methods
	//
	
	// Used only by PFactorTable/MATLAB
	public void change(int [][] indices, double [] weights); // REFACTOR: => setWeightsSparse
	public void changeIndices(int [][] indices); // REFACTOR: eliminate
	public double get(int [] indices); // REFACTOR: => getWeightForIndices
	public void set(int [] indices, double value); // REFACTOR: => setWeightForIndices

	//
	// Used by PFactorTable and other
	//
	
	public void changeWeights(double [] values); // REFACTOR: => replaceWeightsSparse
	public DiscreteDomain[] getDomains(); // REFACTOR: => getDomainIndexer
	public int [][] getIndices(); // REFACTOR: => getIndicesSparseUnsafe
	public double[] getWeights(); // REFACTOR: => getWeightsSparseUnsafe
	
	public void normalize(int [] directedTo); // REFACTOR: => setConditionalAndNormalize

	//
	// Others
	//
	
	// Only used by GradientDescent.runStep
	public void changeWeight(int index, double weight); // REFACTOR: => setWeightForSparseIndex
	
	// Only used only by ParameterEstimator.saveFactorTables
	public IFactorTable copy(); // REFACTOR: => clone
	
	// Used in FactorTableBase & ParameterEstimator.run
	public void copy(IFactorTable that); // REFACTOR: keep (for now)
	
	/**
	 * Creates a new factor table based on this one, with {@code newDomains} added to the
	 * end of the tables domains. The existing table values will be duplicated for all
	 * combinations of the new domain values.
	 */
	// Only used by DiscreteFactor.replaceVariablesWithJoint
	public IFactorTable createTableWithNewVariables(DiscreteDomain[] newDomains); // REFACTOR: keep (for now)

	// Only used by TableFactorFunction.eval
	public double evalAsFactorFunction(Object ... arguments); // REFACTOR: => getWeightForArguments (Elements?)

	// Only used by TableFactorFunction.evalDeterministicFunction
	public void evalDeterministicFunction(Object ... arguments); // REFACTOR: => evalDeterministic
	
	// Only used in test
	public int[] getColumnCopy(int column); // REFACTOR: eliminate
	
	// multiple users
	public int getColumns(); // REFACTOR: => getDimensions
	
	// Only used by FactorTable.copy
	public int [] getDirectedFrom(); // REFACTOR: => getDomainIndexer().getInputDomainIndices()
	public int [] getDirectedTo(); // REFACTOR: => getDomainIndexer().getOutputDomainIndices()
	
	// Only used by FactorTableBase.getColumnCopy and test
	public int getEntry(int row, int column); // REFACTOR: => getDomainIndexer().jointIndexToElementIndex(...)
	
	// Multiple users
	public double[] getPotentials(); // REFACTOR: => getEnergiesSparseUnsafe
	
	// Only used by lp.STableFactor.computeValidAssignments and test
	public int[] getRow(int row); // REFACTOR: => sparseIndexToIndices
	
	// Multiple users
	public int getRows(); // REFACTOR: => sparseSize
	
	// Multiple users
	public int getWeightIndexFromTableIndices(int[] indices); // REFACTOR: => sparseIndexFromTableIndices
	
	// Only used by TableFactorFunction.isDeterministicDirected
	public boolean isDeterministicDirected(); // REFACTOR: keep
	
	public boolean isDirected(); // REFACTOR: keep
	
	/**
	 * Creates a new factor table that replaces two or more of its domains with
	 * a joint domain.
	 * <p>
	 * @param varIndices contains the indices of the domains to be joined. Must have at least two entries
	 * in the range [0,{@link #getColumns()}-1].
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
	
	public void normalize(); // REFACTOR: => keep
	
	// Is this just an optimization? Can directedTo != getDirectedTo()?
	public void normalize(int [] directedTo, int [] directedFrom); // REFACTOR: => setConditionalAndNormalize
	
	// Only used by ParameterEstimator.run
	public void randomizeWeights(Random rand); // REFACTOR: keep?
	
	// only used by test
	public void serializeToXML(String serializeName, String targetDirectory); // REFACTOR: eliminate
	
	// Only called from Factor.setDirectedTo
	public void setDirected(int[] directedTo, int[] directedFrom); // REFACTOR: => setDirected(BitSet)
	
	//
	// From INewFactorTable*
	//
	
	/**
	 * Returns the energy of factor table entry with given {@code indices}.
	 */
	public abstract double getEnergyForIndices(int ... indices);
	public abstract double getWeightForIndices(int ... indices);
	public abstract void normalizeConditional();
	
}
