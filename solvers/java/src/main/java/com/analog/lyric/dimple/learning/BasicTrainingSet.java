package com.analog.lyric.dimple.learning;

import java.util.Iterator;

public class BasicTrainingSet implements ITrainingSet
{
	/*-------
	 * State
	 */
	
	private final ITrainingSample _commonAssignments;
	private final Iterable<ITrainingSample> _samples;

	/*--------------
	 * Construction
	 */
	
	public BasicTrainingSet(ITrainingSample commonAssignments, Iterable<ITrainingSample> samples)
	{
		_commonAssignments = commonAssignments;
		_samples = samples;
	}
	
	public BasicTrainingSet(Iterable<ITrainingSample> samples)
	{
		this(null, samples);
	}

	/*----------------------
	 * ITrainingSet methods
	 */
	
	@Override
	public Iterator<ITrainingSample> iterator()
	{
		return _samples.iterator();
	}

	@Override
	public ITrainingSample getCommonAssignments()
	{
		return _commonAssignments;
	}
}
