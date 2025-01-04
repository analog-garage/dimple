# Solvers <a name="SolversAPI"></a>

## Solver-Specific Options

Each solver supports a number of options specific to that solver. These
are described in the following sections. Solver-specific options may be
used to configure how overall inference works for that solver or may be
used to configure the behavior of individual factors or variables for
that solver. Solver-specific options are typically set on the applicable
model object (factor graph, factor, or variable) and the values will be
observed and used to configure the corresponding solver objects:

=== "Python"

    &lt;*model-object*>.set_option(&lt;*OptionClass&gt;.&lt;option-name&gt;, &lt;*value&gt;)

=== "Java"

    &lt;*model-object*>.setOption(&lt;*OptionClass&gt;.&lt;option-name&gt;, &lt;*value&gt;)

=== "MATLAB"

    &lt;*model-object*>.setOption(&lt;*OptionClass&gt;.&lt;option-name&gt;, &lt;*value&gt;)

Solver-specific options may be set at any time, even before the solver
for a graph has been specified. Options that are not applicable to the
object on which it is set or that are not applicable to the active
solver will simply be ignored. For more details on the options mechanism
see [\[sec:Options\]](#sec:Options){reference-type="ref+label"
reference="sec:Options"} of this document.

## Solver-Specific Methods

Each solver also may support solver-specific methods, which are
described in the following sections. As with options, solver-specific
methods may be available for various objects: a factor-graph, variable,
or factor. In each case, to call a solver-specific method, the method is
applied to the solver object, returned by the Solver property. For
example:

=== "Java"

    ```java
    ((SolverSpecificGraph)fg.getSolver()).solverSpecificMethod(arguments);

    ((SolverSpecificVar)variable.getSolver()).solverSpecificMethod(arguments);

    ((SolverSpecificFactor)factor.getSolver()).solverSpecificMethod(arguments);
    ```

    The downcast in the first cast can be avoided if you save the return
    value from and call the methods through that. For example:

    ```java
    JunctionTreeSolverGraph sgraph =
        fg.setSolverFactory(new JunctionTreeSolver());
    sgraph.useConditioning(true);
    ```

    The downcasts are also not required for common methods available to all
    solvers described in the next section.

=== "MATLAB"

    ```matlab
    factorGraph.Solver.solverSpecificMethod(arguments);

    variable.Solver.solverSpecificMethod(arguments);

    factor.Solver.solverSpecificMethod(arguments);
    ```

Some solver-specific methods return results, while others do not. Some
solver-specific methods require arguments, while others do not. If no
arguments are needed, the parentheses are optional.

=== "MATLAB"

    In some cases it is convenient to call solver-specific methods on each
    element in an array of objects. Utility methods are provided for this
    purpose. Specifically, to call a solver-specific method that has no
    return value:

    ```matlab
    objectArray.invokeSolverMethod('methodName', arguments);
    ```

    In this case, 'methodName' is a text string with the name of the
    solver-specific method, and arguments is an optional comma-separated
    list of arguments to that method.

    To call a solver specific method that has a return value:

    ```matlab
    returnArray = objectArray.invokeSolverSpecificMethodWithReturnValue('methodName', arguments);
    ```

    In this case, returnArray is a cell-array of the return values of the
    method, with dimensions equal to the dimensions of the object array.

## Common Options

A few options are applicable to multiple solvers and are therefore
described in this subsection. These are available on the `SolverOptions`
and `DimpleOptions` classes.

### SolverOptions.enableMultithreading

Controls whether to use multithreading for this
solver. Multithreading is currently only supported by the MinSum and
SumProduct solvers but will eventually be implemented in others. This
value will be ignored if not applicable.

### SolverOptions.maxAutomaticFactorTableSize

This controls whether factor tables will be automatically computed at
initialization time for discrete factors based on the expected number of
entries in the table. Factor table creation can be expensive both in the
memory it takes to store large tables and in the time that is needed to
evaluate the factor function for every possible combination of
arguments. This option provides a means to set a threshold for creation.
The expected size of the table is computed differently based on whether
the factor is directed and the function is deterministic:

-   If the factor is not deterministic directed, then the expected size
    of the factor table is the sizes of all of the variable domains
    multiplied together.

-   If the factor is deterministic and directed the expected size, then
    the expected size is the sizes of just the input variable domains
    multipled together.

This will not prevent factor tables from being generated manually. Only
applicable to discrete factors, and currently only useful when using the
Gibbs solver.

### DimpleOptions.randomSeed

When set, this option specifies a random seed
that may be used by solvers that use a random number generator. The seed
will only be used if explicitly set; the default value is not used. This
can be used to ensure repeatable behavior during testing or profiling
but should not be used for normal operation.

## Common Methods

There are also some methods that are common to all solvers. These are:

### getMultithreadingManager()

Dimple users can retrieve a MultithreadingManager on which to perform
additional actions.

=== "Java"

    ```java
    fg.getSolver().getMultithreadingManager()
    ```

=== "MATLAB"

    ```matlab
    fg.Solver.getMultithreadingManager()
    ```

Users can configure both the multithreading mode and the number of
workers using the `MultithreadingManager`.

### Multithreading Modes

Dimple provides various multithreading algorithms that have different
speed advantages depending on the size of the user's graph and
FactorTables. In the future Dimple should be modified to automatically
detect the best threading algorithm. Currently, however, it defaults to
the "Phase" multithreading mode and requires the user manually set the
mode to change this. For a given graph, users can try both modes and see
which is faster.

The currently supported multithreading modes are:

-   Phase - Divides the schedule into "phases" where each phase
    contains schedule entries that are entirely independent of one
    another. These phases are then easy to parallelize.

-   SingleQueue - Uses a single queue and a dependency graph to pull off
    work for each thread on the fly.

The following methods can be used for getting and setting modes:

=== "Java"

    - `fg.Solver.getMultithreadingManager().getModes()` - Returns a Java
        array of enums specifying the valid modes.

    - `fg.Solver.getMultithreadingManager().setMode(ModeName)` - Allows
        users to set the mode by string. Currently "Phase" or
        "SingleQueue" will work.

    - `fg.Solver.getMultithreadingManager().setMode(enum)` - Allows users to
        set the mode by the enums returned by the getModes method.

=== "MATLAB"

    - `fg.getSolver().getMultithreadingManager().getModes()` - Returns an
        array of enums specifying the valid modes.

    - `fg.getSolver().getMultithreadingManager().setMode(ModeName)` - Allows
        users to set the mode by string. Currently "Phase" or
        "SingleQueue" will work.

    - `fg.getSolver().getMultithreadingManager().setMode(enum)` - Allows
        users to set the mode by the enums returned by the getModes method
        or with MultithreadingMode.&lt;PhaseName&gt;.

### Setting Number of Threads and Workers

Dimple provides a ThreadingPool as a singleton for multithreading. It
sets the number of threads in this pool to the number of virtual cores
in the user's machine by default. Users can override this default value.
In addition, Dimple allows users to specify the number of "workers"
for a given FactorGraph. This "NumWorkers" is also set to the number
of virtual cores on the user's machine by default. Whereas NumThreads
specifies how many threads are in the threadPool, NumWorkers specifies
how work is divided up across the graph. These workers are run by the
thread pool. Best performance is achieved when NumWorkers and NumThreads
are the same. However, NumThreads is global and shared by all graphs
where NumWorkers is specific to a given FactorGraph.

The following methods can be used to change number of workers:

-   fg.getSolver().getMultithreadingManager().getNumWorkers()

-   fg.getSolver().getMultithreadingManager().setNumWorkers(num)

-   fg.getSolver().getMultithreadingManager().setNumWorkersToDefault()

<!-- -->

-   fg.Solver.getMultithreadingManager().getNumWorkers()

-   fg.Solver.getMultithreadingManager().setNumWorkers(num)

-   fg.Solver.getMultithreadingManager().setNumWorkersToDefault()

The following global methods can be used to set the number of threads in
the ThreadPool

-   getDimpleNumThreads()

-   setDimpleNumThreads(numThreads)

-   setDimpleNumThreadsToDefault()

<!-- -->

-   ThreadPool.getNumThreads()

-   ThreadPool.setNumThreads(numThreads)

-   ThreadPool.setNumThreadsToDefault()

## Common Belief Propagation Options <a name="BPOptions"></a>

There are a number of options that are applicable to multiple solvers
that are based on some form of message-passing belief propagation. These
include the Sum-Product, Min-Sum, Particle BP, and Junction Tree
solvers. These options are defined in the BPOptions class. The following
options are supported:

integer 1 graph Controls how many iterations to perform when running
solve(). This is not applicable to all solvers. It is currently only
used by the SumProduct, MinSum and ParticleBP solvers. It only makes
sense to set this to a value greater than one if the graph is not singly
connected or "loopy", that is when there is more than one unique path
between two or more nodes in the graph. You can tell if a graph is loopy
using the FactorGraph method isForest(), which will be false if the
graph is not singly connected.

scheduler DefaultScheduler graph Controls what scheduling strategy will
be used for ordering edge updates. The option may either be specified as
an actual scheduler object or as the name or instance of the scheduler
class.

string AllEdgeScheduleValidator graph Specifies validation strategy for
schedules. This is used to validate schedules produced by
CustomScheduler. The default validator ensures that the schedule
contains at least one edge entry for each direction of every edge in the
graph. The NullScheduleValidator may be used to disable validation of
custom schedules.

double 0.0 variables and factors

The belief propogation based solvers supports damping, in which messages
are damped by replacing each message by a weighted sum of the computed
message value and the previous value of that message (when the
corresponding edge was most-recently updated). In the current version of
Dimple, damping is supported only in discrete variables and factors that
connect only to discrete variables[^1]. The damping parameter specifies
a weighting value in the range 0 through 1:

$$\mathrm{message} = \mathrm{computedMessage} \cdot (1 - D) + \mathrm{previousMessage} \cdot D$$

where $D$ is the damping value. So that a value of 0 means that the
previous message will not be considered, effectively turning off
damping. This option applies the same damping parameter to all edges
connected to the variable or factor on which it is set. If you want
different values for different edges, you need to use the option.

double vectorOptionDoubleList *empty* variables and factors This is the
similar to the option but allows you specify different weights for
different edges. Unlike the simple damping option, this usually makes no
sense to set on the graph itself since factors and variables will
typically have different numbers and arrangements of edges. The value
must either be an empty list, indicating that damping should be turned
off, or a list of weights with the same length as the number of siblings
of the affected variable and factor. The damping weights will be applied
in the order in which the siblings are declared. This option takes
precedence over the simple damping option if both are specified for the
same node.

integer integer max discrete factors This specifies the maximum size of
the outgoing messages on the discrete factors on which it is set. If
this number K is less than the full size of the message (i.e. the size
of the domain of the target variable), then only the K-best values --
those with the largest weights -- will be included in the message. This
can results in a faster but more approximate form of inference and is
most suited to graphs with very large-dimension variables. IMPORTANT:
k-best and damping are not compatible with each other[^2]

stringUpdateApproach enum AUTOMATIC discrete factors

This option controls which update algorithm is applied to discrete
factors. The option can take one of three values:

-   NORMAL - Perform updates using just the factor tables. Do not use
    the optimized update technique.

-   OPTIMIZED - Use the optimized update algorithm. Note that factors
    that have only one edge, or factors that do not have all of their
    edges updated simultaneously by the schedule, ignore this setting
    and use the normal approach.

-   AUTOMATIC - Automatically determine whether to use the optimized
    algorithm. The automatic selection algorithm can be tuned through
    the and options.

double 1.0 discrete factors This option is an execution time scaling
factor used when the option is set to AUTOMATIC. It controls how
execution time costs are weighed. The value must be a positive number.

double 10.0 discrete factors This option is an memory allocation scaling
factor used when the option is set to AUTOMATIC. It controls how memory
allocation costs are weighed. The value must be a positive number.

double 1.0 discrete factors This option controls the representation of
the auxiliary tables used by the optimized update algorithm, which is
controlled through the . Internally, the optimized algorithm creates
multiple factor tables to perform the update. This option specifies a
density, below which an auxiliary table uses a sparse representation. It
must be a number in the range \[0.0, 1.0\]. The value 1.0 (the default),
indicates that a sparse representation should be used if there are any
zero-entries in the table. The value 0.0 will prevent the sparse
representation from being used entirely. Sparse tables typically
decrease execution time, but they use more memory. When the update
approach is set to AUTOMATIC, this option impacts both the execution
time and memory allocation estimates used to choose the update approach.

### Sum-Product Solver <a name="SumProductSolver"></a>

Use of the sum-product solver is specified by calling:

    fg.Solver = 'SumProduct';

    SumProductSolverGraph sfg = fg.setSolverFactory(new SumProductSolver());

If no solver is specified, the SumProduct solver is used by default.

The SumProduct solver supports both discrete and continuous variables.
The SumProduct solver uses the sum-product form of belief propagation to
perform inference on a graph. For discrete variables, each message to or
from a factor is in the form of a vector of length equal to the domain
size of the variable. For continuous variables, messages are represented
using a Gaussian parameterization. In some cases, this is an
approximation to the exact message. For Real variables, a message is in
the form of a pair of values representing the mean and variance of the
corresponding Normal distribution. For Complex and RealJoint variables,
a message is in the form of a vector and matrix, representing the mean
and covariance of the corresponding multivariate Normal distribution.

While the Gaussian representation of messages for continuous variables
is sometimes an approximation, there are some specific built-in factors
for which exact Gaussian messages are computed. This can be done when a
factor preserves the Gaussian form of the distribution on each edge. The
following table is a list of such lists built-in factors. See
section [\[sec:builtInFactors\]](#sec:builtInFactors){reference-type="ref"
reference="sec:builtInFactors"} for more information on built-in
factors.

  Built-in Factor                Variable Types         Notes
  ------------------------------ ---------------------- -------------------------------------------------------------------------------------------------------------------------------------
  Normal                         Real                   Applies only if mean and precision parameters are constants and all connected variables are Real and unbounded[^3].
  MultivariateNormal             Complex or RealJoint   All connected variables must be RealJoint or Complex and unbounded.
  Sum                            Real                   All connected variables must be Real and unbounded.
  Subtract                       Real                   All connected variables must be Real and unbounded.
  Negate                         Real                   All connected variables must be Real and unbounded.
  ComplexSum                     Complex                All connected variables must be Complex and unbounded.
  ComplexSubtract                Complex                All connected variables must be Complex and unbounded.
  ComplexNegate                  Complex                All connected variables must be Complex and unbounded.
  RealJointSum                   RealJoint              All connected variables must be RealJoint of the same dimension and unbounded.
  RealJointSubtract              RealJoint              All connected variables must be RealJoint of the same dimension and unbounded.
  RealJointNegate                RealJoint              All connected variables must be RealJoint of the same dimension and unbounded.
  Product                        Real, Constant         Applies only if the product is one unbounded Real variable times one scalar constant to produce an unbounded Real variable.
  MatrixRealJointVectorProduct   RealJoint, Constant    Applies only if the product is one unbounded RealJoint variable times a constant matrix to produce an unbounded RealJoint variable.
  LinearEquation                 Real                   Linear equation. All connected variables must be Real and unbounded.

For factors that are neither discrete-only or listed in the above table,
an approximate computation is used in computing messages from such a
factor. This includes any factor that connects to both discrete and
continuous variables as well as factors that connect only to continuous
variables but do not appear in the list above. The approximate method is
sample based and uses Gibbs sampling to sample from the factor, allowing
approximate messages to be computed from the sample statistics. Several
methods described below allow control over the behavior of these sampled
factors.

For discrete-only factors, two factor update algorithms are available:
normal and optimized. The optimized algorithm can be applied only to
factors with more than one edge, and only when the schedule updates all
of the factor's edges simultaneously. The optimized algorithm computes
the outbound message update with fewer operations than the normal
algorithm, which can decrease execution time; however, it also uses more
memory and increases initialization time. Several options, described
below, influence which algorithm is used. Key among them is the
updateApproach option, which can be set to normal, optimized, or
automatic. When set to automatic, Dimple makes an estimate of the memory
usage and execution time of each algorithm in order to select one.

Factors connected to continuous variables that do not support exact
message computation, instead use a sampled approximation (see
section [0.1.6](#sec:SumProductSolver){reference-type="ref"
reference="sec:SumProductSolver"}) where the sampling is performed using
the Gibbs solver.

For all such factors in a graph, you may set any of the Gibbs solver
options described in
[\[sec:GibbsOptions\]](#sec:GibbsOptions){reference-type="ref+label"
reference="sec:GibbsOptions"} to control how the sampling will be done.
The most important of these options have different default values when
used with Sum-Product. This is accomplished by setting the options on
the solver graph object when it is constructed. In order to override
these defaults, it is necessary to set them on the solver graph (to
apply to all such factors, using `graph.Solver.setOption(...))`, or on
the factor specific factor object (to apply to a single factor, using
`factor.setOption(...))`.

These options and their SumProduct-specific default values are:

-   : 1000 (See SampledFactor.DEFAULT_SAMPLES_PER_UPDATE)

-   : 10 (See SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE)

-   : 1 (See SampledFactor.DEFAULT_SCANS_PER_SAMPLE)

### Min-Sum Solver

Use of the MinSum solver is specified by calling:

    fg.Solver = 'MinSum';

    SFactorGraph sgraph = fg.setSolverFactory(new MinSumSolver());

Unlike the Sum-Product solver, the Min-Sum solver supports only discrete
variables. It only uses the standard BP Options described in .

### Junction Tree Solver <a name="JunctionTreeSolverAPI"></a>

There are two distinct forms of Junction Tree solver in Dimple: the
sum-product form used for computing exact marginal variable beliefs and
the min-sum form used for computing MAP values. The Junction Tree
solvers support only discrete variables.

Use of one of the two forms of Junction Tree solver by calling either:

    fg.Solver = 'JunctionTree';

    JunctionTreeSolverGraph sgraph =
        fg.setSolverFactory(new JunctionTreeSolver());

for the sum-product version or

    fg.Solver = 'JunctionTreeMAP';

    JunctionTreeMAPSolverGraph sgraph =
        fg.setSolverFactory(new JunctionTreeMAPSolver());

for the min-sum version.

The Junction Tree solvers are useful for performing exact inference on
loopy discrete graphical models for which the standard sum-product or
min-sum algorithms will only produce approximate results. This works by
transforming the model into a corresponding non-loopy model, building a
proxy solver layer that connects the original model to the transformed
version and doing inference on that model. If your model already is
non-loopy then you can simply use sum-product or min-sum directly for
exact inference. To test to see if a graph is loopy or not, use :

    useJunctionTree = ~fg.isForest();

    useJunctionTree = !fg.isForest();

One significant limitation when using this solver is that the cost of
inference and amount of memory needed to store the factor tables is
proportional to the size of the tables, which in turn is exponential in
the number of variables represented in a table. The Junction Tree
algorithm may be unable to determine an equivalent tree structure that
has small enough factors either to fit in memory or to perform inference
on in an acceptable amount of time. The typical failure mode in such
cases is to get an when attempting to run solve. The Junction Tree
algorithm works best when used with smaller graphs or larger graphs that
have few loops or are loopy but long and narrow.

#### JunctionTreeOptions <a name="JunctionTreeOptions"></a>

The following options are available for use with both versions of the
Junction Tree solver.

Because exact inference can be done in a single iteration, the Junction
Tree solver fixes the number of iterations to one and will ignore
attempts to set it to another value. Because damping would result in
inexact inference, the Junction Tree solver does not provide options for
using damping.

boolean false graph Specifies whether to use conditioning when
constructing the transformation of the model. When true, then any
variables in the model that have a fixed value will be disconnected from
the rest of the graph in the transformed version and its value will be
incorporated in the factors of the transformed model. This will produce
a more efficient transformed model when there are fixed values in the
original model. Using this will require that a new transformation be
computed every time a fixed value changes.

integer 1 graph Specifies the maximum number of times the junction tree
transformer should try to determine an optimal transformation. Each
attempt uses a greedy "variable elimination" algorithm using a
randomly chosen cost function and random choices to break ties, so more
iterations could produce a more efficient tree transformation.

### Gibbs Solver <a name="GibbsSolverAPI"></a>

Use of the Gibbs solver is specified by calling:

    fg.Solver = 'Gibbs';

    GibbsSolverGraph sfg = fg.setSolverFactory(new GibbsSolver());

The Gibbs solver supports both discrete and continuous variables.

This solver performs Gibbs sampling on a factor graph. It supports a
variety of output information on the sampled graph, including the best
joint sample (lowest energy), marginals of each variable (discrete
variables only), and a full set of samples for a user-selected set of
variables. The solver supports both sequential and randomized scan, and
it supports tempering with an exponential annealing schedule.

The Gibbs solver supports several user selectable generic samplers
(those that don't require specific conjugacy relationships). The
following table lists the available generic samplers, and the variable
types supported by each.

  Sampler           Variable Type     Description
  ----------------- ----------------- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  CDFSampler        Discrete[^4]      Samples from the full conditional distribution of the variable. This is the default sampler for discrete variables.
  SliceSampler      Real[^5]          Slice sampler using the doubling procedure. See Neal, Slice Sampling (2000). This is the default sampler for real variables.
  MHSampler         Discrete & Real   Metropolis-Hastings sampler. For discrete variables, the default proposal kernel is uniform over values other than the current value. For real variables, the default proposal kernel is Normal with standard deviation 1 (the standard deviation is user settable). Alternate proposal kernels are also available (see below).
  SuwaTodoSampler   Discrete          Suwa-Todo sampler. See Suwa, Todo, Markov Chain Monte Carlo Method without Detailed Balance (2010).
  BlockMHSampler    Discrete & Real   Block Metropolis-Hastings sampler. Allows block proposals for a collection of more than one variable at a time. In the current version of Dimple, there are no built-in general purpose block proposal kernels. To use this sampler, a custom block proposal kernel must be written in Java, and used by specifying a block schedule entry that references this proposal kernel. See section [\[sec:BlockScheduleEntries\]](#sec:BlockScheduleEntries){reference-type="ref" reference="sec:BlockScheduleEntries"} for more information.

In cases where the factors of the graph support a conjugate
distribution, the solver will automatically determine this and use the
appropriate conjugate sampler. The following table lists the supported
conjugate samplers and the corresponding factors that support them. The
corresponding sampler will be used for a given variable if all of the
edges connected to that variable support the same sampler[^6].

  Sampler                   Built-in Factor                            Edge
  ------------------------- ------------------------------------------ ----------------
  BetaSampler               Beta                                       value
                            Binomial                                   $\rho$
  DirichletSampler          Dirichlet                                  value
                            Categorical                                $\alpha$
                            DiscreteTransition                         $\alpha$
  GammaSampler              Gamma                                      value, $\beta$
                            NegativeExpGamma                           $\beta$
                            Normal                                     $\tau$
                            LogNormal                                  $\tau$
                            Poisson                                    $\lambda$
                            CategoricalUnnormalizedParameters          $\alpha$
                            DiscreteTransitionUnnormalizedParameters   $\alpha$
  NegativeExpGammaSampler   NegativeExpGamma                           value
                            CategoricalEnergyParameters                $\alpha$
                            DiscreteTransitionEnergyParameters         $\alpha$
  NormalSampler             Normal                                     value, $\mu$
                            LogNormal                                  $\mu$

Additionally, conjugate sampling is supported across a subset of
applicable deterministic functions. In the current version of Dimple,
this includes the following factors:

  Factor Function   Edges     Condition
  ----------------- --------- -----------------------------------------------------------------------------------------------------------------------------------------------------
  Multiplexer       in, out   If any of the *in* variables would support the same conjugate sampler as the *out* variable, then those *in* variables will use conjugate sampling.

The Gibbs solver automatically performs block-Gibbs updates for
variables that are deterministically related. The Gibbs solver
automatically detects deterministic relationships associated with
built-in deterministic factor functions (see
section [\[sec:builtInFactors\]](#sec:builtInFactors){reference-type="ref"
reference="sec:builtInFactors"} for a list of these functions).

For user-defined factors specified by MATLAB factor functions or factor
tables, the Gibbs solver will detect if they are deterministic functions
as along as the factor is marked as the directed outputs are indicated
using the DirectedTo property, as described in
section [\[sec:Factor.DirectedTo\]](#sec:Factor.DirectedTo){reference-type="ref"
reference="sec:Factor.DirectedTo"}.

For user-defined factors specified by factor functions or factor tables,
the Gibbs solver will detect if they are deterministic functions as
along as the factor is marked as the directed outputs are indicated
using the DirectedTo property, as described in
section [\[sec:Factor.DirectedTo\]](#sec:Factor.DirectedTo){reference-type="ref"
reference="sec:Factor.DirectedTo"}.

The Gibbs solver also automatically performs block-Gibbs updates for
certain built-in factors that Gibbs sampling on individual variables
would fail due to the dependencies between variables imposed by the
factor. In these cases, a custom proposal distribution ensures that
proposals are consistent with the constraints of the factor. However,
the custom proposals are not assured to result in efficient mixing. In
the current version of Dimple, the following built-in factors
automatically implement block-Gibbs updates:

-   Multinomial

-   MultinomialUnnormalizedParameters

-   MultinomialEnergyParameters

Most Dimple methods work more-or-less as normal when using the Gibbs
solver, but in some cases the interpretation is slightly different than
for other solvers. For example, the .Belief method for a discrete
variable returns an estimate of the belief based on averaging over the
sample values.

NOTE: The setNumIterations() method is not supported by the Gibbs solver
as the term "iteration" is ambiguous in this case. Instead, the method
setNumSamples() should be used to set the length of the run. The
Solver.iterate() method performs a single-variable update in the case of
the Gibbs solver, rather than an entire scan of all variables.

The following sections list the solver-specific aspects of the API for
the Gibbs solver.

#### GibbsOptions <a name="GibbsOptions"></a>

The following options affect the behavior of various aspects of the
Gibbs solver:

See description in
[\[option:SolverOptions.maxAutomaticFactorTableSize\]](#option:SolverOptions.maxAutomaticFactorTableSize){reference-type="ref+label"
reference="option:SolverOptions.maxAutomaticFactorTableSize"}.

integer 1 graph Specifies the number of samples to be generated when
solving the graph post burn-in. (This value times the value of plus one
determines the total number of samples that will be produced.)

integer 0 graph Specifies sampling rate in terms of the number of
scans[^7] of the graph to perform for each sample.

integer 0 graph Specifies the number of scans of the graph to perform
during the burn-in phase before generating samples.

integer 0 graph Specifies the number of random restarts (zero by
default, which means run once and don't restart). For a value greater
than zero, the after running the specified number of samples, the solver
is restarted with the variable values randomized, and re-run (including
burn-in). The sample values (the best sample value, or all samples, if
requested) are extracted across all runs.

boolean false graph Specifies whether to save all sample values for
variables when running Gibbs. Note that this is practical only if the
number of variables in the graph times the number of samples per
variable is reasonably sized.

boolean false graph Specifies whether to save scores for all generated
samples in Gibbs. If true, then for each sample the total
energy/log-likelihood a.k.a. *score* of the graph will be saved. The
saved scores can later be retrieved by the getAllScores() method
described below.

scheduler GobbsDefaultScheduler graph Controls what scheduling strategy
will be used for ordering variable updates. The option may either be
specified as an actual scheduler object or as the name or instance of
the scheduler class.

string VariablesOnlyScheduleValidator graph Specifies validation
strategy for schedules. This is used to validate schedules produced by
CustomScheduler. The default validator ensures that the schedule
contains at least one node entry for every variable in the graph. The
NullScheduleValidator may be used to disable validation of custom
schedules.

stringIGenericSampler class CDFSampler variables Specifies the default
sampler to use for discrete variables when a conjugate sampler is not
suitable. The sampler may be configured by use of additional options
defined by each sampler type. See
[0.1.13](#sec:Samplers){reference-type="ref+label"
reference="sec:Samplers"} for more details.

stringIGenericSampler class SliceSampler variables Specifies the default
sampler to use for real-valued variables (including Complex and
RealJoint) when a conjugate sampler is not suitable. The sampler may be
configured by use of additional options defined by each sampler type.
See [0.1.13](#sec:Samplers){reference-type="ref+label"
reference="sec:Samplers"} for more details.

boolean true variables Specifies whether to use conjugate sampling when
available for a given variable. Note that if a specific sampler has been
specified for a particular variable (by setting the option directly on
the model or solver variable object) then a conjugate sampler will not
be used regardless.

boolean false variables Specifies whether to compute the belief moments
(mean vector and covariance matrix) for RealJoint (and Complex)
variables while sampling. To minimize computation, these are not
computed by default for RealJoint variables (Real variables always
compute similar statistics, and do not have a corresponding option to
enable them). If true, the belief moments are computed for each sample
on-the-fly (without saving all samples). The computed moments can later
be retrieved by the getSampleMean and getSampleCovariance
solver-specific methods for the variable (see
section [\[sec:RealJointVariableSpecificMethods\]](#sec:RealJointVariableSpecificMethods){reference-type="ref"
reference="sec:RealJointVariableSpecificMethods"})

boolean false graph Specifies whether to use a tempering and annealing
process when running Gibbs.

double 1.0 graph Specifies the rate at which the temperature will be
lowered during the tempering and annealing process. This rate is
specified in terms of the number of samples required for the temperature
to be lowered by half. This value is only used if annealing has been
enabled as specified by the enableAnnealing option.

double 1.0 graph Specifies the initial temperature to use when annealing
is enabled (as specified by the enableAnnealing option).

The following methods are available on a graph set to use the Gibbs
solver:

Variable initialization (both on the first run and subsequent restarts)
is randomized whenever possible. For a discrete variable, the value is
sampled from the Input (uniform if an input is not specified). For a
real variable, if an Input is specified and the Input supports one of
the conjugate samplers listed above, that sampler is used to initialize
the variable. If bounds are also specified for the variable domain, the
values is truncated to fall within the bounds. If only bounds are
specified (which are finite above and below), then the value is
uniformly sampled from within the bounds. If no finite bounds are
specified and there is no input, the variable is initialized to zero (or
the value specified by setInitialSampleValue) on the initial run, and
left at the final value of the previous run on restart.

Enable or disable the use of tempering, or determine if tempering is in
use.

    graph.Solver.setTemperature(T);
    graph.Solver.getTemperature();

    sfg.setTemperature(T);
    sfg.getTemperature();

Set/get the current temperature. Setting the current temperature
overrides the current annealing temperature.

    graph.Solver.getAllScores();

    sfg.getAllScores();

Returns an array including the score value for each sample. This method
only returns a non-empty value if the option was set to true on the
graph before generating samples.

    graph.Solver.getTotalPotential();

    sfg.getTotalPotential();

After running the solver, returns the total potential (score) over all
factors of the graph (including input priors on variables) given the
most recent sample values.

    graph.Solver.sample(numSamples)

    sfg.sample(numSamples)

This method runs a specified number of samples without re-initializing,
burn-in, or random-restarts (this is distinct from iterate(), which runs
a specified number of single-variable updates). Before running this
method for the first time, the graph must be initialized using the
initialize() method.

    graph.Solver.burnIn()

    sfg.burnIn()

Run the burn-in samples independently of using solve (which
automatically runs the burn-in samples). This may be run before using
sample() or iterate().

    graph.Solver.getRejectionRate()

    sfg.getRejectionRate()

Get the overall rejection rate over the entire graph. The rejection rate
is the ratio of the number of MCMC proposals that were rejected to the
total number of sampler updates. Rejections only occur in MCMC samplers,
such as the MHSampler. In other samplers, such as conjugate samplers or
the CDFSampler, rejection doesn't occur and the rate for variables that
use these samplers is zero (in these cases, sampling the same value
twice in a row is not considered rejection). When getting the rejection
rate for the entire graph, both the number of rejections and number of
updates is counted for all variables, as well as all blocks of variables
over which a block sampler is used. These counts are accumulated from
the time the graph is initialized, which automatically occurs when
running solve(). This includes both burn-in and subsequent sampling.
These counts can also be reset explicitly using the
resetRejectionRateStats method (see below), which allows these values to
be determined, for example, during a specific set of samples.

    graph.Solver.resetRejectionRateStats()

    sfg.resetRejectionRateStats()

Explicitly reset the rejection-rate statistics. These are automatically
reset when the graph is initialized, which automatically occurs when
running solve(), but may be reset manually at other times using this
method.

    variable.Solver.getCurrentSample();

    Object d = ((GibbsDiscrete)variable.getSolver()).getCurrentSample();
    double r = ((GibbsReal)variable.getSolver()).getCurrentSample();
    double[] rj = ((GibbsRealJoint)variable.getSolver()).getCurrentSample();
    Value val= ((ISolverVariableGibss)variable.getSolver()).getCurrentSampleValue();

Returns the current sample value for a variable.

    variable.Solver.getAllSamples();

    Object[] ds = ((GibbsDiscrete)variable.getSolver()).getAllSamples();
    double[] rs = ((GibbsReal)variable.getSolver()).getAllSamples();
    double[][] rjs = ((GibbsRealJoint)variable.getSolver()).getAllSamples();

Returns an array including all sample values seen so far for a variable.
Over multiple variables, samples with the same index correspond to the
same joint sample value. This method only returns a non-empty value if
the options was enabled for the variable when sampling was performed.

    variable.Solver.getBestSample();

    Object d = ((GibbsDiscrete)variable.getSolver()).getBestSample();
    double r = ((GibbsReal)variable.getSolver()).getBestSample();
    double[] rj = ((GibbsRealJoint)variable.getSolver()).getBestSample();

Returns the value of the best sample value seen so far, where best is
defined as the sample with the minimum total potential over the graph
(sum of -log of the factor values and input priors). When getting the
best sample from multiple variables, they all correspond to the same
sample in time, thus should be a valid sample from the joint
distribution.

    variable.Solver.setInitialSampleValue(initialSampleValue)
    variable.Solver.getInitialSampleValue()

    variable.getSolver().setInitialSampleValue(initialSampleValue)
    variable.getSolver().getInitialSampleValue()

Set/get the initial sample value to be used as the starting value for
this variable. This value is used only on the first run (not subsequent
restarts). Setting this value overrides any randomization of the
starting value on the first run.

    variable.Solver.setSampler(samplerName);
    variable.Solver.getSampler();
    variable.Solver.getSamplerName();

    variable.getSolver().setSampler(samplerName);
    variable.getSolver().getSampler();
    variable.getSolver().getSamplerName();

Set/get the sampler to be used for this variable. Setting the sampler
for a given variable overrides the default sampler for the given
variable type, and also overrides any conjugate sampler that might
otherwise be used. Using this method the sampler may be set only to one
of the generic samplers appropriate for the given variable type.

The getSampler method returns the sampler object, while the
getSamplerName method returns a string indicating the name of the
sampler being used for this variable. Automatic assignment of a
conjugate sampler is done at graph initialization time, so in order to
determine what sampler will actually be used, these methods must be
called either after a call to the graph initialize method, or after
running the solver.

    variable.Solver.getRejectionRate()

    variable.getSolver().getRejectionRate()

Get the rejection rate for the sampler used for a specific variable. The
rejection rate is the ratio of the number of MCMC proposals that were
rejected to the total number of sampler updates. Rejections only occur
in MCMC samplers, such as the MHSampler. In other samplers, such as
conjugate samplers or the CDFSampler, rejection doesn't occur and the
rate for variables that use these samplers is zero (in these cases,
sampling the same value twice in a row is not considered rejection).
These counts are accumulated from the time the graph is initialized,
which automatically occurs when running solve(). This includes both
burn-in and subsequent sampling. These counts can also be reset
explicitly using the resetRejectionRateStats method (see below), which
allows these values to be determined, for example, during a specific set
of samples.

    variable.Solver.getNumScoresPerUpdate()

    variable.getSolver().getNumScoresPerUpdate()

Returns the average number of score computations performed per update
when sampling from this variable. Use of an MCMC sampler requires
computation of the score (energy) for specific settings of the
variables. For some samplers, such as the slice sampler, the number of
times the score is computed varies, and depends on the particular values
and the form of the distribution. The returned value indicates the
average number of times the score has been computed. If a non-MCMC-based
sampler is used, the returned value will be zero. The count is
accumulated from the time the graph is initialized, which automatically
occurs when running solve(). This includes both burn-in and subsequent
sampling. The count can also be reset explicitly using the
resetRejectionRateStats method (see below), which allows this value to
be determined, for example, during a specific set of samples.

    variable.Solver.resetRejectionRateStats()

    variable.getSolver().resetRejectionRateStats()

Explicitly reset the rejection-rate statistics for a specific variable
(the statistics for computing the rejection rate as well as the number
of scores per update). These are automatically reset when the graph is
initialized, which automatically occurs when running solve(), but may be
reset manually at other times using this method.

The following methods apply only to Discrete, Bit, and FiniteField
variables when using the Gibbs solver.

    variable.Solver.getSampleIndex;

    variable.getSolver().getSampleIndex();

Returns the index of the current sample for a variable, where the index
refers to the index into the domain of the variable.

    variable.Solver.getAllSampleIndices;

    variable.getSolver().getAllSampleIndices();

Returns an array including the indices of all samples seen so far for a
variable.

    variable.Solver.getBestSampleIndex;

    variable.getSolver().getBestSampleIndex();

Returns the index of the best sample seen so far.

    variable.Solver.setInitialSampleIndex(initialSampleIndex)
    variable.Solver.getInitialSampleIndex()

    variable.getSolver().setInitialSampleIndex(initialSampleIndex)
    variable.getSolver().getInitialSampleIndex()

Set/get the initial sample index associated with the starting value for
this variable. The value associated with this index is used only on the
first run (not subsequent restarts). Setting this index overrides any
randomization of the starting value on the first run.

The following methods apply only to Real variables when using the Gibbs
solver.

    variable.Solver.getSampleMean;

    variable.getSolver().getSampleMean();

Returns the mean value of all samples that have been collected. This is
and estimate of the mean of the belief for the corresponding variable.

    variable.Solver.getSampleVariance;

    variable.getSolver().getSampleVariance();

Returns the variance of all samples that have been collected. This is
and estimate of the variance of the belief for the corresponding
variable.

#### RealJointVariableSpecificMethods <a name="RealJointVariableSpecificMethods"></a>

The following methods apply only to RealJoint and Complex variables when
using the Gibbs solver.

    variable.Solver.getSampleMean;

    variable.getSolver().getSampleMean();

Returns the mean vector of all samples that have been collected. This is
and estimate of the mean of the belief for the corresponding variable.
This method is only available if, prior to performing inference, the
option is set to true.

    variable.Solver.getSampleCovariance;

    variable.getSolver().getSampleCovariance();

Returns the covariance matrix computed over all samples that have been
collected. This is and estimate of the covariance of the belief for the
corresponding variable. This method is only available if, prior to
performing inference, the option is set to true.

    factor.Solver.getPotential();

    factor.getSolver().getPotential();

Returns the potential value of a factor given the current values of its
connected variables.

    factor.Solver.getPotential(values);

    factor.getSolver().getPotential(values);

Get the potential value of a factor given the variable values specified
by the argument vector. The argument must be a vector with length equal
to the number of connected variables. For a table-factor (connected
exclusively to discrete variables), each value corresponds the index
into the domain list for that variable (not the value of the variable
itself). For a real-factor (connected to one or more real variables),
each value corresponds to the value of the variable.

The built-in schedulers designed for belief propagation are not
appropriate for the Gibbs solver. Instead, there are two built-in
schedulers specifically for the Gibbs solver:

-   GibbsSequentialScanScheduler

-   GibbsRandomScanScheduler

The GibbsSequentialScanScheduler chooses the next variable for updating
in a fixed order. It updates all variables in the graph, completing an
entire scan, before repeating the same fixed order. (In Gibbs literature
this seems to be known as a sequential-scan, systematic-scan, or
fixed-scan schedule.)

The GibbsRandomScanScheduler randomly selects a variable for each update
(with replacement).

The default scheduler when using the Gibbs solver is the
GibbsSequentialScanScheduler, which is used if no scheduler is
explicitly specified.

The user may specify a custom schedule when using the Gibbs solver. In
this case, the schedule should include only Variable node updates (not
specific edges), and no Factor updates (any Factor updates specified
will be ignored).

To explicitly specify a scheduler, use the Scheduler or Schedule
property of the FactorGraph (see
sections [\[sec:FactorGraph.Scheduler\]](#sec:FactorGraph.Scheduler){reference-type="ref"
reference="sec:FactorGraph.Scheduler"}
and [\[sec:FactorGraph.Schedule\]](#sec:FactorGraph.Schedule){reference-type="ref"
reference="sec:FactorGraph.Schedule"}).

#### BlockScheduleEntries <a name="BlockScheduleEntries"></a>

The Gibbs solver allows a schedule to optionally include block entries
that allow a group of variables to be updated at once. A block schedule
entry can either be included in a custom schedule or added to the
schedule produced by one of the Gibbs-specific built-in schedulers. A
block schedule entry includes two pieces of information:

-   A reference to a block sampler, which is used to perform the update

-   A list of variables to be included in this block

In the current version of Dimple, the only built-in block sampler is the
BlockMHSampler, which implements block Metropolis-Hastings sampling for
the variables included in the block. The BlockMHSampler requires a block
proposal kernel to be specified. In the current version of Dimple, there
are no built-in general purpose block proposal kernels. To use this
sampler, a custom block proposal kernel must be written in Java (see
section [\[sec:CreatingACustomProposalKernel\]](#sec:CreatingACustomProposalKernel){reference-type="ref"
reference="sec:CreatingACustomProposalKernel"}).

To specify a block schedule entry in a custom schedule, the schedule
entry consists of a cell-array in which the first element of the cell is
a reference to a Java sampler object, and the subsequent entries are the
variables to be included in the block. For example:

    import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
    ...
    fg.Scheduler = CustomScheduler(fg, 'GibbsOptions.scheduler', {BlockMHSampler(MyProposalKernel), a, b, c}, d, e);

To add a block schedule entry to a custom schedule, the the argument to
the `add` method is a reference to a sampler object, and the subsequent
entries are the variables to be included in the block. For example:

    CustomScheduler s = new CustomScheduler(fg, GibbsOptions.scheduler);
    s.addBlock(new BlockMHSampler(new MyProposalKernel()), a, b, c));
    s.addNode(new NodeScheduleEntry(d));
    s.addNode(new NodeScheduleEntry(e));
    fg.setOption(GibbsOptions.scheduler, s);

In the above example, we create a block schedule entry that updates
variables a, b, and c together, with separate schedule entries for
variables d and e. The constructor for the BlockMHSampler requires a
proposal kernel. In the above example, "MyProposalKernel" is a
user-provided custom proposal class written in Java. (Note that the
"import" line in the above example is simply to avoid having to write
the fully qualified name each time the BlockMHSampler is used.)

Block schedule entries can also be used with either of the
Gibbs-specific built-in schedulers described above. When a block entry
is added in this way, for each of the variables included in a block
entry, the individual variable entries that would have been present in
the schedule are removed. That is, those variables are only included in
the corresponding block entry (or entries) and are not also updated
independently. In case of the GibbsRandomScanScheduler, each update
selects an entry randomly from among all blocks plus all variables that
are not in a block.

A block schedule entry can be added when using a built-in Gibbs-specific
scheduler using:

    fg.Scheduler.addBlockWithReplacement(blockSampler, block);

where block is either a VariableBlock or a list of variables used to
define the block.

The following example shows adding a block schedule entry that includes
two elements of the variable x, and variable y.

    fg.Scheduler.addBlockWithReplacement(BlockMHSampler(MyKernel), x(2:3), y);

Multiple block schedule entries can be added at once using:

    fg.Scheduler.addBlocksWithReplacement({blockSampler1, block1}, {blockSampler1, block2}, ...);

The following example shows adding a block schedule entry that includes
two elements of the variable x, and variable y, and a second block that
includes variables a and b.

    fg.Scheduler.addBlocksWithReplacement({BlockMHSampler(MyKernel), x(2:3), y}, {BlockMHSampler(MyKernel2), a, b});

A block schedule entry can be added when using a built-in Gibbs-specific
scheduler by defining a VariableBlock containing the variables and using
the addBlockScheduleEntry method on the scheduler. For instance, the
following example shows adding a block schedule entry that includes the
variables a, b, and c:

    IGibbsScheduler scheduler = GibbsOptions.scheduler.getOrDefault(fg);
    VariableBlock block = fg.addVariableBlock(a, b, c);
    scheduler.addBlockWithReplacement(new BlockMHSampler(myKernel), block);

Note that to use the addBlockScheduleEntry method, the scheduler must
first have been explicitly set to one of the Gibbs-specific schedulers.

To implement a custom block proposal kernel, a new Java class must be
created that implements the IBlockProposalKernel interface. See
section [\[sec:CreatingACustomProposalKernel\]](#sec:CreatingACustomProposalKernel){reference-type="ref"
reference="sec:CreatingACustomProposalKernel"} for more detail.

### Particle BP Solver

Use of the particle BP solver is specified by calling:

    fg.Solver = 'ParticleBP';

    ParticleBPSolverGraph sfg = fg.setSolverFactory(new ParticleBPSolver());

The following lists the solver-specific options for the ParticleBP
solver.

The following options affect the behavior of various aspects of the
ParticleBP solver:

integer 1 real variables Specifies the number of particles used to
represent the variable. This option takes affect when the solver
variable is constructed and when it is initialized.

integer 1 real variables For variables on which it is set, specifies the
number of updates per particle to perform each time the particle is
resampled.

integer 1 graph Specifies the number of iterations between re-sampling
all of the variables in the graph. Default is 1, meaning resample
between every iteration.

1x2 vectortwo-element OptionDoubleList \[-infinity infinity\] variables
Set the range over which the initial particle values will be defined.
The initial particle values are uniformly spaced between the min and max
values specified. If the range is specified using this method, it
overrides any other initial value. Otherwise, if a finite domain has
been specified, the initial particle values are uniformly spaced between
the lower and upper bound of the domain. Otherwise, all particles are
initially set to zero.

    % Set particle range to the unit interval for all variables in the graph:
    graph.setOption('ParticleBPOptions.initialParticleRange', [0.0, 1.0]);

    // Set particle range to the unit interval for all variables in the graph:
    ParticleBPOptions.initialParticleRange.set(graph, 0.0, 1.0);

stringIProposalKernel class NormalProposalKernel real variables
Specifies the type of proposal kernel to use for the specified
variables. The selected proposal kernel may have additional options that
can be used to configure its behavior. These can also be set on the
variables.

boolean false graph Determines whether to use a tempering and annealing
process during inference.

double 1.0 graph Specifies the rate at which the temperature will be
lowered during the tempering and annealing process. This rate is
specified in terms of the number of iterations required for the
temperature to be lowered by half. This value is only used if annealing
has been enabled as specified by the enableAnnealing option.

double 1.0 graph Specifies the initial temperature to use when annealing
is enabled (as specified by the enableAnnealing option).

The following solver-specific methods are available on the solver graph.
(It is assumed that sfg is a variable of type ParticleBPSolverGraph
obtained when the solver was set or by casting the result of the
getSolver() method.)

    graph.Solver.setTemperature(newTemperature);
    temp = graph.Solver.getTemperature();

    sfg.setTemperature(newTemperature);
    double temp = sfg.getTemperature();

Set/get the current temperature. Setting the current temperature
overrides the current annealing temperature. This should rarely be
necessary.

The Particle BP solver supports both discrete and real variables. For
discrete variables, the solver uses sum-product BP as normal, and all of
the corresponding methods for the sum-product solver may be used for
discrete variables. For real variables, several solver-specific methods
are defined, as follows.

#### ParticleBPRealVariableSpecificMethods <a name="ParticleBPRealVariableSpecificMethods"></a>

    variable.Solver.setProposalStandardDeviation(stdDev);
    variable.Solver.getProposalStandardDeviation();

    variable.getSolver().setProposalStandardDeviation(stdDev);
    variable.getSolver().getProposalStandardDeviation();

Set/get the standard deviation for a Gaussian proposal distribution (the
default is 1).

    variable.Solver.getParticleValues();

    ((ParticleBPReal)variable.getSolver()).getParticleValues();

Returns the current set of particle values associated with the variable.

    variable.Solver.getBelief(valueSet);

    ((ParticleBPReal))variable.getSolver()).getBelief(valueSet);

Given a set of values in the domain of the variable, returns the belief
evaluated at these points. The result is normalized relative to the set
of points requested so that the sum over the set of returned beliefs is
1.

NOTE: the generic variable method Belief (or getBeliefs() with no
arguments) operates similarly to the discrete-variable case, but the
belief values returned are those at the current set of particle values.
Note that this representation does not represent a set of weighted
particles. That is, the particle positions are distributed approximately
by the belief and the belief values represent the belief. It remains to
be see if this should be the representation of belief that is used, or
if an alternative representation would be better. The alternative
solver-specific getBelief(valueSet) method allows getting the beliefs on
a user-specified set of values, which may be uniform, and would not have
this unusual interpretation.

### LP Solver <a name="LPSolver"></a>

Use of the linear programming (LP) solver is specified by calling:

    fg.Solver = 'LP';

    fg.setSolverFactory(new LPSolver());

The LP solver transforms a factor graph MAP estimation problem into an
equivalent linear program, which is solved using a linear programming
software package. The solver can either be a linear programming solver
(in which case the MAP is estimated using an LP relaxation, with no
guarantees of correctness), or by an integer linear programming (ILP)
solver, in which case the solution is guaranteed to be the MAP. Because
this solver release on an external package, you will need to install and
configure the specified package before using this solver.

The LP solver supports only discrete variables.

In the current version of Dimple (version 0.7), there is no support for
rolled-up graphs when using the LP solver.

The following options are applicable to the LP solver:

The following options affect the behavior of various aspects of the LP
solver:

string '' graph Selects which external LP solver will be used to solve
the linear program. Valid values include 'matlab', 'CPLEX', 'GLPK',
'Gurobi', 'LpSolve', 'MinSate', 'Mosek', and 'SAT4J'. The default value
is synonomous with specifying 'matlab' and will delegate the solver
specified by the MatlabLPOption that will be run from the MATLAB
frontend. This will obviously only work when running Dimple from MATLAB.
None of these solvers are included with Dimple and must be installed and
configured separately. The interface for the non-MATLAB based solvers is
provided by the third-party Java ILP package. See
[javailp.sourceforge.net](http://javailp.sourceforge.net) for more
information about configuring various solvers.

string '' graph

Selects which LP solver will be run from MATLAB to solve the linear
program. This option is only relevant if the LPSolver option has been
set to 'matlab' and Dimple is being run from MATLAB. The choices for the
string solvername are 'matlab', 'glpk', 'glpkIP', 'gurobi', and
'gurobiIP'. The 'matlab', 'glpk', and 'gurobi' solvers are linear
programming solvers, while 'glpkIP' and 'gurobiIP' are ILP solvers. The
default value is synonomous with 'matlab'.

Using the matlab LP solver requires the the MATLAB Optimization Toolbox.
Using 'glpk' or 'glpkIP' requires glpkmex to be in the matlab path, and
'gurobi' and 'gurobiIP' require the gurobi matlab interface to be in the
matlab path; in either case the appropriate packages will need to be
obtained and installed.

### Proposal Kernels <a name="ProposalKernels"></a>

The following proposal kernels are provided by Dimple for use in
samplers. Additional kernels may be added by creating new proposal
kernel Java classes that implement the appropriate interfaces.

#### NormalProposalKernel <a name="NormalProposalKernel"></a>

The following options may be used to configure this kernel:

double 1.0 variables Specifies the standard deviation to use on
NormalProposalKernel instances attached to the variables that are
affected by the option setting. The value must be non-negative.

#### CircularNormalProposalKernel <a name="CircularNormalProposalKernel"></a>

The CircularNormalProposalKernel makes proposals from a Normal
distribution on a circularly wrapping range of the real line. For
example, setting the bounds of the range to $-\pi$ and $\pi$ would
create proposals representing angles on a circle.

Since this is a subclass of NormalProposalKernel, the standardDeviation
option defined for that class will also affect this one. The following
additional options may also be used:

double $-\pi$ variables Specifies lower bound to use on
CircularNormalProposalKernel instances attached to the variables that
are affected by the option setting.

double $+\pi$ variables Specifies upper bound to use on
CircularNormalProposalKernel instances attached to the variables that
are affected by the option setting.

[]{#sec:UniformDiscreteProposalKernel
label="sec:UniformDiscreteProposalKernel"}

This kernel does not have any configurable options.

### Samplers <a name="Samplers"></a>

Samplers are used by sampling solvers (e.g. Gibbs) to generate samples
for variables either singly or in blocks.

The following non-conjugate single variable samplers are currently
available:

#### CDFSampler <a name="CDFSampler"></a>

The CDFSampler can be used with discrete variable types. It samples from
the full conditional distribution of the variable. It is the default
sampler used for discrete variables in the Gibbs solver.

It does not support any configuration options.

#### MHSampler <a name="MHSampler"></a>

The MHSampler may be used for discrete or real variables. It implements
the Metropolis-Hastings sampling algorithm. The sampler may be
configured to use different proposal kernels for discrete and real
variables as described below.

The following options may be used to configure the MHSampler for a given
variable:

stringIProposalKernel class UniformDiscreteProposalKernel variables
Specifies the proposal kernel to use when using the MHSampler on
discrete variables for which this option setting is visible. Depending
on the kernel selected, it may also be configured by additional option
settings. See [0.1.12](#sec:ProposalKernels){reference-type="ref+label"
reference="sec:ProposalKernels"} for more details.

stringIProposalKernel class NormalProposalKernel variables Specifies the
proposal kernel to use when using the MHSampler on real variables for
which this option setting is visible. Depending on the kernel selected,
it may also be configured by additional option settings. See
[0.1.12](#sec:ProposalKernels){reference-type="ref+label"
reference="sec:ProposalKernels"} for more details.

#### SliceSampler <a name="SliceSampler"></a>

The slice sampler may be used with real variables. It implements the
algorithm described in Neal's paper "Slice Sampling" 2010. It is the
default sampler used for real variables in the Gibbs solver.

The following options may be used to configure the SliceSampler for a
given variable:

double 1.0 variables The size of the initial slice to use when sampling
for SliceSampler instances used by variables that are affected by the
option setting. For variables with a natural range that is much smaller
or much larger than the default value of one, it may be beneficial to
modify this value.

integer 10 variables The maximum number of doublings used during the
doubling phase of the slice sampler. The maximum slice interval is on
the order of $initialSliceWidth \cdot 2^{maximumDoublings}$

#### SuwoTodoSampler <a name="SuwoTodoSampler"></a>

The Suwo-Todo sampler can be used for discrete variables. It implements
the algorithm described in Suwa and Todo's paper "Markov Chain Monte
Carlo Method without Detailed Balance" (2010).

This sampler does not support any configuration options.

[^1]: Support for damping for continuous variables may be added in a
    future release.

[^2]: This restriction may be removed in a future version of Dimple.

[^3]: Unbounded means that the domain of the variable must not have
    finite upper or lower bounds

[^4]: In this table, Discrete support implies any of the discrete
    variable types, including Discrete and Bit.

[^5]: In this table, Real support implies any of the continuous variable
    data types, including Real, Complex, and RealJoint.

[^6]: Additionally, for the conjugate sampler to be used, the domain of
    the variable must not be bounded to a range smaller than the natural
    range of the corresponding distribution.

[^7]: A scan is a number of updates to each variable equal to the number
    of variables in the graph. For a fixed schedule, this typically
    means updating each variable exactly once. For a random update
    schedule, this is not necessarily the case.
