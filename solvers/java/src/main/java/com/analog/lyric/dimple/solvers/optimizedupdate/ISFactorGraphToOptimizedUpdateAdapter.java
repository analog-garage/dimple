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

package com.analog.lyric.dimple.solvers.optimizedupdate;

import java.util.Map;

import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.util.misc.Internal;

/**
 * SFactorGraph supplies an implementation to provide solver-specific functions to the optimized
 * update code.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public interface ISFactorGraphToOptimizedUpdateAdapter
{
	IMarginalizationStep createDenseMarginalizationStep(TableWrapper tableWrapper,
		int inPortNum,
		int dimension,
		IFactorTable g_factorTable);

	IMarginalizationStepEstimator createDenseMarginalizationStepEstimator(CostEstimationTableWrapper tableWrapper,
		int inPortNum,
		int dimension,
		CostEstimationTableWrapper g);

	IUpdateStep createDenseOutputStep(int outPortNum, TableWrapper tableWrapper);

	IUpdateStepEstimator createDenseOutputStepEstimator(CostEstimationTableWrapper tableWrapper);

	IMarginalizationStep createSparseMarginalizationStep(TableWrapper tableWrapper,
		int inPortNum,
		int dimension,
		IFactorTable g_factorTable,
		Tuple2<int[][], int[]> g_and_msg_indices);

	IMarginalizationStepEstimator createSparseMarginalizationStepEstimator(CostEstimationTableWrapper tableWrapper,
		int inPortNum,
		int dimension,
		CostEstimationTableWrapper g);

	IUpdateStep createSparseOutputStep(int outPortNum, TableWrapper tableWrapper);

	IUpdateStepEstimator createSparseOutputStepEstimator(CostEstimationTableWrapper tableWrapper);

	Costs estimateCostOfNormalUpdate(IFactorTable factorTable);

	Costs estimateCostOfOptimizedUpdate(IFactorTable factorTable, double sparseThreshold);

	double[] getDenseValues(IFactorTable factorTable);

	double[] getSparseValues(IFactorTable factorTable);

	int getWorkers(FactorGraph factorGraph);

	void putFactorTableUpdateSettings(Map<IFactorTable, FactorTableUpdateSettings> optionsValueByFactorTable);

	IOptionKey<UpdateApproach> getUpdateApproachOptionKey();

	IOptionKey<Double> getOptimizedUpdateSparseThresholdKey();

	IOptionKey<Double> getAutomaticExecutionTimeScalingFactorKey();

	IOptionKey<Double> getAutomaticMemoryAllocationScalingFactorKey();
}
