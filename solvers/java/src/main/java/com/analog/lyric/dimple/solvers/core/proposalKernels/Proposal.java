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
