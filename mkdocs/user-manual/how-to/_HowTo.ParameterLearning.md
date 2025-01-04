# Parameter Learning

Dimple currently has two supported parameter learning algorithms:
Expectation-Maximization on directed graphs and PseudoLikelihood
Parameter Estimation on undirected graphs. Both of these algorithms are
provided as experimental implementations.

## PseudoLikelihood Parameter Estimation on Undirected Graphs

The PseudoLikelihood Parameter Estimation uses the following as its
objective function:

$$\ell_{PL}(\theta) = \frac{1}{M}\sum_m \sum_i \sum_{a \sim  i} (x_{-i}^{(m)},x_i^{(m)}) - \\
\frac{1}{M} \sum_m \sum_i log Z(x_{N(i)};\theta)$$

Currently it uses a very naive gradient descent optimizer. Future
versions will likely have pluggable optimizers for each learning
algorithm. (Likely including algorithms like BFGS).

The following creates a learner and initializes a few variables:

=== "Python"

    *not yet available*

=== "Java"

    ```java
    PseudoLikelihood pll = new PseudoLikelihood(fg, tables, vars2);
    ```

=== "MATLAB"

    ```matlab
    pl = PLLearner(factorGraph,factorTables,variables);
    ```

Arguments:

=== "Python"

    *not yet available*

=== "Java"

    -   factorGraph - the Factor Graph of interest
    
    -   factorTables - an array of factor tables for which to learn
        parameters.
    
    -   variables - an array of variable matrices (the order must match your
        data ordering).

=== "MATLAB"

    -   factorGraph - the Factor Graph of interest
    
    -   factorTables - a cell array of factor tables for which to learn
        parameters.
    
    -   variables - a cell array of variable matrices (the order must match
        your data ordering).

The following method runs pseudo likelihood gradient descent. After it
is run, the factor tables will contain the values of the learned
parameters. For now the optimizer is simply a routine that multiplies
the gradient by a scale factor and applies that change to the
parameters. In the future, optimizers will be first class citizens and
can be plugged into learners.

=== "Python"

    *not yet available*

=== "Java"

    ```java
    pll.learn(data, numSteps, scaleFactor);
    ```

    Arguments:
    
    -   data - An MxN array where M is the number of samples and N is the
        number of variables. Variable data must be specified in the same
        order the variables were specified in the learner's constructor. For
        now, this data specifies the domain indices, not the domain values.
        This should be fixed in the future (so the user can do either). In
        reality, we'll probably split out training data into a more
        interesting data structure. (Same with the optimizer)
    
    -   numSteps - How many gradient descent steps should the optimizer run.
    
    -   scaleFactor - The value by which we multiply the gradient before
        adding to the current parameters. oldParams = oldParams +
        scaleFactor\*gradient

=== "MATLAB"

    ```matlab
    args.numSteps = 100;
    args.scaleFactor = 0.05;
    pl.learn(samples,args);
    ```

    Arguments:
    
    -   samples - An MxN matrix where M is the number of samples and N is
        the number of variables. Variable data must be specified in the same
        order the variables were specified in the learner's constructor. For
        now, this data specifies the domain indices, not the domain values.
        This should be fixed in the future (so the user can do either). In
        reality, we'll probably split out training data into a more
        interesting data structure. (Same with the optimizer)
    
    -   args.numSteps - How many gradient descent steps should the optimizer
        run.
    
    -   args.scaleFactor - The value by which we multiply the gradient
        before adding to the current parameters. oldParams = oldParams +
        scaleFactor\*gradient

Users can divide their samples into subsets to run pseudo likelihood
parameter learning in "batch" mode. Assuming users have their samples
stored in a cell array of matrices, they could iterate over the cell
array as follows:

=== "Python"

    *not yet available*

=== "Java"

    ```java
    for (int i = 0; i < samples.length; i++)
        pl.learn(samples[i],scaleFactor);
    ```

=== "MATLAB"

    ```matlab
    for i = 1:length(samples)
        pl.learn(samples{i},args);
    end
    ```

When calling the learn routine, users can set the data. However, if
users want some visibility into the gradient or the numerical gradient,
they must first set the data using the setData method

=== "MATLAB"

    ```matlab
    pl.setData(samples);
    ```

Arguments:

-   samples - Takes the same form as in the learn method.

Users can retrieve the pseudo likelihood given the currently set samples
using the following code:

=== "MATLAB"

    ```matlab
    likelihood = pl.calculatePseudoLikelihood();
    ```

Return value:

-   likelihood - The log pseudo likelihood.

For debugging purposes, the user can retrieve the gradient given the
current sample set and parameter settings.

=== "MATLAB"

    ```matlab
    result = pl.calculateGradient()
    ```

Return values:

-   result - MxN matrix where M is the number of factor tables being
    learned and N is the number of weights per factor table.

For debugging purposes, the user can return a numerical gradient

=== "MATLAB"

    ```matlab
    pl.calculateNumericalGradient(table, weightIndex, delta)
    ```

Arguments:

-   table - Which table to modify

-   weightIndex - Which weight index to modify

-   delta - the delta (in the log domain) of the parameter.

## Expectation-Maximization on Directed Graphs

See the FactorGraph.baumWelch method in the API section. see
section [\[sec:FactorGraph.BaumWelch\]](#sec:FactorGraph.BaumWelch){reference-type="ref"
reference="sec:FactorGraph.BaumWelch"}
