## Factors and Related Classes

### Factor

The Factor class can represent either a single factor or a
multidimensional array of factors. The Factor class is never created
directly, but is the result of using the addFactor or
addFactorVectorized (or related) methods on a FactorGraph.

Read-write. When read, retrieves the current name of the factor or array
of factors. When set, modifies the name of the factor to the
corresponding value. The value set must be a string.

    factor.Name = 'string';

When setting the Name, only one factor in an array may be set at a time.
To set the names of an entire array of factors to distinct values, the
setNames method may be used (see
section [\[sec:Factor.setNames\]](#sec:Factor.setNames){reference-type="ref"
reference="sec:Factor.setNames"}).

    factor.setName("string");

Read-write. All variables and factors in a Factor Graph must have unique
names. However, sometimes its desirable to have variables or factors
share similar strings when being plotted or printed. Users can set the
Label property to set the name for display. If the Label is not set, the
Name will be used for display. Once the label is set, the label will be
used for display.

    factor.Label = 'string';

    factor.setLabel("string");

[]{#sec:Factor.DirectedTo label="sec:Factor.DirectedTo"}

Read-write. The DirectedTo property indicates a set of variables to
which the factor is directed. The value may be either a single variable
or a cell array of variables. The DirectedTo property is used by some
solvers, and in some cases is required for proper operation of certain
features. Such cases are identified elsewhere in this manual.

Read-write. The DirectedTo property indicates a set of variables to
which the factor is directed. The value may be either a single variable,
a comma separated list of variables, a VariableList, or an array of
integers specifying the edge indexes to which the factor is directed.
The DirectedTo property is used by some solvers, and in some cases is
required for proper operation of certain features. Such cases are
identified elsewhere in this manual.

For example, if a factor F corresponds to a function $F(a, b, c, d)$,
where $a$, $b$, $c$, and $d$ are variables, then the factor is directed
toward $c$ and $d$ if $\sum_{c, d} F(a, b, c, d)$ is constant for all
values of $a$ and $b$. In this case, we may set:

    F.DirectedTo = {c, d};

    F.setDirectedTo(c,d);

In some cases, the set of DirectedTo variables can be automatically
determined when a factor is created. In this case it need not be set
manually by the user. This includes many built-in factors supported by
Dimple. If this property is set by the user, then in the case of factors
connected only to discrete variables, Dimple will check that the factor
is in fact directed in the specified direction.

If the set of variables the factor are directed toward are part of a
variable array, then these may be specified together in a single cell
array. For example, if varArray is an array of variables, and a factor F
is directed toward all of the variables in varArray, then we can set:

    F.DirectedTo = varArray;

In the case of a vector of factors, we can identify the variables to
which each factor is directed in a vectorized way. For example:

    s = Discrete(domain,N);
    fg.addFactorVectorized(factorFunction, s(1:(end-1)), s(2:end)).DirectedTo = s(2:end);

This example also shows that the DirectedTo property can be set directly
on the result of the factor creation without assigning the factor to a
named variable.

As a more complicated vectorized example, the following creates 12
factors, each of which contains 10 variables (5 from a and 5 from b).
The first 2 of the 5 from a and the first from b are what the factor is
directed to.

    a = Bit(3,4,5);
    b = Bit(3,4,5);
    fg.addFactorVectorized(factorFunction, {a, [1 2]}, {b, [1 2]}).DirectedTo = {a(:,:,1:2), b(:,:,1)};

[]{#sec:Factor.Score label="sec:Factor.Score"}

Read-only. When read, computes and returns the score (energy) of the
factor given a specified value for each of the neighboring variables to
this factor. The score represents the energy of the factor given the
specified variable configuration. The score value is relative, and may
be arbitrarily normalized by an additive constant.

The value of each variable used when computing the Score is the Guess
value for that variable (see
section [\[sec:Variable.Guess\]](#sec:Variable.Guess){reference-type="ref"
reference="sec:Variable.Guess"}). If no Guess had yet been specified for
a given variable, the value with the most likely belief (which
corresponds to the Value property of the variable) is used[^1].

The variable energy is normalized by the maximum input probability.

[]{#sec:Factor.InternalEnergy label="sec:Factor.InternalEnergy"}

Read-only. (Only applies to the Sum Product Solver). When read returns:

$$InternalEnergy(a) = \sum_{\vec{x} \in \vec{X}}B_a(\vec{x})*(-log(Weight(\vec{x})))$$

Where a is an instance of a Factor, X is the set of variables connected
to a, Weight is the FactorTable entry for the specified set of variable
values, and $B_a$ is the belief of that factor node.

    double ie = f.getInternalEnergy();

[]{#sec:Factor.BetheEntropy label="sec:Factor.BetheEntropy"}

Read-only. (Only applies to the Sum Product Solver). When read returns:

$$BetheEntropy(a) = - \sum_{\vec{x} \in domain(\vec{X})}B_a(\vec{x})*log(B_a(\vec{x}))$$

Where a is an instance of a Factor, X is the set of variables connected
to a, and $B_a$ is the belief of that factor node.

    double be = f.getBetheEntropy();

[]{#sec:Factor.Belief label="sec:Factor.Belief"}

Read-only. (Only applies to the Sum Product Solver). To support the
Bethe Free Energy property, Dimple provides getBelief associated with a
Factor.

$$Belief_a(\vec{x}) = Weight(\vec{x})\prod_{i=0}^N \mu_{X_i \rightarrow a}(x_i)$$

Where $\vec{x} \in domain(\vec{X})$ and $\vec{X}$ is the set of
variables connected to the factor a.

    b = f.Belief;

    double b = f.getBelief();

Read-only. Retrieves a cell array an array containing a list of Ports
connecting the factor to its neighboring variables.

[]{#sec:Factor.setNames label="sec:Factor.setNames"}

For an array of factors, the setNames method sets the name of each
factor in the array to a distinct value derived from the supplied string
argument. When called with:

    factorArray.setName('baseName');

the resulting factor names are of the form: `baseNamevv0`,
`baseNamevv1`, `baseNamevv2`, etc., where each factor's name is the
concatenation of the base name with the suffix `vv` followed by a unique
number for each factor in the array.

    factorArray.invokeSolverSpecificMethod('methodName', arguments);

For an array of factors, the invokeSolverSpecificMethod calls the
specified solver-specific method on each of the factors in the array.
Here, 'methodName' is a text string with the name of the solver-specific
method, and arguments is an optional comma-separated list of arguments
to that method. This method does not result in any return values. For
solver specific methods that return results, use the
invokeSolverSpecificMethodWithReturnValue method instead.

    returnArray = factorArray.invokeSolverSpecificMethodWithReturnValue('methodName', arguments);

For an array of factors, the invokeSolverSpecificMethodWithReturnValue
calls the specified solver-specific method on each of the factors in the
array, returning a return value for each variable. Here, 'methodName' is
a text string with the name of the solver-specific method, and arguments
is an optional comma-separated list of arguments to that method. The
returnArray is a cell-array of the return values of the method, with
dimensions equal to the dimensions of the factor array.

### DiscreteFactor

When all variables connected to a Factor are discrete, a DiscreteFactor
is created.

[]{#sec:Factor.Belief label="sec:Factor.Belief"}

Read-only. The belief of a factor is the joint belief over all joint
states of the variables connected to that factor. There are two
properties that represent the belief in different ways: Belief and
FullBelief. Reading the Belief property after the solver has been
run[^2] returns the belief in a compact one-dimensional vector that
includes only values that correspond to non-zero entries in the factor
table. This form is used because in some situation, the full
representation over all possible variable values (as returned by the
FullBelief property) would result in a data structure too large to be
practical.

    fb = myFactor.Belief;

    double [] fb = myFactor.getBelief();

The result is a vector of belief values, where the order of the vector
corresponds to the order of the factor table entries. The order of
factor table entries can be determined from the factor using:

    ind = f.FactorTable.Indices

    int [][] indices = f.getFactorTable().getIndices()

This returns a two-dimensional array, where each row corresponds to one
entry in the factor table, and where each column-entry in a row
indicates the index into the domain of the corresponding variable (where
the order of the variable is the order used when the factor was
created).

[]{#sec:Factor.FullBelief label="sec:Factor.FullBelief"}

Read-only. Reading the FullBelief property after the solver has been
run[^3] returns the belief in a multi-dimensional array, where each
dimension of the multi-dimensional array ranges over the domain of the
corresponding variable (the order of the dimensions corresponds to the
variable order used when the factor was created).

    fb = myFactor.FullBelief;

    double [] fb = myFactor.getFullBelief();

### FactorFunction {#sec:FactorFunction}

The FactorFunction class is used to specify a Dimple built-in factor
function in a way that can be reused for creating multiple factors, and
that allows specification of constructor arguments.

    FactorFunction(factorFunctionName, [constructorArguments])

-   factorFunctionName is a string that indicates the name of the
    built-in factor function.

-   constructorArguments is a variable-length comma-separated list of
    constructor arguments, whose interpretation is specific to the
    particular built-in factor function. If no arguments are needed,
    this list would be empty.

There are no available properties or methods in this class.

The FactorFunction class is an abstract class that is used as the base
class for all FactorFunctions passed to the addFactor call. To create
new FactorFunctions, users must override the FactorFunction class.

-   FactorFunction()

-   FactorFunction(String name);

This method must be overridden in a derived class. The method takes an
array of Values and returns the energy (negative log of the weight
value) associated with the corresponding values.

    double evalEnergy(Value[] values);

This method takes no arguments and returns a boolean indicating whether
the factor function is directed. If directed, then there are a set of
directed outputs for which the marginal distribution for all possible
input values is a constant. This method may be overridden in a derived
class. If not overridden, it is assumed false.

If a factor function is directed, this method indicates which edge
indices are the directed outputs (numbering from zero), returning an
array of integers. There are two forms of this method, which may be used
depending on whether the set of directed outputs depends on the number
of edges in the factor that uses this factor function (many factor
functions support a variable number of edges). If `isDirected` is
overridden and can return `true`, then this method must also be
overridden. If the result depends on the number of edges connecting to
the factor, then the second form must be overridden, otherwise the first
form may be overridden instead.

    int[]  getDirectedToIndices();

    int[] getDirectedToIndices(int numEdges);

This method takes no arguments and returns a boolean indicating whether
a factor function is both directed and deterministic. If deterministic
and directed, then it is in the form of a deterministic function such
that for all possible settings of the input values there is exactly one
output value the results in a non-zero weight (or, equivalently, a
non-infinite energy)[^4]. This method may be overridden in a derived
class. If not overridden, it is assumed false.

If a factor function is directed and deterministic, this method
evaluates the values considered the inputs of the deterministic function
and returns the resulting values for the corresponding outputs. Note
that these are not the weight or energy values, but the actual values of
the associated variables that are considered outputs of the
deterministic function. The resulting values are returned in-place to
the values passed in the argument list. If `isDeterministicDirected` is
overridden and can return `true`, then this method must also be
overridden.

    evalDeterministic(Value[] arguments);

The method takes an array of Values and returns the weight associated
with the corresponding values. This method need not be overridden in a
derived class as the default implementation uses `evalEnergy` to compute
the result.

    double eval(Value[] values);

### FactorTable {#sec:FactorTable}

The FactorTable class is used to explicitly specify a factor table in
lieu of Dimple creating one automatically from a factor function. This
is sometimes useful in cases where the factor table is highly
structured, but automatic creation would be time consuming due to a
large number of possible states of the connected variables.

    FactorTable([ ( indexList, weightList ) | weightMatrix ], domains)

A FactorTable is constructed by specifying the table values in one of
two forms, or by creating an all-zeros FactorTable to be filled in later
using the set method. The first form, specifying an indexList and
weightList, is useful for sparse factor tables in which many entires are
zero and need not be included in the table. The second form, specifying
a weightMatrix, is useful for dense factor tables in which most or all
of the entries are non-zero.

-   indexList is an array where each row represents a set of zero-based
    indices into the list of domain elements for each successive domain
    in the given set of domains.

-   weightList is a one-dimensional array of real-valued entries in the
    factor table, where each entry corresponds to the indices given by
    the corresponding row of the indexList.

-   weightMatrix is an N dimensional array of real-valued entries in the
    factor table. The number of dimensions, N, must correspond to the
    number of discrete domain elements given in the subsequent
    arguments, and the number of elements in each dimension must equal
    the number of elements in the corresponding domain element.

-   domains is a comma-separated list of one or more discrete variable
    domains. Each domain either in the form of a DiscreteDomain object
    or a cell-array of the elements of the domain. From an existing
    variable, the domain can be obtained using the Domain property of
    that variable.

An example using the first method is:

    a = Bit;
    b = Bit;
    c = Bit;
    ft = FactorTable([0 0 0; 0 1 1; 1 0 1; 1 1 0], [1 .9 .5 .3], a.Domain, b.Domain, c.Domain);

An example using the second method is:

    a = Discrete({'red','green','blue'});
    b = Bit;
    ft = FactorTable([.5 .4; .3 .1; 0 .4], a.Domain, b.Domain);

-   FactorTable.create(DiscreteDomain \... domains) - Creates a full
    table with with the specified domains.

-   FactorTable.create(int \[\]\[\] indices, double \[\]
    weights,DiscreteDomain \... domains) - Creates a table with the
    specified indices and weights with the specified domains

-   FactorTable.create(int \[\]\[\] indices, double \[\] weights,
    Discrete \... variables) - Same as the previous method but extracts
    the domains from the variables.

-   FactorTable.create(Object table, DiscreteDomain \[\] domains) -
    Assumes table is a tensor (number of dimensions equal to number of
    domains).

A FactorTable is constructed by specifying the table values in one of
two forms, or by creating an all-zeros FactorTable to be filled in later
using the set method. The first form, specifying an indexList and
weightList, is useful for sparse factor tables in which many entires are
zero and need not be included in the table. The second form, specifying
a weightMatrix, is useful for dense factor tables in which most or all
of the entries are non-zero.

-   indices is an array where each row represents a set of zero-based
    indices into the list of domain elements for each successive domain
    in the given set of domains.

-   weights is a one-dimensional array of real-valued entries in the
    factor table, where each entry corresponds to the indices given by
    the corresponding row of the indexList.

-   table is an N dimensional array of real-valued entries in the factor
    table. The number of dimensions, N, must correspond to the number of
    discrete domain elements given in the subsequent arguments, and the
    number of elements in each dimension must equal the number of
    elements in the corresponding domain element.

Read-write. When read, returns an array of indices of the factor table
corresponding to entries in the factor table. Each row represents a set
of zero-based indices into the list of domain elements for each
successive domain in the given set of domains.

When written, replaces the previous array of indices with a new array.
When writing using this property, the number of rows in the table must
not change since this must equal the number of entires in the Weights.
To change both Indices and Weights simultaneously, use the change
method.

Read-write. When read, returns a one-dimensional array of real-valued
entries in the factor table, where each entry corresponds to the indices
given by the corresponding row of the indexList.

When written, replaces the previous array of weights. When writing using
this property, the number of entries must not change since this must
equal the number of rows in the Indices. To change both Indices and
Weights simultaneously, use the change method.

Read-only. Returns a cell-array an array of DiscreteDomain objects, each
of which represents the corresponding domain specified in the
constructor, in the same order as specified in the constructor.

[]{#sec:FactorTable.set label="sec:FactorTable.set"}

This method allows setting individual entries in the factor table. For
each entry to be set, the domain values (not indices) are specified
followed by the weight value. To set a single entry, the domain values
are specified in a comma-separated list, followed by the weight value.
To set multiple entries in a single call, a cell array of such
comma-separated lists are specified.

An example of setting a single entry:

    ft.set('red', 1, 0.45);

An example of setting multiple entries:

    ft.set({'red', 1, 0.45}, {'blue', 0, 0.75});

This method allows setting individual entries in the factor table. For
each entry to be set, the indices are specified followed by the weight
value.

    ft.set(new int [] {0,1,1},1.0);

This method retrieves the weight associated with a particular entry in
the factor table. The entry is specified by a comma-separated list of
domain values (not indices). For example:

    w = ft.get('red', 1);

This method retrieves the weight associated with a particular entry in
the factor table. The entry is specified by a comma-separated list of
indices. For example:

    double w = ft.get(new int [] {0,1,1});

This method allows simultaneously replacing both the array of Indices
and Weights in the factor table.

    ft.change(indexList, weightList);

The arguments indexList and weightList are exactly as described in the
FactorTable constructor.

[^1]: For some solvers, beliefs are not supported for all variable
    types; in such cases there is no default value, so a Guess must be
    specified.

[^2]: In the current version of Dimple, the Belief property is only
    supported for factors connected exclusively to discrete variables,
    and is supported only by the SumProduct solver. These restrictions
    may be removed in a future version.

[^3]: In the current version of Dimple, the Belief property is only
    supported for factors connected exclusively to discrete variables,
    and is supported only by the SumProduct solver. These restrictions
    may be removed in a future version.

[^4]: The indication that a factor function is deterministic directed is
    used by the Gibbs solver, and is necessary for such factor functions
    to work when using the Gibbs solver.
