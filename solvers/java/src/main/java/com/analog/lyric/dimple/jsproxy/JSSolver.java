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

package com.analog.lyric.dimple.jsproxy;

import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontreemap.JunctionTreeMAPSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public enum JSSolver
{
	Gibbs(new GibbsSolver()),
	JunctionTree(new JunctionTreeSolver()),
	JunctionTreeMAP(new JunctionTreeMAPSolver()),
	MinSum(new MinSumSolver()),
	ParticleBP(new ParticleBPSolver()),
	SumProduct(new SumProductSolver());
	
	private final IFactorGraphFactory<?> _factory;
	
	private JSSolver(IFactorGraphFactory<?> factory)
	{
		_factory = factory;
	}
	
	IFactorGraphFactory<?> getFactory()
	{
		return _factory;
	}
}
