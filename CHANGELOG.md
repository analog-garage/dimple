# Changelog

## Versions

## [0.8.1]

* Initial limited python API based on JPype
* Python 3.12 or later
* Now need Java 22 or later
* garpy-dimple conda package

## [0.8.0]

### Significant Changes Since 0.7

* Categorical and DiscreteTransition factor functions no longer limited to zero-based integers and can be used with arbitrary discrete variables.
* SolverOptions.maxAutomaticFactorTableSize option allows control of when factor tables are generated based on size.
* New events for monitoring Gibbs solver.
* New scheduler & scheduleValidator options in BPOptions and GibbsOptions for specifying schedulers and
  schedule validation logic.
* New API for specifying custom schedules. Custom schedules are now validated at graph initialization time
  instead of when first assigned.
* Input/FixedValue APIs have been deprecated and replaced by Priors
* DataLayers represent data settings and distributions over entire graph.
* Updated version dependencies of third party libraries:
    - Guava 14 -> 18
    - Apache commons math 3.2 -> 3.5
    - JTransforms 2.3 -> 3.0
* Removed experimental javascript API that was introduced in 0.07 and moved to an external project 'dimplejs'.

### Significant Bug Fixes Since 0.7

* Gibbs solver now supports large discrete factors.

## [0.7.0]

### Significant New Features Since 0.6
 
- DimpleApplet and Javascript API to enable use of Dimple from browser-based
Javascript applications.
- New option mechanism used for configuring solver options.
- New com.analog.lyric.options package supports type-safe hierarchical options.
- DimpleEnvironment provides some facilities that can be shared among multiple graphs, including a logging interface.
- Added built-in factors to convert between vectors of Real variables and RealJoint or Complex variables (ComplexToRealAndImaginary, RealAndImaginaryToComplex, RealJointProjection, RealJointToRealVector, RealVectorToRealJoint).
- Added built-in factor for complex absolute value (ComplexAbs), and associated use of overloaded abs() method in MATLAB API.
- Modified API for custom Java FactorFunctions, now requiring use of evalEnergy(Value[]) method instead of the other variants; and for deterministic directed FactorFunctions, evalDeterministic now takes a Value array argument instead of an Object array.
- Added methods for the Gibbs solver to determine the rejection rate when using Metropolis-Hastings and related samplers.
- Added methods for Real and RealJoint variables in the Gibbs solver to determine the sample mean and (co)variance.
- Added If factor creation utility in MATLAB API to simplify conditional assignment factor creation.
- Refactoring in Java API including:
  - VariableBase supplanted by Variable
  - Solver-specific objects now have solver specific names (e.g. GibbsReal instead of SRealVariable)

### Significant Bug Fixes Since 0.6

- Fixed problem with gradle builds when git is not available or source tree is not a git clone.

### Optimizations

- Domains and JointDomainIndexer objects in Java are now interned on creation to avoid creation of multiple equivalent instances.

## [0.6.0]

* Dimple is now built with Java 7 and requires MATLAB 2013b or later.

Projects using Dimple will have to be using a version of the Java JVM of at least 1.7. Because MATLAB 2013b is the first version based on Java 7, at least that version will now be required.


### Significant New Features Since 0.5

* Added support for generating and handling events
* Added built-in factors Multinomial, MultinomialUnnormalizedParameters, and MultinomialEnergyParameters
* Added built-in Poisson factor
* Added alternative algorithm for updating factor nodes with the SumProduct solver
* Support for block Metropolis-Hastings sampling in the Gibbs solver


### Significant Bug Fixes Since 0.5

* Corrected Gibbs conjugate sampling for Beta, Dirichlet, and Gamma samplers where either zero or more than one prior connects to a variable

## [0.5.0]

### Significant New Features Since 0.4 (Oct 5, 2013)

* Added junction tree solvers
* Merged Gaussian solver into the SumProduct solver, using Gaussian-parameterized messages for continuous variables in the SumProduct solver
* Added generic sampling-based approximate message computation for factors connecting to at least one continuous variable that do not otherwise support exact Gaussian message computation
* Added ExchangeableDirichlet factor function
* Added MultivariateNormal built-in factor function
* Added Multiplexer factor function
* Added MatrixProduct, VectorInnerProduct, and MatrixRealJointVectorProduct built-in factor functions, and corresponding use cases for the overloaded * operator in MATLAB
* Added RealJointSum, RealJointSubtract, and RealJointNegate built-in factor functions, and corresponding use cases for the overloaded + and - operators in MATLAB
* Generalize finite field custom factors to factor functions that can be used with any solver (retaining performance benefit when using SumProduct solver)
* Added Java solver template to aid in the design of new solvers
* Modified Java model package structure
* Added dimpleVersion MATLAB function

### Significant Bug Fixes Since 0.4

* Fix performance problem in tree scheduler for large graphs
* Fix performance problem for large deterministic-directed factors
* Adding continuous variables to a graph (Real, Complex, or RealJoint) no longer requires changing the solver first (since the default SumProduct solver now supports continuous variables)
* Fix support for including constants when adding custom built-in factors
* Fix Score method support in SumProduct solver for graphs with continuous variables
* Fix tree scheduler to support forests

### [0.4.0] - 2013-10-05

### New Features Since 0.3 (July 8)

* Added conjugate sampling support in Gibbs solver (see Gibbs Solver section of the user manual for a table of supported conjugate factors and associated samplers)
* Updated commons math library to commons-math3-3.2
* Added multithreading support for MinSum and SumProduct
* Added Java version of user manual
* Added javadoc
* Added LP Solver documentation
* Added MultiplexerCPD support
* Modified Java API for DiscreteDomain
* Modified Java API for factor tables and related classes
* Can now add boundary variables to a nested graph after graph creation, allowing use of implicit factor creation in sub-graphs
* Added built-in factor functions for Complex variables (ComplexSum, ComplexSubtract, ComplexNegate, ComplexConjugate, ComplexProduct, ComplexDivide, ComplexExp), and for MATLAB API added corresponding overloaded operators for Complex variables
* Added support for RealJoint and Complex variables in Gibbs solver
* Added ComplexDomain class in Java API (was already in MATLAB API)
* Support Value property in Real and RealJoint variable in Gaussian solver
* Support bounded domains in RealJoint and Complex variables
* Support FixedValue property in RealJoint and Complex variables, and support FixedValue property in Gaussian solver (all variable types)
* Added ComplexStream for rolled-up graphs with Complex variables
* In Gibbs solver, initialization and restart sample from Input distribution if a corresponding sampler is available
* In Gaussian solver, Input may now be set using a Normal factor function with fixed parameters (consistent with setting Inputs on real variables in other solvers); this allows Inputs to be set in a solver-neutral way
* Added built-in Dirichlet factor function
* Modified Categorical and DiscreteTransition factor functions to use RealJoint parameter, suitable for a Dirichlet prior; added two variants of each of these factor functions, one with an array of Real parameters, suitable for an array of Gamma priors, and one with an array of Real energy parameters, suitable for an array of NegativeExpGamma priors
* Added built-in factor functions for Bernoulli and Binomial distributions
* Added documentation for methods related to getting Beta free energy

### Fixes Since 0.3

* Tests and demos that require an optional MATLAB toolbox check for presence before trying to run


