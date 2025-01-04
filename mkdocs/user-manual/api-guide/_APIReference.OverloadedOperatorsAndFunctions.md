## List of Overloaded MATLAB Operators and Functions {#sec:overloaded}

The following table lists the set of overloaded MATLAB operators that
can be used to implicitly create factors. The table shows the operator,
the corresponding built-in factor (as described in
section [\[sec:builtInFactors\]](#sec:builtInFactors){reference-type="ref"
reference="sec:builtInFactors"}), the valid variable data types of the
inputs and outputs (B = Bit, D = Discrete, or R = Real, C = Complex,
RJ = RealJoint), and wether or not vectorized inputs are supported. The
use of these operators and functions is described in
section [\[sec:ImplicitFactorCreation\]](#sec:ImplicitFactorCreation){reference-type="ref"
reference="sec:ImplicitFactorCreation"}.

::: {#ftn:equals}
  Operator      Factor                                                                  Out                                                                    In1      In2                          Vectorized                                                                  Description
  ------------- ----------------------------------------------------------------------- ---------------------------------------------------------------------- -------- ---------------------------- --------------------------------------------------------------------------- ----------------------------
  $\&$          And                                                                     B                                                                      B        B                                                                                                        Logical AND
  $|$           Or                                                                      B                                                                      B        B                                                                                                        Logical OR
  xor()         Xor                                                                     B                                                                      B        B                                                                                                        Logical XOR
  $\sim$        Not                                                                     B                                                                      B        \-                                                                                                       Logical NOT
  $+$           Sum                                                                     D,R[^1]                                                                D,R      D,R                                                                                                      Plus
  \*            ComplexSum                                                              C                                                                      C,R      C,R                                                                                                      Complex plus
                RealJointSum                                                            RJ                                                                     RJ       RJ                           \-                                                                          RealJoint plus
  $-$           Subtract                                                                D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                                                                                                      Minus
  \*            ComplexSubtract                                                         C                                                                      C,R      C,R                                                                                                      Complex minus
  \*            RealJointSubtract                                                       RJ                                                                     RJ       RJ                           \-                                                                          RealJoint minus
  \*            Negate                                                                  D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      \-                                                                                                       Unary minus
  \*            ComplexNegate                                                           C                                                                      C, R     \-                                                                                                       Unary complex minus
                RealJointNegate                                                         RJ                                                                     RJ       \-                           \-                                                                          Unary RealJoint minus
  $*$           Product                                                                 D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                          [^2]                                                                        Scalar multiply
  \*            ComplexProduct                                                          C                                                                      C,R      C,R                          ^[2](#ftn:inScalar){reference-type="ref" reference="ftn:inScalar"}^         Complex scalar multiply
  \*            VectorInnerProduct                                                      R                                                                      D,R,RJ   D,R,RJ                       \-                                                                          Vector inner product[^3]
  \*            MatrixProduct                                                           R                                                                      D,R      D,R                          \-                                                                          Matrix multiply[^4]
  \*            MatrixVectorProduct                                                     R                                                                      D,R      D,R                          \-                                                                          Matrix-vector multiply[^5]
                MatrixRealJointVectorProduct                                            RJ                                                                     RJ,D,R   RJ,D,R                       \-                                                                          Matrix-vector multiply[^6]
  $.*$          Product                                                                 D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                                                                                                      Point-wise multiply
  \*            ComplexProduct                                                          C                                                                      C,R      C,R                                                                                                      Complex pointwise multiply
  $/$           Divide                                                                  D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                          [^7]                                                                        Scalar divide
  \*            ComplexDivide                                                           C                                                                      C,R      C,R                          ^[7](#ftn:divScalar){reference-type="ref" reference="ftn:divScalar"}^       Complex scalar divide
  $./$          Divide                                                                  D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                                                                                                      Point-wise divide
  \*            ComplexDivide                                                           C                                                                      C,R      C,R                                                                                                      Complex pointwise divide
  $\wedge$      Power                                                                   D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                          [^8]                                                                        Scalar power
  \*            Square[^9]                                                              D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                          ^[8](#ftn:powerScalar){reference-type="ref" reference="ftn:powerScalar"}^   Scalar square
  $.\wedge$     Power                                                                   D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                                                                                                      Point-wise power
  \*            Square^[9](#ftn:Square){reference-type="ref" reference="ftn:Square"}^   D,R^[1](#ftn:outReal){reference-type="ref" reference="ftn:outReal"}^   D,R      D,R                                                                                                      Point-wise square
  $'$           ComplexConjugate                                                        C                                                                      C        \-                                                                                                       Complex conjugate
  $<$           LessThan                                                                B                                                                      D,R      D,R                                                                                                      Less than
  $>$           GreaterThan                                                             B                                                                      D,R      D,R                                                                                                      Greater than
  $<=$          GreaterThan[^10]                                                        B                                                                      D,R      D,R                                                                                                      Less than or equal to
  $>=$          LessThan[^11]                                                           B                                                                      D,R      D,R                                                                                                      Greater than or equal to
  Equals()      Equals                                                                  B                                                                      B,D,R    B,D,R[^12]                                                                                               Equals[^13]
  NotEquals()   NotEquals                                                               B                                                                      B,D,R    B,D,R$^{\ref{ftn:equals}}$                                                                               Not equals[^14]
  mod()         \-                                                                      D                                                                      D        D                                                                                                        Modulo function[^15]
  abs()         Abs                                                                     D,R                                                                    D,R      \-                                                                                                       Absolute value
  \*            ComplexAbs                                                              R                                                                      C        \-                                                                                                       Complex absolute value
  sqrt()        Sqrt                                                                    R                                                                      R        \-                                                                                                       Square root
  log()         Log                                                                     R                                                                      R        \-                                                                                                       Natural log
  exp()         Exp                                                                     R                                                                      R        \-                                                                                                       Exponential function
  \*            ComplexExp                                                              C                                                                      C        \-                                                                                                       Complex exponential
  sin()         Sin                                                                     R                                                                      R        \-                                                                                                       Sine
  cos()         Cos                                                                     R                                                                      R        \-                                                                                                       Cosine
  tan()         Tan                                                                     R                                                                      R        \-                                                                                                       Tangent
  asin()        ASin                                                                    R                                                                      R        \-                                                                                                       Arc-sine
  acos()        ACos                                                                    R                                                                      R        \-                                                                                                       Arc-cosine
  atan()        ATan                                                                    R                                                                      R        \-                                                                                                       Arc-tangent
  sinh()        Sinh                                                                    R                                                                      R        \-                                                                                                       Hyperbolic sine
  cosh()        Cosh                                                                    R                                                                      R        \-                                                                                                       Hyperbolic cosine
  tanh()        Tanh                                                                    R                                                                      R        \-                                                                                                       Hyperbolic tangent
:::

[^1]: []{#ftn:outReal label="ftn:outReal"}If either input is Real, then
    the output is Real

[^2]: []{#ftn:inScalar label="ftn:inScalar"}One of the inputs may be a
    vector as long as the other is a scalar.

[^3]: If both inputs are vectors of the same dimension, then the
    VectorInnerProduct factor will be used. Each vector may be an array
    of scalar variables or a RealJoint variable.

[^4]: If both inputs are two-dimensional matrices of appropriate
    dimension, then the MatrixProduct factor will be used.

[^5]: If one input is a vector and the other is a matrix of appropriate
    dimension, then the MatrixVectorProduct factor will be used.

[^6]: If one input is a RealJoint variable and the other is a matrix of
    scalar variables or constants of appropriate dimension, then the
    MatrixRealJointVectorProduct factor will be used.

[^7]: []{#ftn:divScalar label="ftn:divScalar"}The dividend may be a
    vector as long as the divisor is a scalar.

[^8]: []{#ftn:powerScalar label="ftn:powerScalar"}The base may be a
    vector as long as the exponent is a scalar.

[^9]: []{#ftn:Square label="ftn:Square"}If the power is the constant 2,
    the Square factor is used instead of the Power factor.

[^10]: Uses GreaterThan factor, reversing the order.

[^11]: Uses LessThan factor, reversing the order.

[^12]: This function is not limited to two inputs, but can take an
    arbitrary number of inputs

[^13]: Equivalent to the $==$ operator, but the $==$ operator is not
    overloaded for this purpose so that it can instead be used to
    determine whether or not two variables reference the same Dimple
    variable.

[^14]: Equivalent to the $\sim=$ operator, but the $\sim=$ operator is
    not overloaded for this purpose so that it can instead be used to
    determine whether or not two variables reference the same Dimple
    variable.

[^15]: Currently, the mod() operator supports discrete variables only,
    and it uses the MATLAB definition of mod on negative numbers. This
    may be subject to change in future versions.
