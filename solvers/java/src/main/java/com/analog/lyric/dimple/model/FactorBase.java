package com.analog.lyric.dimple.model;


public abstract class FactorBase extends Node
{
	// FIXME: constructors should probably have package access to enforce intention that all
	// subclasses are either Factors or FactorGraphs.
	
	public FactorBase(int id)
	{
		super(id);
	}
	
	public FactorBase()
	{
		super();
	}

	/** {@inheritDoc} If null {@link #asFactorGraph()} will be non-null. */
	@Override
	public Factor asFactor() { return null; }
	
	/** {@inheritDoc} If null {@link #asFactor()} will be non-null. */
	@Override
	public FactorGraph asFactorGraph() { return null; }

}
