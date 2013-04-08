package com.analog.lyric.dimple.solvers.core.proposalKernels;

public class Proposal
{
	public Object value = 0;
	public double hastingsTerm = 0;		// Negative log p(x' -> x) / p(x -> x')

	// Construct with no Hastings term
	public Proposal(Object value) {this.value = value;}
	
	// General constructor
	public Proposal(Object value, double hastingsTerm) {this.value = value; this.hastingsTerm = hastingsTerm;}
}
