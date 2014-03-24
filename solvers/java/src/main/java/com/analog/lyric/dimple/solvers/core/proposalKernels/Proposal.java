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

package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;

public class Proposal
{
	public final Value value;
	public final double hastingsTerm;		// Negative log p(x' -> x) / p(x -> x')

	// Construct with no Hastings term
	public Proposal(Value value) {this.value = value; this.hastingsTerm = 0;}
	
	// General constructor
	public Proposal(Value value, double hastingsTerm) {this.value = value; this.hastingsTerm = hastingsTerm;}
	
	// Construct with no Hastings term; for real valued proposal
	public Proposal(double value) {this.value = RealValue.create(value); this.hastingsTerm = 0;}
	
	// General constructor; for real valued proposal
	public Proposal(double value, double hastingsTerm) {this.value = RealValue.create(value); this.hastingsTerm = hastingsTerm;}
}
