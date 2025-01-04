## Streaming Data

### Variable Stream Common Properties and Methods

Dimple supports several types of variable streams:

-   DiscreteStream

-   BitStream

-   RealStream

-   RealJointStream

-   ComplexStream

Each of these share common properties and method listed in the following
sections.

Read-write. When written, connects the variable stream to a data source
(see section [0.1.8](#sec:DataSource){reference-type="ref"
reference="sec:DataSource"}). The data sink must be of a type
appropriate for the particular variable stream type.

Read-write. When written, connects the variable stream to a data sink
(see section [0.1.12](#sec:DataSink){reference-type="ref"
reference="sec:DataSink"}). The data sink must be of a type appropriate
for the particular variable stream type.

Read-only. Indicates the dimensions of the variable stream. The
dimensions correspond to the size of the variable array at each position
in the stream.

Read-only. Indicates the number of elements in the variable stream that
are actually instantiated. Each element corresponds to one copy of the
variable or variable array at a specific point in the stream. Dimple
instantiates the minimum number of contiguous elements to cover the
slices of the stream that are actually used in factors (see
section [\[sec:VariableStream.getSlice\]](#sec:VariableStream.getSlice){reference-type="ref"
reference="sec:VariableStream.getSlice"}), plus the number of additional
elements to cover the indicated BufferSize (see
section [\[sec:FactorGraphStream.BufferSize\]](#sec:FactorGraphStream.BufferSize){reference-type="ref"
reference="sec:FactorGraphStream.BufferSize"}).

Read-only. Returns a variable array containing all of the currently
instantiated variables in the stream.

Read-only. Returns the domain in a form that depends on the variable
type, as summarized in the following table:

  Stream Type       Domain Data Type
  ----------------- -------------------------------------------------------------------------------------------------------------------------------------
  DiscreteStream    DiscreteDomain (see section [\[sec:DiscreteDomain\]](#sec:DiscreteDomain){reference-type="ref" reference="sec:DiscreteDomain"})
  BitStream         DiscreteDomain (see section [\[sec:DiscreteDomain\]](#sec:DiscreteDomain){reference-type="ref" reference="sec:DiscreteDomain"})
  RealStream        RealDomain (see section [\[sec:RealDomain\]](#sec:RealDomain){reference-type="ref" reference="sec:RealDomain"})
  RealJointStream   RealJointDomain (see section [\[sec:RealJointDomain\]](#sec:RealJointDomain){reference-type="ref" reference="sec:RealJointDomain"})
  ComplexStream     ComplexDomain (see section [\[sec:ComplexDomain\]](#sec:ComplexDomain){reference-type="ref" reference="sec:ComplexDomain"})

[]{#sec:VariableStream.getSlice label="sec:VariableStream.getSlice"}

    varStream.getSlice(startIndex);

The getSlice method is used to extract a *slice* of the stream, which
means a version of the stream that may be offset from the original
stream itself. This is generally used for specifying streams to connect
to a factor when calling addFactor.

Takes a single numeric argument, startIndex, which indicates the
starting position in the stream. The resulting stream slice is
essentially a reference to the stream offset by startIndex-1. For
example, a startIndex of 2 returns a slice offset by 1, such that the
first location in the slice corresponds to the second location in the
original stream. A startIndex of 1 returns a slice identical to the
original stream.

Takes a single numeric argument, startIndex, which indicates the
starting position in the stream. The resulting stream slice is
essentially a reference to the stream offset by startIndex. For example,
a startIndex of 1 returns a slice offset by 1, such that the first
location in the slice corresponds to the second location in the original
stream. A startIndex of 0 returns a slice identical to the original
stream.

### DiscreteStream

[]{#DiscreteStream.Constructor label="DiscreteStream.Constructor"}

The DiscreteStream constructor is used to create a stream of Discrete
variables or arrays of Discrete variables.

    DiscreteStream(domain, [dimensions]);

-   domain is a required argument indicating the domain of the variable.
    The domain may either be a numeric array of domain elements, a cell
    array of domain elements, or a DiscreteDomain object (see
    section [\[Discrete.Domain\]](#Discrete.Domain){reference-type="ref"
    reference="Discrete.Domain"}).

-   dimensions is an optional variable-length comma-separated list of
    matrix dimensions (an empty list indicates a single Discrete
    variable).

<!-- -->

    DiscreteStream(domain);

-   domain is a required argument indicating the domain of the variable.
    The domain may either be a comma separated list, an object array, or
    a DiscreteDomain object (see
    section [\[Discrete.Domain\]](#Discrete.Domain){reference-type="ref"
    reference="Discrete.Domain"}).

### BitStream

The BitStream constructor is used to create a stream of Bit variables or
arrays of Bit variables.

    BitStream([dimensions]);

-   dimensions is an optional variable-length comma-separated list of
    matrix dimensions (an empty list indicates a single Bit variable
    stream).

<!-- -->

    BitStream();

### RealStream

The RealStream constructor is used to create a stream of Real variables
or arrays of Real variables.

    RealStream([domain], [dimensions]);

    RealStream();
    RealStream(RealDomain domain)
    RealStream(double lowerBound, double upperBound)

All arguments are optional and can be used in any combination.

-   domain specifies a bound on the domain of the stream. It can either
    be specified as a two-element array or a RealDomain object (see
    section [\[sec:RealDomain\]](#sec:RealDomain){reference-type="ref"
    reference="sec:RealDomain"}). If specified as an array, the first
    element is the lower bound and the second element is the upper
    bound. -Inf and Inf are allowed values for the lower or upper bound,
    respectively. If no domain is specified, then a domain from
    $-\infty$ to $\infty$ is assumed.

-   dimensions specify the array dimensions. The behavior of the list of
    dimensions is identical to that for Discrete variables as described
    in
    section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
    reference="sec:VariableMatrixDimensions"}.

<!-- -->

-   domain specifies a bound on the domain of the variable. It can
    either be specified as two elements or a RealDomain object (see
    section [\[sec:RealDomain\]](#sec:RealDomain){reference-type="ref"
    reference="sec:RealDomain"}). If specified as two values, the first
    element is the lower bound and the second element is the upper
    bound. Negative infinity and positive infinity are allowed values
    for the lower or upper bound, respectively. If no domain is
    specified, then a domain from $-\infty$ to $\infty$ is assumed.

### RealJointStream

The RealJointStream constructor is used to create a stream of RealJoint
variablesor arrays of RealJoint variables.

    RealJointStream(domain, [dimensions])
    RealJointStream(numJointVariables, [dimensions]);

The arguments are defined as follows:

-   domain specifies a bound on the domain of the variable. It is
    specified by a RealJointDomain object (see
    section [\[sec:RealJointDomain\]](#sec:RealJointDomain){reference-type="ref"
    reference="sec:RealJointDomain"}). If no domain is specified, then
    an unbounded domain is assumed and numJointVariables must be
    specified instead.

-   numJointVariables specifies the number of joint real-valued
    elements. This argument is present only if the domain argument is
    not specified.

-   dimensions specify the array dimensions (the array of individual
    RealJointStream variables). The behavior of the list of dimensions
    is identical to that for Discrete variables as described in
    section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
    reference="sec:VariableMatrixDimensions"}.

<!-- -->

    RealJointStream(int numJointVariables)
    RealJointStream(RealJointDomain domain)

The arguments are defined as follows:

-   numJointVariables specifies the number of joint real-valued
    elements.

-   domain specifies the domain of the RealJoint variable using a
    RealJointDomain object (see
    [\[sec:RealJointDomain\]](#sec:RealJointDomain){reference-type="ref"
    reference="sec:RealJointDomain"}). Using this version of the
    constructor allows bounds to be specified in some or all dimensions
    of the domain.

### ComplexStream

The ComplexStream constructor is used to create a stream of Complex
variablesor arrays of Complex variables .

    ComplexStream([domain], [dimensions]);

The arguments are defined as follows:

-   domain specifies the domain of the ComplexStream using a
    ComplexDomain object (see
    [\[sec:ComplexDomain\]](#sec:ComplexDomain){reference-type="ref"
    reference="sec:ComplexDomain"}). If no domain is specified, then an
    unbounded domain is assumed.

-   dimensions specify the array dimensions (the array of individual
    Complex variables). The behavior of the list of dimensions is
    identical to that for Discrete variables as described in
    section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
    reference="sec:VariableMatrixDimensions"}.

<!-- -->

    ComplexStream()
    ComplexStream(ComplexDomain domain)

The arguments are defined as follows:

-   domain specifies the domain of the ComplexStream using a
    ComplexDomain object (see
    [\[sec:ComplexDomain\]](#sec:ComplexDomain){reference-type="ref"
    reference="sec:ComplexDomain"}). Using this version of the
    constructor allows bounds to be specified in some or all dimensions
    of the domain.

### FactorGraphStream

A FactorGraphStream is constructed automatically and returned as the
result of adding a factor to a graph using the addFactor method where
one or more of the arguments are variable streams.

[]{#sec:FactorGraphStream.BufferSize
label="sec:FactorGraphStream.BufferSize"}

Read-write. When written, modifies the number of instantiated elements
in the FactorGraphStream to include the specified number of copies of
the corresponding factor and connected variables. By default, the
BufferSize is 1. When running the solver on one step of the overall
factor graph, the solver uses the entire buffer. Making the buffer size
larger means using more of the history in performing inference for each
step. The results of inference run on previous steps that is beyond the
size of the buffer is essentially frozen, and is no longer updated on
subsequent steps of the solver.

### Data Source Common Properties and Methods {#sec:DataSource}

Dimple supports several types of streaming data sources. A data source
is a source of Input values to the variables within a variable
stream[^1]. When performing inference, as each step that the graph is
advanced, the next source value is read from the data source, and the
earlier values are shifted back to earlier time-steps in the graph.

Each source type corresponds to a particular format of input data. Each
type is appropriate only to a specific type of variable stream and
solver. The following table summarizes these requirements.

  Data Source                Variable Stream Type        Supported Solvers
  -------------------------- --------------------------- -------------------------------
  DoubleArrayDataSource      DiscreteStream, BitStream   all
                             RealStream                  SumProduct
  MultivariateDataSource     RealJointStream             SumProduct
  FactorFunctionDataSource   RealStream                  SumProduct, Gibbs, ParticleBP

Each of these share common properties and method listed in the following
sections.

Read-only. Indicates the dimensions of the data source. The dimensions
correspond to the size of the variable array at each position in the
stream that the data source will feed.

### DoubleArrayDataSource

    DoubleArrayDataSource([dimensions], [initialData]);

-   dimensions is an optional row vector indicating the matrix
    dimensions of the data source (an empty argument indicates a single
    data source to connect to a single variable stream).

-   initialData is an optional array of data the stream will contain
    (more data can be added later). This is a multidimensional array
    where the first dimensions correspond to the dimension of the
    variable stream this will feed, the next dimensions corresponds to
    the length of the Input vector for each variable (the domain size
    for discrete variable streams, and 2 for real variable streams used
    with the SumProduct solver), and the final dimension is the number
    of time-steps of data to provide. For single variable streams, the
    first dimensions are omitted.

<!-- -->

-   DoubleArrayDataSource() - Create data source with no initial data

-   DoubleArrayDataSource(ArrayList \<double\[\] \>initialData) -
    Specify some initial data.

-   DoubleArrayDataSource(double \[\]\[\] initialData) - Specify some
    initial data. The first dimension indexes the step and the second
    dimension indexes the single step distribution.

<!-- -->

    dataSource.add(data);

This method appends the data source with the specified data. The data
argument is a multidimensional array, where the first dimensions
correspond to the dimension of the variable stream this will feed, the
next dimensions corresponds to the length of the Input vector for each
variable (the domain size for discrete variable streams, and 2 for real
variable streams used with the SumProduct solver), and the final
dimension is the number of time-steps of data to provide. For single
variable streams, the first dimensions are omitted.

### MultivariateDataSource

    MultivariateDataSource([dimensions]);

-   dimensions is an optional row vector indicating the matrix
    dimensions of the data source (an empty argument indicates a single
    data source to connect to a single variable stream).

<!-- -->

    MultivariateDataSource();

    dataSource.add(means, covariances);

This method appends the data source with the a single time-step of data
(multiple time-steps must be added using successive calls to this
method). Means should have dimensions \[VariableDimensions
NumberJointVariables\] and Covariances should be of the form
\[VariableDimensions NumberJointVariables NumberJointVariables\].

-   dataSource.add(MultivariateNormalParameters msg) - Add data for a
    single step.

-   dataSource.add(MultivariateNormalParameters\[\] msgs) - Add multiple
    steps of data.

-   dataSource.add(double \[\] means, double \[\]\[\] covariances) - Add
    data for a single step.

### FactorFunctionDataSource

    FactorFunctionDataSource([dimensions]);

-   dimensions is an optional row vector indicating the matrix
    dimensions of the data source (an empty argument indicates a single
    data source to connect to a single variable stream).

<!-- -->

    FactorFunctionDataSource();

    dataSource.add(data);

This method appends the data source with the a single time-step of data
(multiple time-steps must be added using successive calls to this
method).The data argument is a multidimensional cell array, with
dimension equal to the corresponding dimensions of the variable stream
this will feed. Each element is a FactorFunctions (see
section [\[sec:FactorFunction\]](#sec:FactorFunction){reference-type="ref"
reference="sec:FactorFunction"}), which is to represent the Input of the
corresponding variable.

### Data Sink Common Properties and Methods {#sec:DataSink}

Dimple supports several types of streaming data sinks:

Dimple supports several types of streaming data sinks. A data sink is a
data structure used to store successive results of inference from the
variables with a variable stream. Specifically, it stores the Belief
values of these variables[^2]. When performing inference, as each step
that the graph is advanced, the Belief value for the earliest element of
the variable stream is stored in the data sink.

Each sink type corresponds to a particular format of output data. Each
type is appropriate only to a specific type of variable stream and
solver. The following table summarizes these requirements.

  Data Sink              Variable Stream Type        Supported Solvers
  ---------------------- --------------------------- -------------------
  DoubleArrayDataSink    DiscreteStream, BitStream   all
                         RealStream                  SumProduct
  MultivariateDataSink   RealJointStream             SumProduct

Each of these share common properties and method listed in the following
sections.

Read-only. Indicates the dimensions of the data sink. The dimensions
correspond to the size of the variable array at each position in the
stream that the data sink will be fed from.

    hasNext = dataSink.hasNext();

Used in connection with the getNext method (described in the sections
below), this method takes no arguments and returns a boolean indicating
whether or not there are any more time steps in the dataSink that have
not yet been extracted.

### DoubleArrayDataSink

    DoubleArrayDataSink([dimensions]);

-   dimensions is an optional array indicating the matrix dimensions of
    the data sink (an empty argument indicates a single data sink to
    connect to a single variable stream).

<!-- -->

    DoubleArrayDataSink();

Read-only. Extracts the entire contents of the data sink as an array.
The first dimensions of the array correspond to the Dimensions of the
data sink , the next dimension corresponds to the size of the belief
array for each variable,and the final dimension corresponds to the
number of time steps that had been gathered. For discrete variables, the
dimension of the belief array corresponds to the domain sizes, while for
real variables used with the SumProduct solver the dimension is 2, where
the elements correspond to the mean and standard deviation,
respectively.

    b = dataSink.getNext();

This method takes no arguments, and returns the set of belief values
from the next time-step. The returned value is a multidimensional array,
where the first dimensions correspond to the Dimension of the data sink,
and the next dimension corresponds to the size of the belief array for
each variable. For discrete variables, the dimension of the belief array
corresponds to the domain sizes, while for real variables used with the
SumProduct solver the dimension is 2, where the elements correspond to
the mean and standard deviation, respectively.

    double [] b = dataSink.getNext();

This method takes no arguments, and returns the belief values from the
next time-step. The returned value is an array of beliefs for the
variable for the given step. For discrete variables, the dimension of
the belief array corresponds to the domain sizes, while for real
variables used with the SumProduct solver the dimension is 2, where the
elements correspond to the mean and standard deviation, respectively.

### MultivariateDataSink

    MultivariateDataSink([dimensions]);

-   dimensions is an optional array indicating the matrix dimensions of
    the data sink (an empty argument indicates a single data sink to
    connect to a single variable stream).

<!-- -->

    MultivariateDataSink();

    b = dataSink.getNext();

This method takes no arguments, and returns the set of belief values
from the next time-step. The returned value is a cell array of
MultivariateNormalParameters objects (see
section [\[sec:MultivariateNormalParameters\]](#sec:MultivariateNormalParameters){reference-type="ref"
reference="sec:MultivariateNormalParameters"}), each of which contains
and mean vector and covariance matrix. The dimensions of this cell array
correspond to the Dimension of the data sink.

    b = dataSink.getNext();

This method takes no arguments, and returns the belief values from the
next time-step. The returned value is a MultivariateNormalParameters
object (see
section [\[sec:MultivariateNormalParameters\]](#sec:MultivariateNormalParameters){reference-type="ref"
reference="sec:MultivariateNormalParameters"}) that contains the mean
vector and covariance matrix.

[^1]: In the current version of Dimple, data sources are limited to
    providing Inputs to variables. A future version of Dimple may expand
    this capability to allow sourcing FixedValues or other types of
    input data.

[^2]: In the current version of Dimple, data sinks are limited to the
    Beliefs to variables. A future version of Dimple may expand this
    capability to allow sinking other types of result data.
