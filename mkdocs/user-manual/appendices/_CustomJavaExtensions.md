# Creating custom Dimple extensions in Java <a name="userJava"></a>

## Creating a Custom Factor Function

There are some cases in which it is desirable to add a custom factor
function written in Java rather than MATLAB. Specific cases where this
is desirable are:

-   A factor function is needed to support continuous variables that is
    not available as a Dimple built-in factor.

-   A MATLAB factor function runs too slowly when creating a factor
    table, where a Java implementation may run more quickly.

When a factor function function is needed to support continuous
variables that is not available as a Dimple built-in factor, then it is
necessary to create a custom factor function.

To create a custom factor function, you must create a Java class that
extends the Dimple `FactorFunction` class. When extending the
`FactorFunction` class, the following method must be overwritten:

-   `evalEnergy`: Evaluates a set of input values and returns an energy
    value (negative log of a weight value).

The user may extend other methods, as appropriate:

-   Constructor: If a constructor is specified (for example, to pass
    constructor arguments), it must call the constructor of the super
    class.

-   `isDirected`: Indicates whether the factor function is directed. If
    directed, then there are a set of directed outputs for which the
    marginal distribution for all possible input values is a constant.
    If not overridden, this is assumed false.

-   `getDirectedToIndices`: If a factor function is directed, indicates
    which edge indices are the directed outputs (numbering from zero),
    returning an array of integers. There are two forms of this method,
    which may be used depending on whether the set of directed outputs
    depends on the number of edges in the factor that uses this factor
    function (many factor functions support a variable number of edges).
    If `isDirected` is overridden and can return `true`, then this
    method must also be overridden.

-   `isDeterministicDirected`: Indicates whether a factor function is
    both directed and deterministic. If deterministic and directed, then
    it is in the form of a deterministic function such that for all
    possible settings of the input values there is exactly one output
    value the results in a non-zero weight (or, equivalently, a
    non-infinite energy)[^1]. If not overridden, this is assumed false.

-   `evalDeterministic`: If a factor function is directed and
    deterministic, this method evaluates the values considered the
    inputs of the deterministic function and returns the resulting
    values for the corresponding outputs. Note that these are not the
    weight or energy values, but the actual values of the associated
    variables that are considered outputs of the deterministic function.
    If `isDeterministicDirected` is overridden and can return `true`,
    then this method must also be overridden.

-   `eval`: Evaluates a set of input values and returns a weight instead
    of an energy value. Overriding this method would only be useful if
    implementing this method can be done significantly more
    computationally efficiently than the default implementation, which
    calls evalEnergy and then computes $\exp(-energy)$.

The following is a very simple example of a custom factor function:

```java
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;

/*
 * This factor enforces equality between all variables and weights
 * elements of the domain proportional to their value
 */
public class BigEquals extends FactorFunction
{   
    @Override
    public final double evalEnergy(Value[] input)
    {
        if (input.length == 0)
            return 0;
            
        Value firstVal = input[0];
            
        for (int i = 1; i < input.length; i++)
            if (!input[i].valueEquals(firstVal))
                return Double.POSITIVE_INFINITY;
            
        return 0;
    }
}
```

## Creating a custom proposal kernel <a name="CreatingACustomProposalKernel"></a>

In some cases, it may be useful to add a custom proposal kernel when
using the Gibbs solver with a Metropolis-Hastings sampler. In
particular, since the *block* Metropolis-Hastings sampler does not have
a default proposal kernel, it is necessary to add a custom proposal
kernel in this case.

To create a custom proposal kernel, you must create a Java class that
implements either the Dimple `IProposalKernel` interface in the case of
a single-variable proposal kernel, or the `IBlockProposalKernel`
interface in the case of a block proposal kernel.

These interfaces define the following methods that must be implemented:

-   `next` This method takes the current value(s) of the associated
    variable(s) along with the corresponding variable domain(s), and
    returns a proposal. The proposal object returned (either a
    `Proposal` object for a single-variable proposal or a
    `BlockProposal`: object for a block proposal) includes both the
    proposed value(s) as well as the forward and reverse proposal
    probabilities (the negative log of these probabilities).

-   `setParameters`: Allows the class to include user-specified
    parameters that can be set to modify the behavior of the kernel.
    This method must be implemented but may be empty if no parameters
    are needed.

-   `getParameters`: Returns the value of any user-specified parameters
    that have been specified. This method must be implemented but may
    return null if no parameters are needed.

## Compiling Dimple extensions in Java

The new class must be compiled to class files, or optionally create a
jar file. If using Eclipse, users can simply create a new project,
create the new class, and the .class files will be created
automatically.

## Adding Java binary to MATLAB Path

In MATLAB, you must use the javaaddpath call to add the Java class or
jar files to the javaclasspath. For example:

```matlab
    javaaddpath('<path to my project>/MyFactorFunctions/bin');
```

or

```matlab
    javaaddpath('<path to the jar>/myjar.jar');
```

[^1]: The indication that a factor function is deterministic directed is
    used by the Gibbs solver, and is necessary for such factor functions
    to work when using the Gibbs solver.
