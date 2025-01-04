## Variables and Related Classes

### Variable Types

The following variable types are defined in Dimple. Some variable types
are supported by only a subset of solvers. The following table lists the
Dimple variable types and the solvers that support them.

  Variable Type        | Supported Solvers            |
  ---------------------|-------------------------------
  Discrete             |  all
  Bit                  |  all
  Real                 | SumProduct, Gibbs, ParticleBP
  RealJoint            | SumProduct, Gibbs
  Complex              | SumProduct, Gibbs
  FiniteFieldVariable  | all[^1]

### Common Properties and Methods

The following properties and methods are common to variables of all
types.

Read-write. When read, retrieves the current name of the variable or
array of variables. When set, modifies the name of the variable to the
corresponding value. The value set must be a string.

    var.Name = 'string';

When setting the Name, only one variable in an array may be set at a
time. To set the names of an entire array of variables to distinct
values, the setNames method may be used (see
section [\[sec:Variable.setNames\]](#sec:Variable.setNames){reference-type="ref"
reference="sec:Variable.setNames"}).

    var.setName('string');

Read-write. All variables and factors in a Factor Graph must have unique
names. However, sometimes it is desirable to have variables or factors
share similar strings when being plotted or printed. Users can set the
Label property to set the name for display. If the Label is not set, the
Name will be used for display. Once the label is set, the label will be
used for display.

=== "Python"
    ```python
    var.label = 'string'
    ```

=== "Java"
    ```java
    var.setLabel('string');
    ```

=== "MATLAB"
    ```matlab
    var.Label = 'string';
    ```

Read-only. Returns the domain in a form that depends on the variable
type, as summarized in the following table:

  Variable Type        | Domain Data Type |
  --------------------- --------------------------------------------------------------------------------------------------------------------------
  Discrete              | DiscreteDomain (see section [0.1.9](#sec:DiscreteDomain){reference-type="ref" reference="sec:DiscreteDomain"})
  Bit                   | DiscreteDomain (see section [0.1.9](#sec:DiscreteDomain){reference-type="ref" reference="sec:DiscreteDomain"})
  Real                  | RealDomain (see section [0.1.10](#sec:RealDomain){reference-type="ref" reference="sec:RealDomain"})
  RealJoint             | RealJointDomain (see section [0.1.11](#sec:RealJointDomain){reference-type="ref" reference="sec:RealJointDomain"})
  Complex               | ComplexDomain (see section [0.1.12](#sec:ComplexDomain){reference-type="ref" reference="sec:ComplexDomain"})
  FiniteFieldVariable   | FiniteFieldDomain (see section [0.1.13](#sec:FiniteFieldDomain){reference-type="ref" reference="sec:FiniteFieldDomain"})

Read-only. Returns the solver-object associated with the variable, to
which solver-specific methods can be called. See
section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"}, which describes the solvers, including the
solver-specific methods for each solver.

[]{#sec:Variable.Guess label="sec:Variable.Guess"}

Read-write. Specifies a value from the variable to be used when
computing the Score of the factor graph (or of the variable or
neighboring factors). The Guess must be a valid value from the domain of
the variable.

If the Guess had not yet been set, its value defaults to the most likely
belief (which corresponds to the Value property of the variable)[^2].

[]{#sec:Variable.Score label="sec:Variable.Score"}

Read-only. When read, computes and returns the score (energy) of the
Input to this variable, which is treated as a single-edge factors, given
a specified value for the variable. The score value is relative, and may
be arbitrarily normalized by an additive constant.

The value of the variable used when computing the Score is the Guess
value for this variable (see
section [\[sec:Variable.Guess\]](#sec:Variable.Guess){reference-type="ref"
reference="sec:Variable.Guess"}). If no Guess had yet been specified,
the value with the most likely belief (which corresponds to the Value
property of the variable) is used[^3].

[]{#sec:Variable.InternalEnergy label="sec:Variable.InternalEnergy"}

Read-only. (Only applies to the Sum Product Solver). When read, returns:

$$InternalEnergy(i) = \sum_{d \in D}B_i(d)*(-log(Input(d)))$$

Read-only. When read returns:

Where D is variable i's domain, Input is the variable's input, and $B_i$
is the variable Belief.

    double ie = v.getInternalEnergy();

[]{#sec:Variable.BetheEntropy label="sec:Variable.BetheEntropy"}

Read-only. (Only applies to the Sum Product Solver). When read, returns:

$$BetheEntropy(i) = - \sum{d \in D}B_i(d)*log(B_i(d))$$

Where D is variable i's domain and $B_i$ is the variable Belief.

    double be = v.getBetheEntropy();

Read-only. Retrieves a cell array an array containing a list of Ports
connecting the variable to its neighboring factors.

[]{#sec:Variable.setNames label="sec:Variable.setNames"}

For an array of variables, the setNames method sets the name of each
variable in the array to a distinct value derived from the supplied
string argument. When called with:

    varArray.setName('baseName');

the resulting variable names are of the form: `baseNamevv0`,
`baseNamevv1`, `baseNamevv2`, etc., where each variable's name is the
concatenation of the base name with the suffix `vv` followed by a unique
number for each variable in the array.

    variableArray.invokeSolverSpecificMethod('methodName', arguments);

For an array of variables, the invokeSolverSpecificMethod calls the
specified solver-specific method on each of the variables in the array.
Here, 'methodName' is a text string with the name of the solver-specific
method, and arguments is an optional comma-separated list of arguments
to that method. This method does not result in any return values. For
solver specific methods that return results, use the
invokeSolverSpecificMethodWithReturnValue method instead.

    returnArray = variableArray.invokeSolverSpecificMethodWithReturnValue('methodName', arguments);

For an array of variables, the invokeSolverSpecificMethodWithReturnValue
calls the specified solver-specific method on each of the variables in
the array, returning a return value for each variable. Here,
'methodName' is a text string with the name of the solver-specific
method, and arguments is an optional comma-separated list of arguments
to that method. The returnArray is a cell-array of the return values of
the method, with dimensions equal to the dimensions of the variable
array.

Dimple supports a set of overloaded MATLAB operators and functions that
operate on variables to implicitly add factors to a factor graph.

The list of supported operators and functions is given in
section [\[sec:overloaded\]](#sec:overloaded){reference-type="ref"
reference="sec:overloaded"}. A description of how to use these operators
and functions is given in
section [\[sec:ImplicitFactorCreation\]](#sec:ImplicitFactorCreation){reference-type="ref"
reference="sec:ImplicitFactorCreation"}.

Using one of the defined operators or functions with one or more
variables will result in creation of a new variable, which can be
assigned to a new variable name, with the appropriate domain. It will
also result in the creation of a factor which is added to the most
recently created factor graph. This factor will connect to the input
variables and the newly created result variable. For example:

    c = a + b;

This results in the creation of a Sum factor with connected variables,
c, a, and b (in that order).

These operators and functions can be compounded into a single line of
code, resulting in creation of intermediate anonymous variables. For
example:

    z = (a + b) * c^d - sqrt(-e);

Like using the addFactor method, some of the inputs to these operators
and functions may be constants. Specifically, for binary operators, one
of the inputs may be a constant instead of a variable. For example:

    x = a^2;
    y = (a + b + 2) * 3;
    z = a * (2 + 1i);

These operators and functions can be applied to arrays of variables,
with some limitations. Specifically, if each of the input variables are
vectors of the same dimension, then the result will be to create a
vector of output variables of the same dimension, along with a vector of
factors relating the inputs and outputs.

In some cases, to be consistent with MATLAB notation, there is a
distinction made between the vectorized and non-vectorized operator.
Specifically, Dimple uses MATLAB's notation for pointwise product and
power operators to indicate a vectorized operation. For example, if
variables a through e are vectors of variables of identical size, then
the following would create a variable vector z, and a series of factors
relating these variables.

    z = (a .* b) + c.^d - sqrt(-e);

For binary operators, one of the inputs may be a scalar variable or a
scalar constant instead of a variable vector. For a scalar variable, the
result is that scalar variable connecting to each instance of the
factors that are created. For a constant, each instance of the factor
uses the same constant for that input (vectors of distinct constants are
not currently supported).

Dimple overloads the MATLAB repmat function when called with a Dimple
variable or variable array as its first argument. The result of this
function is an array of variables in the requested dimensions. Dimple
does not actually make multiple copies of the variables, but instead
creates a variable array that provides repeated references to the same
variables in the existing array.

For example:

    var = Bit(10, 1);
    varRef = repmat(var, 1, 10);

In this case varRef is a 10x10 array of variables. Each element of
varRef, however, is not a distinct Dimple variable, but a reference to
an element in var, where for each row of varRef, there are 10 repeated
copies of the corresponding variable in var. Use of repmat for variables
can be useful when using addFactorVectorized (see
sections [\[sec:vectorizedFactorCreation\]](#sec:vectorizedFactorCreation){reference-type="ref"
reference="sec:vectorizedFactorCreation"}
and [\[sec:FactorGraph.addFactorVectorized\]](#sec:FactorGraph.addFactorVectorized){reference-type="ref"
reference="sec:FactorGraph.addFactorVectorized"}).

### Discrete {#sec:Discrete}

A Discrete variable represents a variable that can take on a finite set
of distinct states. The Discrete class corresponds to either a single
Discrete variable or a multidimensional array of Discrete variables. All
properties/methods can either be called for all elements in the
collection or for individual elements of the collection.

The Discrete constructor can be used to create an N-dimensional
collection of Dimple Discrete variables. The constructor is called with
the following arguments (arguments in brackets are optional).

    Discrete(domain, [dimensions])

    Discrete(domain)

-   domain is a required argument indicating the domain of the variable.
    The domain may either be a numeric array of domain elements, a cell
    array of domain elements, or a DiscreteDomain object (see
    section [\[Discrete.Domain\]](#Discrete.Domain){reference-type="ref"
    reference="Discrete.Domain"}).

-   dimensions is an optional variable-length comma-separated list of
    matrix dimensions (an empty list indicates a single Discrete
    variable).

<!-- -->

-   domain is a required argument indicating the domain of the variable.
    The domain may either be an object array of domain elements, a comma
    separated list, or a DiscreteDomain object (see
    section [\[Discrete.Domain\]](#Discrete.Domain){reference-type="ref"
    reference="Discrete.Domain"}).

For example:

=== "Java"

    ```java
    Object [] domain = new Object [] {0,1,2};
    Discrete w = new Discrete(domain);
    Discrete x = new Discrete(0,1,2);
    DiscreteDomain dd = DiscreteDomain.create(0,1,2);
    Discrete y = new Discrete(dd);
    ```
=== "MATLAB"
    ```matlab
    domain = [0 1 2];
    w = Discrete(domain);
    x = Discrete(domain, 4);
    y = Discrete(domain, 2, 3);
    z = Discrete(domain, 2, 3, 4);
    ```


We examine each of these arguments in more detail in the following
sections.

[]{#Discrete.Domain label="Discrete.Domain"}

Every Discrete random variable has a domain associated with it. A domain
is a set. Elements of the set may be any object type. For example, the
following are Discrete variables with valid domains:

    a = Discrete({1, 2, 3});
    b = Discrete({1+i, i, 2*i});
    c = Discrete({[1 0; 0 1], [i 1, 2*i 1]});
    d = Discrete({[1 0; 0 1], 2, i+1});
    e = Discrete({1.2, 3, pi/2});
    f = Discrete({'red', 'green', 'blue'});

    Discrete a = new Discrete(1, 2, 3);
    Discrete b = new Discrete(new double [] {1,0,0,1},1); 
    Discrete c = new Discrete("red","blue",2);

\(a\) creates a variable whose domain consists of three values: 1, 2,
and 3. (b) creates a variable whose domain consists of three complex
numbers. (c) creates a variable whose domain consists of two elements,
each of which is a 2x2 complex matrix. (d) creates a variable whose
domain consists of three elements: a matrix, real scalar, and complex
scalar. (e) creates a variable whose domain consists of both
floating-point and integer values. (f) creates a variable whose domain
is a set of strings.

In the previous example we used cell arrays to specify the elements of a
domain. When the domain consists only of numeric values (integer or
floating-point), domains can instead be specified as a numeric array. In
this case, each element of the array (regardless of the array's
dimensions) is considered a distinct entry in the domain.

    a = Discrete(0:2);
    b = Discrete([1 2 3; 4 5 6]);
    c = Discrete([0:2]');

\(a\) creates a variable with a domain of 0, 1, and 2. (b) creates a
variable with a domain of 1, 2, 3, 4, 5, 6. (c) creates a variable with
domain of 0, 1, and 2.

The domain may also be specified using a DiscreteDomain object. In that
case, the domain of the variable consists of the elements of this
object. For example:

    myDomain = DiscreteDomain(0:10);
    a = Discrete(myDomain);

    DiscreteDomain myDomain = DiscreteDomain.create(0,1,2);
    Discrete a = new Discrete(myDomain);

See section [0.1.9](#sec:DiscreteDomain){reference-type="ref"
reference="sec:DiscreteDomain"} for more information about the
DiscreteDomain class.

[]{#sec:VariableMatrixDimensions label="sec:VariableMatrixDimensions"}

If the variable constructor is called without any dimensions, a single
variable will be created.

If one dimension n is specified, a square array of dimensions n x n
variables will be created[^4].

With k dimensions specified, n1, n2, \..., nk, a multidimensional
variable array of dimensions n1 x n2 x \... x nk will be created.

[]{#sec:Discrete.Belief label="sec:Discrete.Belief"}

Read-only. For any single variable, the Belief method returns a vector
whose length is the total number of elements of the domain of the
variable. When called after running a solver to perform inference on the
graph, each element of the vector contains the estimated marginal
probability of the corresponding element of the domain of the variable.
The results are undefined if called prior to running a solver.

For an array of variables, the Belief method will return an array of
vectors (that is, an array one dimension larger than the variable array)
containing the beliefs of each variable in the array.

[]{#sec:Discrete.Value label="sec:Discrete.Value"}

Read-only. In some cases, one may wish to retrieve the single most
likely element of a variable's domain. The Value property does just
that.

For any single variable, the Value method returns a single value chosen
from the domain of the variable. When called after running a solver to
perform inference on the graph, the value returned corresponds to the
element in the variable's domain that has the largest estimated marginal
probability[^5]. The results are undefined if called prior to running a
solver.

For an array of variables, the Value method will return an array of
values, each from the domain of the corresponding variable representing
the largest estimated marginal probability for that variable.

[]{#sec:Discrete.Input label="sec:Discrete.Input"}

Read-write. For any variable, the Input method can be used to set and
return the current input of that variable. An input behaves much like a
single edge factor connected to the variable, and is typically used the
represent the likelihood function associated with a measured value (see
section [\[sec:LikelihoodInput\]](#sec:LikelihoodInput){reference-type="ref"
reference="sec:LikelihoodInput"}).

When read, for a single variable returns an array of values with each
value representing the current input setting for the corresponding
element of the variable's domain. The length of this array is equal to
the total number of elements of the domain. When read, for an array of
variables, the result is an array with dimension one larger than the
dimension of the variable array. The additional dimension represents the
current set of input values for the corresponding variable in the array.

When written, for a single variable, the value must be an array of
length equal to the domain of the variable. The values in the array must
all be non-negative, and non-infinite, but are otherwise arbitrary. When
written, for an array of variables, the values must be a
multidimensional array where the first set of dimensions exactly match
the dimensions of the array (or the portion of the array) being set, and
length of the last dimension is the number of elements in the variable's
domain.

[]{#sec:Discrete.FixedValue label="sec:Discrete.FixedValue"}

Read-write. For any variable, the FixedValue property can be used to set
the variable to a specific fixed value, and to retrieve the fixed-value
if one has been set. This would generally be used for conditioning a
graph on known data without modifying the graph (see
section [\[sec:FixingAVariableValue\]](#sec:FixingAVariableValue){reference-type="ref"
reference="sec:FixingAVariableValue"}).

Reading this property results in an error if no fixed value has been
set. To determine if a fixed value has been set, use the hasFixedValue
method (see
section [\[sec:Discrete.hasFixedValue\]](#sec:Discrete.hasFixedValue){reference-type="ref"
reference="sec:Discrete.hasFixedValue"}).

When setting this property on a single variable, the value must be a
value included in the domain of the variable. The fixed value must be a
value chosen from the domain of the variable. For example:

    a = Discrete(1:10);
    a.FixedValue = 3;

    Discrete a = new Discrete(1,2,3);
    a.setFixedValue(3);

When setting this property on a variable array, the value must be an
array of the same dimensions as the variable array, and each entry in
the array must be an element of the domain.

Because the Input and FixedValue properties serve similar purposes,
setting one of these overrides any previous use of the other. Setting
the Input property removes any fixed value and setting the FixedValue
property replaces the input with a delta function---the value 0 except
in the position corresponding to the fixed value that had been set.

[]{#sec:Discrete.hasFixedValue label="sec:Discrete.hasFixedValue"}

This method takes no arguments. When called for a single variable, it
returns a boolean indicating whether or not a fixed-value is currently
set for this variable. When called for a variable array, it returns a
boolean array of dimensions equal to the size of the variable array,
where each entry indicates whether a fixed value is set for the
corresponding variable.

### Bit

A Bit is a special kind of Discrete with domain \[0 1\].

The Bit constructor can be used to create an N-dimensional collection of
Dimple Discrete variables. Its constructor does not require a domain,
since the domain is predetermined. The constructor takes only a
variable-length list of matrix dimensions, where an empty list indicates
a single Bit variable.

    Bit([dimensions])

The behavior of the list of dimensions is identical to that for Discrete
variables as described in
section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
reference="sec:VariableMatrixDimensions"}.

    Bit()

Read-only. For a single Bit variable, the Belief property is a single
number that represents the estimated marginal probability of the value
one.

For an array of Bit variables, the Belief property is an array of
numbers with size equal to the size of the variable array, with each
value representing the estimated marginal probability of one for the
corresponding variable.

See
section [\[sec:Discrete.Value\]](#sec:Discrete.Value){reference-type="ref"
reference="sec:Discrete.Value"}.

Read-write. For setting the Input property on a single Bit variable, the
value must be a single number in the range 0 to 1, which represents the
normalized likelihood of the value one (see
section [\[sec:LikelihoodInput\]](#sec:LikelihoodInput){reference-type="ref"
reference="sec:LikelihoodInput"}). If $L(x)$ is the likelihood of the
variable, the Input should be set to $\frac{L(x=1)}{L(x=0) + L(x=1)}$.

For setting the Input property on an array of Bit variables, the value
must be an array of normalized likelihood values, where the array
dimensions must match the dimensions of the array (or the portion of the
array) being set.

See
section [\[sec:Discrete.FixedValue\]](#sec:Discrete.FixedValue){reference-type="ref"
reference="sec:Discrete.FixedValue"}.

See
section [\[sec:Discrete.hasFixedValue\]](#sec:Discrete.hasFixedValue){reference-type="ref"
reference="sec:Discrete.hasFixedValue"}.

### Real

A Real variable represents a variable that takes values on the real
line, or on a contiguous subset of the real line. The Real class
corresponds to either a single Real variable or a multidimensional array
of Real variables. All properties/methods can either be called for all
elements in the collection or for individual elements of the collection.

    Real([domain], [dimensions])

    Real()
    Real(RealDomain domain)
    Real(double lowerBound, double upperBound)

All arguments are optional and can be used in any combination.

-   domain specifies a bound on the domain of the variable. It can
    either be specified as a two-element array or a RealDomain object
    (see section [0.1.10](#sec:RealDomain){reference-type="ref"
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
    section [0.1.10](#sec:RealDomain){reference-type="ref"
    reference="sec:RealDomain"}). If specified as two values, the first
    element is the lower bound and the second element is the upper
    bound. Negative infinity and positive infinity are allowed values
    for the lower or upper bound, respectively. If no domain is
    specified, then a domain from $-\infty$ to $\infty$ is assumed.

Examples:

-   Real() specifies a scalar real variable with an unbounded domain.

-   Real(4,1) specifies a 4x1 vector of real variables with unbounded
    domain.

-   Real(\[-1 1\]) specifies a scalar real variable with domain from -1
    to 1.

-   Real(\[-Inf 0\], 4, 10, 2) specifies a 4x10x2 array of real
    variables, each with the domain from negative infinity to zero.

-   Real(RealDomain(-pi, pi)) specifies a scalar real variable with
    domain from $-\pi$ to $\pi$.

<!-- -->

-   Real() specifies a scalar real variable with an unbounded domain.

-   Real(-1, 1) specifies a scalar real variable with domain from -1 to
    1.

-   Real(Double.NEGATIVE_INFINITY,0) specifies a variable with the
    domain from negative infinity to zero.

-   Real(RealDomain.create(-Math.PI, Math.PI)) specifies a scalar real
    variable with domain from $-\pi$ to $\pi$.

Read-only. The behavior of this property for Real variables is solver
specific. Some solvers do not support this property at all and will
return an error when read. See
section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"} for more detail on each of the supported
solvers.

For the SumProduct solver, this property returns the estimated marginal
distribution of the variable in the form of a NormalParameters object
(see section [0.1.14](#sec:NormalParameters){reference-type="ref"
reference="sec:NormalParameters"}), which includes a mean and precision
value[^6]. For an array of Real variables, this property returns a cell
array of NormalParameters objects, each corresponding to the estimated
marginal distribution of the corresponding variable. The results are
undefined if called prior to running a solver.

Read-only. The behavior of this property for Real variables is solver
specific. Some solvers do not support this property at all and will
return an error when read. See
section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"} for more detail on each of the supported
solvers.

For the SumProduct solver, the Value corresponds to the mean value of
the belief.

Read-write. For a Real variable, the Input property is expressed in the
form of a FactorFunction object that can connect to exactly one Real
variable. The list of available built-in FactorFunctions is given in
section [\[sec:usingBuiltInFactors\]](#sec:usingBuiltInFactors){reference-type="ref"
reference="sec:usingBuiltInFactors"}. Typically, an Input would use one
of the standard distributions included in this list. In this case, it
must be one in which all the parameters can be fixed to pre-defined
constants. For the Gibbs and ParticleBP solvers, any such factor
function may be used as an Input. For the SumProduct solver, however,
only a Normal factor function may be used. Below is an example of
setting the Input for a Real variable:

    r = Real();
    r.Input = FactorFunction('Normal', measuredMean, measurementPrecision);

Equivalently, a simpler alternative form for specifying the Input may be
used. In this form the name of the factor followed by the arguments to
the corresponding constructor are listed as elements of a cell array:

    r = Real();
    r.Input = {'Normal', measuredMean, measurementPrecision};

To remove an Input that had previously been set, the Input may be set to
an empty array.

    Real r = new Real();
    r.setInput(new Normal(measuredMean, measurementPrecision));

To remove an Input that had previously been set, the Input may be set to
null.

In the current version of Dimple, Inputs on Real variable arrays must be
set one at a time, or all set to a single common value[^7].

Read-write. The behavior of the FixedValue property for a Real variable
is nearly identical to that of Discrete variables (see
section [\[sec:Discrete.FixedValue\]](#sec:Discrete.FixedValue){reference-type="ref"
reference="sec:Discrete.FixedValue"}). When setting the FixedValue of a
Real variable, the value must be within the domain of the variable, that
is greater than or equal to the lower bound and less than or equal to
the upper bound. For example:

    a = Real([-pi pi]);
    a.FixedValue = 1.7;

    Real a = new Real(-Math.PI,Math.PI);
    a.setFixedValue(1.7);

Because the Input and FixedValue properties serve similar purposes,
setting one of these overrides any previous use of the other. Setting
the Input property removes any fixed value and setting the FixedValue
property removes the input.

See
section [\[sec:Discrete.hasFixedValue\]](#sec:Discrete.hasFixedValue){reference-type="ref"
reference="sec:Discrete.hasFixedValue"}.

### RealJoint

A RealJoint variable is a tightly coupled set of real variables that are
treated by a solver as a single joint variable rather than a separate
collection of variables. For example, in the SumProduct solver, the
messages associated with RealJoint variables involve joint mean and
covariance matrix rather than an individual mean and variance for each
variable.

Like other variables, the RealJoint class can represent either a single
RealJoint variable (representing a collection of real values) or an
array of RealJoint variables.

    RealJoint(domain, [dimensions])
    RealJoint(numElements, [dimensions])

The arguments are defined as follows:

-   domain specifies a bound on the domain of the variable. It is
    specified by a RealJointDomain object (see
    section [0.1.11](#sec:RealJointDomain){reference-type="ref"
    reference="sec:RealJointDomain"}). If no domain is specified, then
    an unbounded domain is assumed and numElements must be specified
    instead.

-   numElements specifies the number of joint real-valued elements. This
    argument is present only if the domain argument is not specified.

-   dimensions specify the array dimensions (the array of individual
    RealJoint variables). The behavior of the list of dimensions is
    identical to that for Discrete variables as described in
    section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
    reference="sec:VariableMatrixDimensions"}.

<!-- -->

    RealJoint(int size)
    RealJoint(RealJointDomain domain)

The arguments are defined as follows:

-   size specifies the number of joint real-valued elements.

-   domain specifies the domain of the RealJoint variable using a
    RealJointDomain object (see
    [0.1.11](#sec:RealJointDomain){reference-type="ref"
    reference="sec:RealJointDomain"}). Using this version of the
    constructor allows bounds to be specified in some or all dimensions
    of the domain.

[]{#sec:RealJoint.Belief label="sec:RealJoint.Belief"}

Read-only. The behavior of this property for RealJoint variables is
solver specific. Some solvers do not support this property at all and
will return an error when read. See
section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"} for more detail on each of the supported
solvers.

For the SumProduct solver, this property returns the estimated marginal
distribution of the variable in the form of a
MultivariateNormalParameters object (see
section [0.1.15](#sec:MultivariateNormalParameters){reference-type="ref"
reference="sec:MultivariateNormalParameters"}), which includes a mean
vector and covariance matrix. For an array of RealJoint variables, this
property returns a cell array of MultivariateNormalParameters objects,
each corresponding to the estimated marginal distribution of the
corresponding variable. The results are undefined if called prior to
running a solver.

[]{#sec:RealJoint.Value label="sec:RealJoint.Value"}

Read-only. The behavior of this property for Real variables is solver
specific. Some solvers do not support this property at all and will
return an error when read. See
section [\[sec:SolversAPI\]](#sec:SolversAPI){reference-type="ref"
reference="sec:SolversAPI"} for more detail on each of the supported
solvers.

For the SumProduct solver, this property returns the mean vector, with
dimension equal to the dimension of the RealJoint variable.

For an array of RealJoint variables, this property returns an array
where the initial dimensions correspond to the dimensions of the
variable array (or portion of the variable array), and the final
dimension corresponds to the dimension of the RealJoint variable.

[]{#sec:RealJoint.Input label="sec:RealJoint.Input"}

Read-write. For a RealJoint variable, the Input property is expressed in
one of two forms: either a single FactorFunction object that can connect
to exactly one RealJoint variable, or a set of FactorFunction objects
that can each connect to exactly one Real variable. The latter case
corresponds to a likelihood function where each dimension is
independent. The list of available built-in FactorFunctions is given in
section [\[sec:usingBuiltInFactors\]](#sec:usingBuiltInFactors){reference-type="ref"
reference="sec:usingBuiltInFactors"}. Typically, an Input would use one
of the standard distributions included in this list. In this case, it
must be one in which all the parameters can be fixed to pre-defined
constants. For the Gibbs and ParticleBP solvers, any such factor
function may be used as an Input. For the SumProduct solver, however,
only a MultvariateNormal factor function or a set of Normal factor
functions may be used.

Below is an example of setting the Input for a RealJoint variable using
a single multivariate factor function:

    r = Real();
    r.Input = FactorFunction('MultivariateNormal', measuredMeanVector, measurementCovarianceMatrix);

Equivalently, a simpler alternative form for specifying the Input may be
used. In this form the name of the factor followed by the arguments to
the corresponding constructor are listed as elements of a cell array:

    r = Real();
    r.Input = {'MultivariateNormal', measuredMeanVector, measurementCovarianceMatrix};

To remove an Input that had previously been set, the Input may be set to
an empty array.

    Real r = new Real();
    r.setInput(new MultivariateNormal(measuredMeanVector, measurementCovarianceMatrix));

To remove an Input that had previously been set, the Input may be set to
null.

To specify a set of univariate factor functions the value of this
property must be a cell array of FactorFunction objects, one for each
dimension of the RealJoint variable. To specify a set of univariate
factor functions the value of this property must be an array of
FactorFunction objects, one for each dimension of the RealJoint
variable.

In the current version of Dimple, Inputs on RealJoint variable arrays
must be set one at a time, or all set to a single common value[^8].

[]{#sec:RealJoint.FixedValue label="sec:RealJoint.FixedValue"}

Read-write. The behavior of the FixedValue property for a RealJoint
variable is similar to that of Discrete variables (see
section [\[sec:Discrete.FixedValue\]](#sec:Discrete.FixedValue){reference-type="ref"
reference="sec:Discrete.FixedValue"}). When setting the FixedValue of a
Real variable, the value must be within the domain of the variable. When
setting a fixed value, the value must be in an array with dimension
equal to the dimension of the RealVariable. For example:

    a = RealJoint(4);
    a.FixedValue = [1.7, 2.0, 0, -1.2];

    RealJoint a = new RealJoint(4);
    a.setFixedValue(new double[]{1.7, 2.0, 0, -1.2});

For an array of RealJoint variables, the fixed values of all variables
may be set together. In this case, the initial dimensions of the input
array must equal the dimensions of the variable array (or subset of the
variable array) being set, while the final dimension must equal the
dimension of the RealJoint variable.

Because the Input and FixedValue properties serve similar purposes,
setting one of these overrides any previous use of the other. Setting
the Input property removes any fixed value and setting the FixedValue
property removes the input.

See
section [\[sec:Discrete.hasFixedValue\]](#sec:Discrete.hasFixedValue){reference-type="ref"
reference="sec:Discrete.hasFixedValue"}.

### Complex

Complex is a special kind of RealJoint variable with exactly two joint
elements.

The behavior of all properties and methods is identical to that of
RealJoint variables, with the exception of a few methods (described
below), which that refer directly to complex numerical values. The
behavior of all properties and methods is identical to that of RealJoint
variables.

    Complex([domain], [dimensions])

The arguments are defined as follows:

-   domain specifies the domain of the Complex variable using a
    ComplexDomain object (see
    [0.1.12](#sec:ComplexDomain){reference-type="ref"
    reference="sec:ComplexDomain"}). If no domain is specified, then an
    unbounded domain is assumed.

-   dimensions specify the array dimensions (the array of individual
    Complex variables). The behavior of the list of dimensions is
    identical to that for Discrete variables as described in
    section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
    reference="sec:VariableMatrixDimensions"}.

<!-- -->

    Complex()
    Complex(ComplexDomain domain)

The arguments are defined as follows:

-   domain specifies the domain of the Complex variable using a
    ComplexDomain object (see
    [0.1.12](#sec:ComplexDomain){reference-type="ref"
    reference="sec:ComplexDomain"}). Using this version of the
    constructor allows bounds to be specified in some or all dimensions
    of the domain.

Read-only. See
section [\[sec:RealJoint.Belief\]](#sec:RealJoint.Belief){reference-type="ref"
reference="sec:RealJoint.Belief"}.

Read-only. This method behaves similarly to the Value property for a
RealJoint variable, except the value returned is a single complex
number. For an array of Complex variables, each entry in the returned
array is a complex number. Read-only. See
section [\[sec:RealJoint.Value\]](#sec:RealJoint.Value){reference-type="ref"
reference="sec:RealJoint.Value"}.

Read-write. See
section [\[sec:RealJoint.Input\]](#sec:RealJoint.Input){reference-type="ref"
reference="sec:RealJoint.Input"}.

Read-write. This method behaves similarly to the FixedValue property for
a RealJoint variable, except that the fixed value is a single complex
number. For an array of Complex variables, each entry in the array
should be a complex number. For example:

a = Complex(); a.FixedValue = 5 + 1i\*2; Read-write. See
section [\[sec:RealJoint.FixedValue\]](#sec:RealJoint.FixedValue){reference-type="ref"
reference="sec:RealJoint.FixedValue"}.

See
section [\[sec:Discrete.hasFixedValue\]](#sec:Discrete.hasFixedValue){reference-type="ref"
reference="sec:Discrete.hasFixedValue"}.

### FiniteFieldVariable

Dimple supports a special variable type called a FiniteFieldVariable,
which represent finite fields with $N=2^{n}$ elements. These fields find
frequent use in error correcting codes. These variables are used along
with certain custom factors that are implemented more efficiently for
sum-product belief propagation than the alternative using discrete
variables and factors implemented directly. See
section [\[sec:finiteFields\]](#sec:finiteFields){reference-type="ref"
reference="sec:finiteFields"} for more information on how these
variables are used.

The behavior of all properties and methods is identical to that of
Discrete variables.

    FiniteFieldVariable(primitivePolynomial, [dimensions])

    FiniteFieldVariable(primitivePolynomial)

The arguments are defined as follows:

-   primitivePolynomial the primitive polynomial of the finite field.
    The format of the primitive polynomial follows the same definition
    used by MATLAB in the `gf` function. See the MATLAB help on the `gf`
    function for more detail.

-   dimensions specify the array dimensions (the array of individual
    FiniteFieldVariable variables). The behavior of the list of
    dimensions is identical to that for Discrete variables as described
    in
    section [\[sec:VariableMatrixDimensions\]](#sec:VariableMatrixDimensions){reference-type="ref"
    reference="sec:VariableMatrixDimensions"}.

<!-- -->

-   primitivePolynomial the primitive polynomial of the finite field.
    The format of the primitive polynomial follows the same definition
    used by MATLAB in the `gf` function. See the MATLAB help on the `gf`
    function for more detail.

### DiscreteDomain {#sec:DiscreteDomain}

The DiscreteDomain class represents a domain with a finite fixed set of
elements. It is the type of Domain used by Discrete variables.
DiscreteDomain objects are immutable.

    DiscreteDomain(elementList)

The elementList argument is either a cell array or array of domain
elements. Every entry of the array or cell array is considered an
element of the domain, regardless of the number of dimensions it has.
For a cell array, each object in the cell array is considered an element
of the domain regardless of the object type. For a numeric array, every
entry in the array must be numeric.

Construction of a DiscreteDomain object is done using the static create
method.

    DiscreteDomain.create(Object[] elementList)

The elementList argument is an array of domain elements. Every entry of
the array is considered an element of the domain, regardless of the
number of dimensions it has. For an array of Objects, each object in the
array is considered an element of the domain regardless of the object
type. For a numeric array, every entry in the array must be numeric.

Read-only. This property returns the set of elements in the discrete
domain in the form of a one-dimensional cell array.

### RealDomain {#sec:RealDomain}

The RealDomain class is used to refer to the domain of Real variables.

    RealDomain([lowerBound, [upperBound] ])

-   lowerBound indicates the lower bound of the domain. The value must
    be a scalar numeric value. It may be set to -Inf to indicate that
    there is no lower bound. The default value is -Inf.

-   upperBound indicates the upper bound of the domain. The value must
    be a scalar numeric value. It may be set to Inf to indicate that
    there is no upper bound. The default value is Inf.

Construction of a RealDomain object is done using the static create
method.

    RealDomain.create()
    RealDomain.create(lowerBound,upperBound)

-   lowerBound indicates the lower bound of the domain. The value must
    be a scalar numeric value. It may be set to negative infinity to
    indicate that there is no lower bound. If the create method with no
    arguments is used, the default lower bound is negative infinity.

-   upperBound indicates the upper bound of the domain. The value must
    be a scalar numeric value. It may be set to positive infinity to
    indicate that there is no upper bound. If the create method with no
    arguments is used, the default upper bound is positive infinity.

Read-only. This property returns the value of the lower bound. The
default value is negative infinity.

Read-only. This property returns the value of the upper bound. The
default value is positive infinity.

### RealJointDomain {#sec:RealJointDomain}

The RealJointDomain class is used to refer to the domain of RealJoint
variables.

    RealJointDomain(numDimensions);
    RealJointDomain(listOfRealDomains);
    RealJointDomain(numDimensions, realDomain);

-   numDimensions indicates the number of dimensions in the domain of
    the RealJoint variable. If the form of constructor that specifies
    numDimensions is called, then all dimensions are assumed to be
    unbounded.

-   listOfRealDomains is a comma-separated list or cell array of
    RealDomain objects, one for each dimension. Each RealDomain in the
    list indicates the domain for the corresponding dimension of the
    RealJoint variable.

-   realDomain specifies a single RealDomain. If the number of
    dimensions is specified along with a single RealDomain, then that
    RealDomain is used for each dimension of the domain of the RealJoint
    variable.

Construction of a RealJointDomain object is done using the static create
method.

    RealJointDomain.create(int size)
    RealJointDomain.create(RealDomain... domains)
    RealJointDomain.create(RealDomain domain, int size)

-   size indicates the number of dimensions in the domain of the
    RealJoint variable. If the form of create that specifies only the
    size is called, then all dimensions are assumed to be unbounded.

-   domains is a list or array of RealDomain objects, one for each
    dimension. Each RealDomain in the list indicates the domain of the
    corresponding dimension of the RealJoint variable. The number of
    entries determines the number of dimensions.

-   domain indicates a single RealDomain that is to be used for all
    dimensions of the RealJoint variable. In this form of create, the
    size must also be specified to indicate the number of dimensions of
    the RealJoint variable.

Read-only. Indicates the number of elements in the RealJointDomain,
which corresponds to the number of dimensions of an associated RealJoint
variable.

Read-only. Returns the collection of RealDomains that correspond to each
dimension of the RealJointDomain.

    domain.getNumVars();

Returns the number of elements in the RealJointDomain, which corresponds
to the number of dimensions of an associated RealJoint variable.

    domain.getRealDomain(dimensionIndex);

Returns the RealDomain that correspond to the dimension corresponding to
the specified `dimensionIndex`.

### ComplexDomain {#sec:ComplexDomain}

The ComplexDomain class, a subclass of the RealJointDomain class, is
used to refer to the domain of Complex variables.

    ComplexDomain();
    ComplexDomain(listOfRealDomains);
    ComplexDomain(realDomain);

-   listOfRealDomains comma-separated list of exactly two RealDomain
    objects, one for each dimension. Each RealDomain in the list
    indicates the domain for the corresponding dimension of the Complex
    variable (real followed by imaginary). If no RealDomains are
    specified, then both dimensions are assumed to be unbounded.

-   realDomain specifies a single RealDomain to apply to both dimensions
    of the domain of the Complex variable.

Construction of a ComplexDomain object is done using the static create
method.

    ComplexDomain.create()
    ComplexDomain.create(RealDomain... domains)
    ComplexDomain.create(RealDomain domain)

-   domains is an array of exactly two RealDomain objects, one for each
    dimension. Each RealDomain in the list indicates the domain of the
    corresponding dimension of the Complex variable (real followed by
    imaginary). If no domains argument is specified, then both
    dimensions are assumed to be unbounded.

-   domain is a single RealDomain object. When only one RealDomain
    object is specified, the same RealDomain is used for both
    dimensions.

Read-only. Indicates the number of elements in the ComplexDomain, which
should always equal two.

Read-only. Returns the collection of RealDomains that correspond to each
dimension of the ComplexDomain (real followed by imaginary).

    domain.getNumVars();

Returns the number of elements in the ComplexDomain, which should always
equal two.

    domain.getRealDomain(dimensionIndex);

Returns the RealDomain that correspond to the dimension corresponding to
the specified `dimensionIndex`.

### FiniteFieldDomain {#sec:FiniteFieldDomain}

The FiniteFieldDomain class represents the domain of a
FiniteFieldVariable. FiniteFieldDomain objects are immutable.

    FiniteFieldDomain(primitivePolynomial)

Construction of a FiniteFieldDomain object is done using the static
create method.

    FiniteFieldDomain.create(primitivePolynomial)

-   primitivePolynomial is an integer representation of the primitive
    polynomial of the finite field.

Read-only. This property returns the set of elements in the discrete
domain in the form of a one-dimensional cell array.

Read-only. This property returns the primitive polynomial associated
with the domain using an integer representation.

Read-only. This property returns the number of bits in the finite field.
The size of the Elements property is $2^{N}$.

### NormalParameters {#sec:NormalParameters}

The NormalParameters class is used to specify the parameters of a
univariate Normal distribution, as used in the SumProduct solver.

    NormalParameters(mean, precision)

-   mean is the mean value of the distribution.

-   precision is the precision of the distribution, which is the inverse
    of the variance.

Read-only. Returns the mean value.

Read-only. Returns the precision value.

Read-only. Returns the variance (inverse of the precision).

Read-only. Returns the standard deviation (square root of the variance).

### MultivariateNormalParameters {#sec:MultivariateNormalParameters}

The MultivariateNormalParameters class is used to specify the parameters
of a multivariate Normal distribution, as used in the SumProduct solver.

    MultivariateNormalParameters(meanVector, covarianceMatrix)

-   meanVector indicates the mean value of each element in a joint set
    of variables. The value must be a one-dimensional numeric array.

-   covarianceMatrix indicates the covariance matrix associated with the
    elements of a joint set of variables. The value must be a
    two-dimensional numeric array with each dimension identical to the
    length of the meanVector.

Read-only. Returns a vector of values, where each value indicates the
mean value of the corresponding element in a joint set of variables.

Read-only. Returns a two-dimensional array of values, representing the
covariance matrix of a joint set of variables.

Read-only. Returns a vector of values representing the information
matrix, defined as $\Sigma^{-1}\mu$, where $\Sigma$ is the covariance
matrix and $\mu$ is the mean vector.

Read-only. Returns a two-dimensional array of values representing the
information matrix, defined as $\Sigma^{-1}$, where $\Sigma$ is the
covariance matrix.

[^1]: The performance enhanced implementations of built-in factors for
    FiniteField variables are only available when using the SumProduct
    solver.

[^2]: For some solvers, beliefs are not supported for all variable
    types; in such cases there is no default value, so a Guess must be
    specified in order to compute the Score.

[^3]: For some solvers, beliefs are not supported for all variable
    types; in such cases there is no default value, so a Guess must be
    specified.

[^4]: This follows a common MATLAB convention.

[^5]: If more than one domain element has identical marginal
    probabilities that are larger than for any other value, a single
    value from the domain is returned, chosen arbitrarily among these.

[^6]: For backward compatibility with Dimple version 0.04 and earlier,
    the Belief can be treated as a two-dimensional array where the first
    element is the mean and the second is the standard deviation.

[^7]: This restriction may be removed in a future version.

[^8]: This restriction may be removed in a future version.
