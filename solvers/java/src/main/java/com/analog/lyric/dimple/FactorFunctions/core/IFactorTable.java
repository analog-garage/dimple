package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Random;

import com.analog.lyric.dimple.model.DiscreteDomain;

public interface IFactorTable
{
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
	
	// Is this just an optimization? Can directedTo != getDirectedTo()?
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
	
	// Only used by FactorTableBase.getColumnBase and test
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
	
	// Only used by DiscreteFactor.replaceVariablesWithJoint
	public IFactorTable joinVariablesAndCreateNewTable(int [] varIndices,
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
}
