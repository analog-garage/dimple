# Using Finite Field Variables {#sec:finiteFields}

## Overview

Dimple supports a special variable type called a FiniteFieldVariable and
a few custom factors for these variables. They represent finite fields
with $N=2^{n}$ elements. These fields find frequent use in error
correcting codes. Because Dimple can describe any discrete distribution,
it is possible to handle finite fields simply by describing their factor
tables. However, the native FiniteFieldVariable type is much more
efficient. In particular, variable addition and multiplication, which
naively require $\mathcal{O}(N^{3})$ operations, are calculated in only
$\mathcal{O}(N\log N)$ operations.

## Finite Fields Without Optimizations

As we mentioned previously, a user can construct (non-optimized) finite
fields from scratch.

First we create a domain for our variables using the MATLAB gf function
(for Galois Field).

=== "MATLAB"

    ```matlab
    m = 3; 
    numElements = 2^m;
    domain = 0:numElements-1;

    tmp = gf(domain,m); 
    real_domain = cell(length(tmp),1);
    for i = 1:length(tmp)
      real_domain{i} = tmp(i); 
    end
    ```

Next we create a bunch of variables with that domain.

=== "MATLAB"

    ```matlab
    x_slow = Discrete(real_domain);
    y_slow = Discrete(real_domain); 
    z_slow = Discrete(real_domain);
    ```

Now we create our graph and add the addition constraint.

=== "MATLAB"

    ```matlab
    fg_slow = FactorGraph();

    addDelta = @(x,y,z) x+y==z;
    fg_slow.addFactor(addDelta,x_slow,y_slow,z_slow);
    ```

This code runs in $\mathcal{O}(N^3)$ time since it tries all
combinations of x,y, and z. Next we set some inputs.

=== "MATLAB"

    ```matlab
    x_input = rand(size(x_slow.Domain.Elements));
    y_input = rand(size(y_slow.Domain.Elements));

    x_slow.Input = x_input;
    y_slow.Input = y_input;
    ```

Finally we set number of iterations, solve, and look at beliefs.

=== "MATLAB"

    ```matlab
    fg_slow.NumIterations = 1;

    fg_slow.solve();

    z_slow.Belief
    ```

The solver runs in $\mathcal{O}(N^2)$ time since z is determined by x
and y, x is determined by z, and y, and y is determined by x and z.

## Optimized Finite Field Operations

Rather than building finite field elements from scratch, a user can use
a built-in variable type and associated set of function nodes. These
native variables are much faster, both for programming and algorithmic
reasons. All of these operations are supported with the SumProduct
solver.

Dimple supports a FiniteFieldVariable variable type, which takes a
primitive polynomial (to be discussed later).

=== "Python"

    *not yet available*

=== "Java"

    ```java
    FiniteFieldVariable v = new FiniteFieldVariable(prim_poly);
    ```

    This would create a finite field Variable with the given primitive
    polynomial.

=== "MATLAB"

    and in MATLAB can also take the dimensions of the matrix as constructor arguments:

    ```matlab
    v = FiniteFieldVariable(prim_poly,3,2);
    ```

    This would create a 3x2 matrix of finite field Variables with the given
    primitive polynomial.


Users can use the following syntax to create an addition factor node
with three variables using the 'FiniteFieldAdd' built-in factor:

=== "Java"

    ```java
    myFactorGraph.addFactor("FiniteFieldAdd",x,y,z);
    ```
=== "MATLAB"

    ```matlab
    myFactorGraph.addFactor("FiniteFieldAdd",x,y,z);
    ```

The factor function must have the "FiniteFieldAdd" name in order for
the SumProduct solver to use the correct custom factor.

Adding this variable take $\mathcal{O}(1)$ time and solving takes
$\mathcal{O}(N\log N)$ time, where N is the size of the finite field
domain.

Similarly, the following syntax can be used to create a factor node with
three variables for multiplication:

=== "Python"

    *not yet available*

=== "Java"

    ```java
    myFactorGraph.addFactor("FiniteFieldMult",x,y,z);
    ```

=== "MATLAB"

    ```matlab
    myFactorGraph.addFactor('FiniteFieldMult',x,y,z);
    ```

Under the hood this will create one of two custom factors,
CustomFiniteFieldConstMult or CustomFiniteFieldMult. The former will be
created if x or y is a constant and the latter will be created if
neither is a constant. This allows Dimple to optimize belief propagation
so that it runs in O(N) for multiplication by constants and O(Nlog(N))
in the more general case.

Suppose we have the finite field equation $x1+x2+x3+x4=0$.

We can not express that using the FiniteFieldAdd factor directly, since
it accepts only three arguments. However, we can support larger addition
constraints by building a tree of these constraints. We do so using the
following function:

=== "MATLAB"

    ```matlab
    NumVars = 4;
    [graph,vars] = getNVarFiniteFieldPlus(prim_poly,NumVars);
    ```

This function takes a primitive polynomial and the number of variables
involved in the constraint and builds up a graph such that
$x_1+...+x_n = 0$ .

It returns both the graph and the external variables of the graph. This
can be used in one of two ways: setting inputs on the variables and
solving the graph directly or using this as a nested sub-graph.

Elements of a finite field with base 2 can be represented as polynomials
with binary coefficients. Polynomials with binary coefficients can be
represented as strings of bits. For instance, $x^3+x+1$ could be
represented in binary as 1011. Furthermore, that number can be
represented by the (decimal) integer 11. When using finite fields for
decoding, we are often taking bit strings and re-interpreting these as
strings of finite field elements. We can use the FiniteFieldProjection
built-in factor to relate n bits to a finite field variable with a
domain of size $2^n$.

The following code shows how to do that:

=== "Python"

    *not yet available*

=== "Java"

    ```java
    Object [] vars = new Object[n*2];
            
    for (int j = 0; j < n; j++)
    {
        vars[j*2] = j;
        vars[j*2+1] = new Bit();
    }
    myFactorGraph.addFactor(FiniteFieldProjection(),vars);
    ```

=== "MATLAB"

    ```matlab
    args = cell(n*2,1);
    for j = 0:n-1
       args{j*2+1} = j;
       args{j*2+2} = bits(n-j);
    end

    myFactorGraph.addFactor('FiniteFieldProjection',v,args{:});
    ```

## Primitive Polynomials

See [Wikipedia](https://en.wikipedia.org/wiki/Primitive_polynomial_(field_theory))
for a definition.

## Algorithmics

Dimple interprets the domains as integers mapping to bit strings
describing the coefficients of polynomials. Internally, the
FiniteFieldVariable contains functions to map from this representation
to a representation of powers of the primitive polynomial. This
operation is known as the discrete log. Similarly Dimple
FiniteFieldVariables provide a function to map the powers back to the
original representation (i.e., an exponentiation operator).

-   The addition code computes $x+y$ by performing a fast Hadamard
    transform of the distribution of both $x$ and $y$, pointwise
    multiplying the transforms, and then performing an inverse fast
    Hadamard transform.

-   The generic multiplication code computes $xy$ by performing a fast
    Fourier transform on the distribution of the non-zero elements of
    the distribution, pointwise multiplying the transforms, performing
    an inverse fast Fourier transform, and then accounting for the zero
    elements.

-   The constant multiplication code computes $x$ by converting the
    distribution of the non-zero values of $x$ to the discrete log
    domain (which corresponds to reshuffling the array), adding the
    discrete log of modulo $N-1$ (cyclically shifting the array), and
    exponentiating (unshuffling the array back to the original
    representation).
