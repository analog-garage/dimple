package com.analog.lyric.dimple.learning;

import java.util.List;
import java.util.RandomAccess;

import com.analog.lyric.dimple.model.VariableBase;

/**
 * A training set that provides an ordered list of all variables used in the samples.
 * This allows the training data to be represented efficiently as an array or sequence
 * in the same order as the variable list.
 */
public interface IVariableListTrainingSet extends ITrainingSet
{
	/**
	 * @return ordered fast random-access list of variables that are to be included in
	 * training samples in this set. The returned list is expected to implement the
	 * {@link RandomAccess} interface.
	 */
	public List<VariableBase> getVariableList();
}
