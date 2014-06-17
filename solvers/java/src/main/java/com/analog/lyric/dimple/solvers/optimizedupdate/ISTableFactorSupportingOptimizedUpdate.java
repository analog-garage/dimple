package com.analog.lyric.dimple.solvers.optimizedupdate;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.util.misc.Internal;

/**
 * Implemented by STableFactors that support optimized update, so that the common optimized update
 * code can access the factor's properties that it requires.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public interface ISTableFactorSupportingOptimizedUpdate
{
	double[][] getInPortMsgs();

	double[][] getOutPortMsgs();

	double getDamping(int _outPortNum);

	boolean isDampingInUse();

	double[] getSavedOutMsgArray(int _outPortNum);

	Factor getFactor();
}
