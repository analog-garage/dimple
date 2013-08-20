package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Random;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.JointDiscreteDomain;

public interface IFactorTable
{
	//==============
	// Old methods
	//
	
	// Used only by PFactorTable/MATLAB
	public void change(int [][] indices, double [] weights);
	public void changeIndices(int [][] indices);
	public double get(int [] indices);
	public void set(int [] indices, double value);

	//
	// Used by PFactorTable and other
	//
	
	public void changeWeights(double [] values);
	public DiscreteDomain[] getDomains();
	public int [][] getIndices();
	public double[] getWeights();
	
	public void normalize(int [] directedTo);

	//
	// Others
	//
	
	// Only used by GradientDescent.runStep
	public void changeWeight(int index, double weight);
	
	// Only used only by ParameterEstimator.saveFactorTables
	public IFactorTable copy();
	
	// Used in FactorTableBase & ParameterEstimator.run
	public void copy(IFactorTable that);
	
	/**
	 * Creates a new factor table based on this one, with {@code newDomains} added to the
	 * end of the tables domains. The existing table values will be duplicated for all
	 * combinations of the new domain values.
	 */
	// Only used by DiscreteFactor.replaceVariablesWithJoint
	public IFactorTable createTableWithNewVariables(DiscreteDomain[] newDomains);

	// Only used by TableFactorFunction.eval
	public double evalAsFactorFunction(Object ... arguments);

	// Only used by TableFactorFunction.evalDeterministicFunction
	public void evalDeterministicFunction(Object ... arguments);
	
	// Only used in test
	public int[] getColumnCopy(int column);
	
	// multiple users
	public int getColumns();
	
	// Only used by FactorTable.copy
	public int [] getDirectedFrom();
	public int [] getDirectedTo();
	
	// Only used by FactorTableBase.getColumnCopy and test
	public int getEntry(int row, int column);
	
	// Multiple users
	public double[] getPotentials();
	
	// Only used by lp.STableFactor.computeValidAssignments and test
	public int[] getRow(int row);
	
	// Multiple users
	public int getRows();
	
	// Multiple users
	public int getWeightIndexFromTableIndices(int[] indices);
	
	// Only used by TableFactorFunction.isDeterministicDirected
	public boolean isDeterministicDirected();
	
	public boolean isDirected();
	
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
		DiscreteDomain jointDomain);
	
	public void normalize();
	
	// Is this just an optimization? Can directedTo != getDirectedTo()?
	public void normalize(int [] directedTo, int [] directedFrom);
	
	// Only used by ParameterEstimator.run
	public void randomizeWeights(Random rand);
	
	// only used by test
	public void serializeToXML(String serializeName, String targetDirectory);
	
	// Only called from Factor.setDirectedTo
	public void setDirected(int[] directedTo, int[] directedFrom);
	
	//
	// New
	//
	
	/**
	 * Returns the energy of factor table entry with given {@code indices}.
	 */
	public abstract double getEnergyForIndices(int ... indices);
}
