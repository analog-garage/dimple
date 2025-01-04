## Other Top Level Functions

### setSolver

    setSolver('SolverName');

This function changes the default solver to the solver designated by the
argument, which is a string indicating the name of the solver (see
section [\[sec:FactorGraph.Solver\]](#sec:FactorGraph.Solver){reference-type="ref"
reference="sec:FactorGraph.Solver"} for the list of valid solver names,
and section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"} for a description of each solver). The
solver name is case insensitive.

    Model.getInstance().setDefaultGraphFactory(graphFactory);

This function changes the default solver to the solver designated by the
argument. (see
section [\[sec:FactorGraph.Solver\]](#sec:FactorGraph.Solver){reference-type="ref"
reference="sec:FactorGraph.Solver"} for the list of valid solver names,
and section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"} for a description of each solver).

### dimpleVersion

Dimple provides a method to return a string describing the current
Dimple version.

    version = dimpleVersion();

    version = Model.getVersion();

This will produce something of the form:

    <Release Number> <Git Branch Name> YYY-MM-DD HH:MM:SS <Timezone Offset>

For example:

    0.04 master 2013-10-11 14:00:05 -0400

The date in the version string represents the last git commit date
associated with the compiled code.

### dimpleOptions

Returns a complete list option keys, corresponding to all of available
Dimple options. See
section [\[sec:Options\]](#sec:Options){reference-type="ref"
reference="sec:Options"} for a description of the Dimple options
mechanism.

    dimpleOptions()
