package com.analog.lyric.dimple.FactorFunctions.core;

import java.io.Serializable;
import java.util.BitSet;

import com.analog.lyric.dimple.model.DiscreteDomain;

public interface INewFactorTableBase extends Cloneable, Serializable
{
	public abstract INewFactorTableBase clone();

	public abstract int domainCount();

	public abstract void evalDeterministic(Object[] arguments);
	public abstract double evalEnergy(Object ... arguments);
	public abstract double evalWeight(Object ... arguments);

	public BitSet getInputSet();
	
	public abstract DiscreteDomain getDomain(int i);
	public abstract int getDomainSize(int i);

	public abstract double getEnergy(int i);
	public abstract double getEnergy(int ... indices);
	
	public abstract double getWeight(int i);
	public abstract double getWeight(int... indices);

	public abstract boolean isDeterministicDirected();
	public abstract boolean isDirected();
	public abstract boolean isNormalized();

	public abstract int jointIndexFromArguments(Object ... arguments);
	public abstract int jointIndexFromIndices(int ... indices);
	public abstract void jointIndexToArguments(int joint, Object[] arguments);
	public abstract void jointIndexToIndices(int joint, int[] indices);
	
	public abstract int jointSize();
	
	public abstract int locationFromArguments(Object ... arguments);
	public abstract int locationFromIndices(int... indices);
	public abstract int locationFromJointIndex(int joint);
	public abstract int locationToJointIndex(int location);
	public abstract void locationToArguments(int location, Object[] arguments);
	public abstract void locationToIndices(int location, int[] indices);

	public abstract void normalize();

	public abstract int size();

}