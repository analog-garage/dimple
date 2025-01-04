## List of Built-in Factors {#sec:builtInFactors}

The following table lists the current set of built-in Dimple factors.
For each, the name is given, followed by the set of variables that would
be connected to the factor, followed by any constructor arguments.
Optional variables and constructor arguments are in brackets. And an
arbitrary length list or array of variables is followed by ellipses. The
allowed set of variable data-types for each variable is given in
parentheses (B = Bit, D = Discrete, F = FiniteFieldVariable, R = Real,
C = Complex, RJ = RealJoint).

::: {#ftn:conjugatePrior}
  Name                                       Variables                                  Constructor                   Description
  ------------------------------------------ ------------------------------------------ ----------------------------- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Abs                                        out(D,R) in(D,R)                           \[smoothing\]                 Deterministic absolute value function, where out = abs(in). An optional smoothing value may be specified as a constructor argument[^1].
  ACos                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic arc-cosine function, where out = acos(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  AdditiveNoise                              out(R) in(B,D,R)                           $\sigma$                      Add Gaussian noise with a known standard deviation, $\sigma$, specified in constructor.
  And                                        out(B) in\...(B)                           \-                            Deterministic logical AND function, where out = AND(in\...).
  ASin                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic arc-sine function, where out = asin(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ATan                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic arc-tangent function, where out = atan(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Bernoulli                                  \[$\rho$\](R) x\...(B)                     \[$\rho$\]                    Bernoulli distribution, $p(x|\rho)$, where $\rho$ is a parameter indicating the probability of one, and x is an array of Bit variables. There can be any number of x variables, all associated with the same parameter value. The conjugate prior for the parameter, $\rho$, is a Beta distribution[^2]. The parameter, $\rho$, can be a variable or a constant specified in the constructor.
  Beta                                       \[$\alpha$\](R) (R) value\...(R)           \[$\alpha$\]                  Beta distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\alpha$ and $\beta$ can be variables, or if both are constant they can be specified in the constructor.
  Binomial                                   \[$N$\](D) $\rho$(R) x(D)                  \[$N$\]                       Binomial distribution, $p(x|N, \rho)$, where $N$ is the total number of trials, $\rho$ is a parameter indicating the success probability, and x is a count of success outcomes. Parameter $N$ can be a Discrete variable with positive integer values or a constant integer value specified in the constructor. The domain of x must include integers from 0 through $N$, or if $N$ is a variable, through the maximum value in the domain of $N$. The conjugate prior for the parameter, $\rho$, is a Beta distribution^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  Categorical                                \[$\alpha$\](RJ) x\...(D)                  \[$\alpha$\]                  Categorical distribution, $p(x | \alpha)$, where $\alpha$ is a vector of parameter variables and x is an array of discrete variables. The number of elements in $\alpha$ must equal the domain size of x. There can be any number of x variables, all associated with the same parameter values. The $\alpha$ parameters are represented as a normalized probability vector. The conjugate prior for this representation is a Dirichlet distribution^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  CategoricalEnergyParameters                \[$\alpha$\]\...(R) x\...(D)               N, \[$\alpha$\]               Categorical distribution, $p(x | \alpha)$, where $\alpha$ is a vector of parameter variables and x is an array of discrete variables. The number of elements in $\alpha$ and the domain size of x must equal the value of the constructor argument, N. There can be any number of x variables, all associated with the same parameter values. In this alternative version of the Categorical distribution, the $\alpha$ parameters are represented as energy values, that is, $\alpha = -\log(\rho)$, where $\rho$ are unnormalized probabilities. The conjugate prior for this representation is such that each entry of $\alpha$ is independently distributed according to a negative exp-Gamma distribution, all with a common $\beta$ parameter^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  CategoricalUnnormalizedParameters          \[$\alpha$\]\...(R) x\...(D)               N, \[$\alpha$\]               Categorical distribution, $p(x | \alpha)$, where $\alpha$ is a vector of parameter variables and x is an array of discrete variables. The number of elements in $\alpha$ and the domain size of x must equal the value of the constructor argument, N. There can be any number of x variables, all associated with the same parameter values. In this alternative version of the Categorical distribution, the $\alpha$ parameters are represented as a vector of unnormalized probability values. The conjugate prior for this representation is such that each entry of $\alpha$ is independently distributed according to a Gamma distribution, all with a common $\beta$ parameter^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  ComplexAbs                                 out(R) in(C)                               \[smoothing\]                 Deterministic complex absolute value, where out = $\sqrt{Re(\mathrm{in}) + Im(\mathrm{in})}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexConjugate                           out(C) in(C,R)                             \[smoothing\]                 Deterministic complex conjugate function, where out = in$^{*}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexDivide                              quotient(C) dividend(C,R) divisor(C,R)     \[smoothing\]                 Deterministic complex divide function, where $\mathrm{quotient} = \frac{\mathrm{dividend}}{\mathrm{divisor}}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexExp                                 out(C) in(C,R)                             \[smoothing\]                 Deterministic complex exponentiation function, where out = exp(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexNegate                              out(C) in(C,R)                             \[smoothing\]                 Deterministic complex negation function, where out = -in. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexProduct                             out(C) in\...(C,R)                         \[smoothing\]                 Deterministic complex product function, where $\mathrm{out} = \prod \mathrm{in}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexSubtract                            out(C) posIn(C,R) negIn\...(C,R)           \[smoothing\]                 Deterministic complex subtraction function, where $\mathrm{out} = \mathrm{posIn} - \sum \mathrm{negIn}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexSum                                 out(C) in\...(C,R)                         \[smoothing\]                 Deterministic complex summation function, where $\mathrm{out} = \sum \mathrm{in}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  ComplexToRealAndImaginary                  outReal(R) outImag(R) in(RJ)               \[smoothing\]                 Deterministic conversion of a Complex variable to two Real variables, with the first representing the real component and the second representing the imaginary component. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Cos                                        out(D,R) in(D,R)                           \[smoothing\]                 Deterministic cosine function, where out = cos(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Cosh                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic hyperbolic-cosine function, where out = cosh(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Dirichlet                                  \[$\alpha$\](RJ) value\...(RJ)             \[$\alpha$\]                  Dirichlet distribution. There can be any number of value variables, all associated with the same parameter values. Parameter vector $\alpha$ can be a RealJoint variable or a constant specified in the constructor. The dimension of $\alpha$ and each of the value variables must be identical.
  DiscreteTransition                         y(D) x(D) A\...(RJ)                        \-                            Parameterized discrete transition factor, $p(y | x, A)$, where x and y are discrete variables, and $A$ is a matrix of transition probabilities. The transition matrix is organized such that columns correspond to the output distribution for each input state. That is, the transition matrix multiplies on the left. Each column of A corresponds to a RealJoint variable. The number of columns in A must equal the domain size of x, and the dimension of each element of A must equal the domain size of y. Each element of A corresponds to a normalized probability vector. The conjugate prior for this representation is such that each element of A is distributed according to a Dirichlet distribution^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  DiscreteTransitionEnergyParameters         y(D) x(D) A\...(R)                         $N_{y}, N_{x} | \newline N$   Parameterized discrete transition factor, $p(y | x, A)$, where x and y are discrete variables, and $A$ is a matrix of transition probabilities. The transition matrix is organized such that columns correspond to the output distribution for each input state. That is, the transition matrix multiplies on the left. The number of columns in A and the domain size of x must equal the value of the constructor argument, $N_{x}$ and the number of rows in A and the domain size of y must equal the value of the constructor argument $N_{y}$. If $N_{x}$ and $N_{y}$ are equal, a single constructor argument, $N$, may be used. The elements of the matrix A are represented as energy values, that is, $A_{i,j} = -\log(\rho_{i,j})$, where $\rho$ is an unnormalized transition probability matrix. The conjugate prior for this representation is such that each entry of A is independently distributed according to a negative exp-Gamma distribution, all with a common $\beta$ parameter^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  DiscreteTransitionUnnormalizedParameters   y(D) x(D) A\...(R)                         $N_{y}, N_{x} | \newline N$   Parameterized discrete transition factor, $p(y | x, A)$, where x and y are discrete variables, and $A$ is a matrix of transition probabilities. The transition matrix is organized such that columns correspond to the output distribution for each input state. That is, the transition matrix multiplies on the left. The number of columns in A and the domain size of x must equal the value of the constructor argument, $N_{x}$ and the number of rows in A and the domain size of y must equal the value of the constructor argument $N_{y}$. If $N_{x}$ and $N_{y}$ are equal, a single constructor argument, $N$, may be used. The elements of the matrix A are represented as unnormalized probability values. The conjugate prior for this representation is such that each entry of A is independently distributed according to a Gamma distribution, all with a common $\beta$ parameter^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  Divide                                     quotient(D,R) dividend(D,R) divisor(D,R)   \[smoothing\]                 Deterministic divide function, where $\mathrm{quotient} = \frac{\mathrm{dividend}}{\mathrm{divisor}}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Equality                                   value\...(B,D,R)                           \[smoothing\]                 Deterministic equality constraint. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Equals                                     out(B) in\...(B,D,R)                       \-                            Deterministic equals function, where out = (in(1) == in(2) == \... ).
  ExchangeableDirichlet                      \[$\alpha$\](R) value\...(RJ)              N, \[$\alpha$\]               Exchangeable Dirichlet distribution. This is a variant of the Dirichlet distribution parameterized with a single common parameter for all dimensions. There can be any number of value variables, all associated with the same parameter value. Parameter $\alpha$ can be a Real variable or a constant specified in the constructor. The dimension of each of the value variables must be identical and equal to the value of N, specified in the constructor.
  Exp                                        out(D,R) in(D,R)                           \[smoothing\]                 Deterministic exponentiation function, where out = exp(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  FiniteFieldAdd                             out(F) in1(F) in2(F)                       \-                            Deterministic finite field two-input addition. See section [\[sec:finiteFields\]](#sec:finiteFields){reference-type="ref" reference="sec:finiteFields"} for a description of how to use finite field variables.
  FiniteFieldMult                            out(F) in1(F) in2(F)                       \-                            Deterministic finite field two-input multiplication. See section [\[sec:finiteFields\]](#sec:finiteFields){reference-type="ref" reference="sec:finiteFields"} for a description of how to use finite field variables.
  FiniteFieldProjection                      fieldVar(F) indices(const) bits\...(B)     \-                            Deterministic projection of a finite field variable onto a set of bit variables corresponding to the bits of the field value. The indices argument is a constant array, which must be a permutation of $0$ through $N-1$, where $N$ is the number of bits in the finite field value. The indices represent the order of the projection of the bits in the finite field value onto the corresponding Bit variable in the list of bits. See section [\[sec:finiteFields\]](#sec:finiteFields){reference-type="ref" reference="sec:finiteFields"} for a description of how to use finite field variables.
  Gamma                                      \[$\alpha$\](R) (R) value\...(R)           \[$\alpha$\]                  Gamma distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\alpha$ and $\beta$ can be variables, or if both are constant they can be specified in the constructor.
  GreaterThan                                out(B) in1(B,D,R) in2(B,D,R)               \-                            Deterministic greater-than function, where out = in1 $>$ in2.
  InverseGamma                               \[$\alpha$\](R) (R) value\...(R)           \[$\alpha$\]                  Inverse Gamma distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\alpha$ and $\beta$ can be variables, or if both are constant they can be specified in the constructor.
  LessThan                                   out(B) in1(B,D,R) in2(B,D,R)               \-                            Deterministic greater-than function, where out = in1 $<$ in2.
  LinearEquation                             out(D,R) in(B,D,R)                         weights                       Deterministic linear equation, multiplying an input vector by a constant weight vector to equal the output variable. The weight vector is specified in the constructor. The number of *in* variables must equal the length of the weight vector. An optional smoothing value may be specified as a constructor argument^[1](#ftn:smoothing){reference-type="ref" reference="ftn:smoothing"}^.
  Log                                        out(D,R) in(D,R)                           \[smoothing\]                 Deterministic natural log function, where out = log(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  LogNormal                                  \[$\mu$\](R) (R) value\...(R)              \[$\mu$\]                     Log-normal distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\mu$ (mean) and $\tau = \frac{1}{\sigma^{2}}$ (precision) can be variables, or if both are constant then fixed parameters can be specified in the constructor.
  MatrixProduct                              C(D,R) A(D,R) B(D,R)                       $N_{r}$ $N_{x}$ $N_{c}$       Deterministic matrix product function, $C = AB$, where $A$, $B$, and $C$ are matrices. Constructor arguments, $N_{r}$ specifies the number of rows in A and C, $N_{x}$ specifies the number of columns in A and number of rows in B, and $N_{c}$ specifies the number of columns in B and C. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  MatrixVectorProduct                        y(D,R) M(D,R) x(D,R)                       $N_{x}$ $N_{y}$               Deterministic matrix-vector product function, $y = Mx$, where $x$ and $y$ are vectors and $M$ is a matrix. Constructor arguments, $N_{x}$ and $N_{y}$, specify the input and output vector lengths, respectively. The matrix dimension is $N_{y} \times N_{x}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  MatrixRealJointVectorProduct               y(RJ) M(D,R) x(RJ)                         $N_{x}$ $N_{y}$               Deterministic matrix-vector product function, $y = Mx$, where $x$ and $y$ are RealJoint values and $M$ is a matrix. Constructor arguments, $N_{x}$ and $N_{y}$, specify the input and output vector lengths, respectively. The matrix dimension is $N_{y} \times N_{x}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Multinomial                                \[$N$\](D) $\alpha$(RJ) x\...(D)           \[$N$\]                       Multinomial distribution, $p(x|N, \alpha)$, where $N$ is the total number of trials, $\alpha$ is a vector of parameter variables, and x is a count of outcomes in each category. Parameter $N$ can be a Discrete variable with positive integer values or a constant integer value specified in the constructor. The number of elements in $\alpha$ must exactly match the number of elements of $x$. The domain of each x variable must include integers from 0 through $N$, or if $N$ is a variable, through the maximum value in the domain of $N$. The $\alpha$ parameters are represented as a normalized probability vector. The conjugate prior for this representation is a Dirichlet distribution^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  MultinomialEnergyParameters                \[$N$\](D) $\alpha$\...(R) x\...(D)        $D$, \[$N$\]                  Multinomial distribution, $p(x|N, \alpha)$, where $N$ is the total number of trials, $\alpha$ is a vector of parameter variables, and x is a count of outcomes in each category. Parameter $N$ can be a Discrete variable with positive integer values or a constant integer value specified in the constructor. The number of elements in $\alpha$ must exactly match the number of elements of $x$, which must match the value of the constructor argument, $D$. The domain of each x variable must include integers from 0 through $N$, or if $N$ is a variable, through the maximum value in the domain of $N$. In this alternative version of the Multinomial distribution, the $\alpha$ parameters are represented as energy values, that is, $\alpha = -\log(\rho)$, where $\rho$ are unnormalized probabilities. The conjugate prior for this representation is such that each entry of $\alpha$ is independently distributed according to a negative exp-Gamma distribution, all with a common $\beta$ parameter^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  MultinomialUnnormalizedParameters          \[$N$\](D) $\alpha$\...(R) x\...(D)        $D$, \[$N$\]                  Multinomial distribution, $p(x|N, \alpha)$, where $N$ is the total number of trials, $\alpha$ is a vector of parameter variables, and x is a count of outcomes in each category. Parameter $N$ can be a Discrete variable with positive integer values or a constant integer value specified in the constructor. The number of elements in $\alpha$ must exactly match the number of elements of $x$, which must match the value of the constructor argument, $D$. The domain of each x variable must include integers from 0 through $N$, or if $N$ is a variable, through the maximum value in the domain of $N$. In this alternative version of the Multinomial distribution, the $\alpha$ parameters are represented as a vector of unnormalized probability values. The conjugate prior for this representation is such that each entry of $\alpha$ is independently distributed according to a Gamma distribution, all with a common $\beta$ parameter^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  Multiplexer                                out(any) select in\...(any)                \[smoothing\]                 Deterministic multiplexer[^3]. The selector must be a discrete variable that selects one of the inputs to pass to the output. The data type of all inputs must be identical to that of the output. For RealJoint variables, the dimension of all inputs must equal that of the output. The with domain of the selector variable must be zero-based contiguous integers, $0...N-1$, where $N$ is the number of input variables. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  MultivariateNormal                         value\...(RJ)                              $\mu$ $\Sigma$                Multivariate Normal distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\mu$ (mean vector) and $\Sigma$ (covariance matrix) are constant that must be specified in the constructor[^4]. The dimension of the mean vector, both dimensions of the covariance matrix, and the dimension of each value variable must be identical.
  Negate                                     out(D,R) in(D,R)                           \[smoothing\]                 Deterministic negation function, where out = -in. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  NegativeExpGamma                           \[$\alpha$\](R) (R) value\...(R)           \[$\alpha$\]                  Negative exp-Gamma distribution, which is a distribution over a variable whose negative exponential is Gamma distributed. That is, this is the negative log of a Gamma distributed variable. There can be any number of value variables, all associated with the same parameter values. Parameters $\alpha$ and $\beta$ can be variables, or if both are constant they can be specified in the constructor, and correspond to the parameters of the underlying Gamma distribution.
  Normal                                     \[$\mu$\](R) (R) value\...(R)              \[$\mu$\]                     Normal distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\mu$ (mean) and $\tau = \frac{1}{\sigma^{2}}$ (precision) can be variables, or if both are constant then fixed parameters can be specified in the constructor.
  Not                                        out(B) in(B)                               \-                            Deterministic logical NOT of function, where out =  in.
  NotEquals                                  out(B) in\...(B,D,R)                       \-                            Deterministic not-equals function, where out = $\sim$(in(1) == in(2) == \... ).
  Or                                         out(B) in\...(B)                           \-                            Deterministic logical OR function, where out = OR(in\...).
  Poisson                                    \[$\lambda$\](R) $k$(D)                    \[$\lambda$\]                 Poisson distribution, $p(k|\lambda)$, where $\lambda$ is the rate parameter, and k is the discrete output. While the value of $k$ for a Poission distribution is unbounded, the domain should be set to include integers from 0 through a maximum value. The maximum value should be a multiple of the maximum likely value of $\lambda$[^5]. The conjugate prior for the parameter, $\lambda$, is a Gamma distribution^[1](#ftn:conjugatePrior){reference-type="ref" reference="ftn:conjugatePrior"}^.
  Power                                      out(D,R) base(D,R) power(D,R)              \[smoothing\]                 Deterministic power function, where out = base $^{\mathrm{power}}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Product                                    out(D,R) in\...(B,D,R)                     \[smoothing\]                 Deterministic product function, where $\mathrm{out} = \prod \mathrm{in}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Rayleigh                                   \[$\sigma$\](R) value\...(R)               \[$\sigma$\]                  Rayleigh distribution. There can be any number of value variables, all associated with the same parameter value. Parameter $\sigma$ can be a variable, or if constant, can be specified in the constructor.
  RealAndImaginaryToComplex                  out(C) inReal(R) inImag(R)                 \[smoothing\]                 Deterministic conversion of two Real variables to a Complex variable, where the first input represents the real component and the second represents the imaginary component. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  RealJointNegate                            out(RJ) in(RJ)                             \[smoothing\]                 Deterministic negation function for RealJoint variables, where out = -in. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  RealJointProjection                        out(R) in(RJ)                              index                         Deterministic conversion of a RealJoint variable to a Real variable corresponding to one specific element of the RealJoint variable. The $index$ constructor argument indicates which element of the RealJoint variable to be used (using zero-based numbering). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  RealJointSubtract                          out(RJ) posIn(RJ) negIn\...(RJ)            \[smoothing\]                 Deterministic subtraction function for RealJoint variables, where $\mathrm{out} = \mathrm{posIn} - \sum \mathrm{negIn}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  RealJointSum                               out(RJ) in\...(RJ)                         \[smoothing\]                 Deterministic summation function for RealJoint variables, where $\mathrm{out} = \sum \mathrm{in}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  RealJointToRealVector                      out\...(R) in(RJ)                          \[smoothing\]                 Deterministic conversion of a RealJoint variable to a vector of Real variables. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  RealVectorToRealJoint                      out(RJ) in\...(R)                          \[smoothing\]                 Deterministic conversion of a vector of Real variables to a RealJoint variable. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Sin                                        out(D,R) in(D,R)                           \[smoothing\]                 Deterministic sine function, where out = sin(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Sinh                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic hyperbolic-sine function, where out = sinh(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Sqrt                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic square root function, where out = sqrt(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Square                                     out(D,R) in(D,R)                           \[smoothing\]                 Deterministic square function, where out = in$^{2}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Subtract                                   out(D,R) posIn(B,D,R) negIn\...(B,D,R)     \[smoothing\]                 Deterministic subtraction function, where $\mathrm{out} = \mathrm{posIn} - \sum \mathrm{negIn}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Sum                                        out(D,R) in\...(B,D,R)                     \[smoothing\]                 Deterministic summation function, where $\mathrm{out} = \sum \mathrm{in}$. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Tan                                        out(D,R) in(D,R)                           \[smoothing\]                 Deterministic tangent function, where out = tan(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  Tanh                                       out(D,R) in(D,R)                           \[smoothing\]                 Deterministic hyperbolic-tangent function, where out = tanh(in). An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  VectorInnerProduct                         z(D,R) x(D,R,RJ) y(D,R,RJ)                 \[smoothing\]                 Deterministic vector inner product function, $z = x \dot y$, where $x$ and $y$ are vectors and $z$ is a scalar. Each vector input may be either an array of scalar variables, or a single RealJoint variable. The number of elements in $x$ and $y$ must be identical. An optional smoothing value may be specified as a constructor argument$^{\ref{ftn:smoothing}}$.
  VonMises                                   \[$\mu$\](R) (R) value\...(R)              \[$\mu$\]                     Von Mises distribution. There can be any number of value variables, all associated with the same parameter values. Parameters $\mu$ (mean) and $\tau = \frac{1}{\sigma^{2}}$ (precision) can be variables, or if both are constant then fixed parameters can be specified in the constructor. The distribution is non-zero for value variables in the range $-\pi$ to $\pi$.
  Xor                                        out(B) in\...(B)                           \-                            Deterministic logical XOR function, where out = XOR(in\...).
:::

### Factor Creation Utility Functions

Dimple includes some helper functions to create other built-in factors
using a similar syntax to the overloaded MATLAB functions described in
section [\[sec:overloaded\]](#sec:overloaded){reference-type="ref"
reference="sec:overloaded"}. As for other overloaded functions, above,
Dimple automatically creates the factors as well as the output
variable(s). Such helper functions are defined for the following
built-in distributions:

-   Bernoulli

-   Beta

-   Binomial

-   Categorical[^6]

-   CategoricalEnergyParameters

-   Dirichlet

-   ExchangeableDirichlet

-   Gamma

-   InverseGamma

-   LogNormal

-   Multinomial[^7]

-   MultinomialEnergyParameters

-   MultivariateNormal[^8]

-   NegativeExpGamma

-   Normal

-   Poisson[^9]

-   Rayleigh

-   VonMises

For each, the arguments are the parameters of the distribution. For
example:

    W = Gamma(alpha, beta);
    X = Normal(mean, precision);
    Y = Categorical(alpha);
    Z = Rayleigh(sigma);
    D = Dirichlet(alpha);
    E = ExchangeableDirichlet(dimension, alpha);

The parameters can be variables, constants[^10], or some of each[^11].

By default, calling one of these functions creates a single output
variable, and the factor is added to the most-recently created graph.
But, optional arguments allow you to specify the dimensions of the array
of output variables, or to specify the factor graph. These arguments can
be in either order after the parameters. For example:

    W = Gamma(alpha, beta, altGraph);
    X = Normal(mean, precision, [100, 1]);
    Y = Categorical(alpha, [10, 10, 2], aGraph);
    Z = Rayleigh(sigma, myGraph, size(somethingElse));

Dimple also includes built-in helper functions to create other factors
that aren't distributions. Specifically:

    outValue = If(condition, trueValue, falseValue);

The "If" helper function implements a conditional assignment function
using the built-in Multiplexer factor. The function creates an output
variable (or array of variables) that is of the same type and dimension
as the trueValue and falseValue variables. These two input variables
must either be of identical type, domain, and dimensions, or one of
these may be a constant. The condition must be a Bit variable or array
of variables. If trueValue and falseValue are arrays, the function
creates an array of Multiplexer factors, each connected to the
corresponding inputs and outputs. In the case of an array, if condition
is a scalar Bit variable, all of these are connected to the same
condition bit. Otherwise, condition must be of the same dimension as the
other inputs. As with the other factor creation utilities, the function
optionally takes a graph input to specify a particular graph in which to
add the factors.

Dimple also includes some built-in helper functions to create structured
graphs, combining smaller factors to form an efficient implementation of
a larger subgraph. Specifically, the following functions are provided:

-   getNBitXorDef(n), where n is a positive integer. Returns a nestable
    graph and an array of n-Bit connector variables. Efficient tree
    implementation of the XORDelta function.

-   getVXOR(n), where n is a positive integer. Returns a nestable graph
    and an array of n-Bits connector variables. Constrains exactly one
    bit to be 1, and all others to be 0.

-   MultiplexerCPD(domains) - See
    section [\[sec:multiplexerCPD\]](#sec:multiplexerCPD){reference-type="ref"
    reference="sec:multiplexerCPD"}

-   MultiplexerCPD(domain, numZs) - See
    section [\[sec:multiplexerCPD\]](#sec:multiplexerCPD){reference-type="ref"
    reference="sec:multiplexerCPD"}

<!-- -->

-   MultiplexerCPD(domains) - See
    section [\[sec:multiplexerCPD\]](#sec:multiplexerCPD){reference-type="ref"
    reference="sec:multiplexerCPD"}

-   MultiplexerCPD(domain, numZs) - See
    section [\[sec:multiplexerCPD\]](#sec:multiplexerCPD){reference-type="ref"
    reference="sec:multiplexerCPD"}

[^1]: []{#ftn:smoothing label="ftn:smoothing"}If smoothing is enabled,
    the factor function becomes
    $e^{-(\textrm{out} - F(\textrm{in}))^2/\textrm{smoothing}}$ (making
    it non-deterministic) instead of
    $\delta(\textrm{out} - F(\textrm{in}))$, where $F$ is the
    deterministic function associated with this factor. This is useful
    for solvers that do not work well with deterministic real-valued
    factors, such as particle BP, particularly when annealing is used.

[^2]: It is not necessary to use the conjugate prior, but in some cases
    there may be a benefit.

[^3]: Note that for the SumProduct solver, an optimized custom
    implementation of this factor function is used automatically, which
    avoids creation of a corresponding factor table.

[^4]: In this version of Dimple, there is no support for variable
    parameters in the MultivariateNormal distribution.

[^5]: If the maximum value is 5 times larger than the largest value of
    $\lambda$, then less than 0.1 of the probability mass would fall
    above this value.

[^6]: This utility can be used to create either a Categorical or
    CategoricalUnnormalizedParameters factor, depending on whether the
    first argument is a RealJoint variable or an array of Real
    variables.

[^7]: This utility can be used to create either a Multinomial or
    MultinomialUnnormalizedParameters factor, depending on whether the
    first argument is a RealJoint variable or an array of Real
    variables.

[^8]: In the current version of Dimple, only constant parameters are
    supported by the MultivariateNormal factor.

[^9]: This utility takes an argument $\lambda$, either a variable or
    constant, plus an additional argument $maxK$, which indicates the
    maximum likely value of the output variable, k. The value of $maxK$
    should be a multiple of the maximum likely value of $\lambda$ (if
    $maxK$ is 5 times larger than the largest value of $\lambda$, then
    less than 0.1 of the probability mass would be greater than $maxK$).

[^10]: If *all* parameter inputs are constants, then instead of creating
    both variables and factors, the variables are created, and the Input
    to each variable is set accordingly.

[^11]: For cases where a parameter is a vector of real values, the
    parameter may be a Real variable array, a cell-arrays that may mix
    Real variables and constants, or an array of constants.
