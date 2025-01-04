# Options <a name="Options"></a>

Dimple provides an option mechanism used to configure the runtime
behavior of various aspects of the system. This section describes the
Dimple option system and how it is used. Only the most widely useful
parts of the API are described here: for complete documentation of the
Java API please consult the HTML Java API documentation. Individual
options are described in more detail later in this document.

*Options were introduced in version 0.07 and replace earlier mechanisms
based on solver-specific method calls. Those methods are now deprecated
and will be removed in a future release.*

## Option Keys

Options are key/value pairs that can be set on Dimple factor graph,
variable, or factor objects to configure their behavior. An option key
uniquely identifies an option, along with its type and default value.

=== "Python"

    In the Python API, opton keys are singleton instances of the `OptionKey`
    class that wrap underlying Java option keys and that are declared as
    class members of public classes. To refer to an option key, you only need to import the
    class in which it is declared and refer to the field, e.g:

    ```python
    from garpy.dimple import SolverOptions
    ...
    graph.set_option(BPOptions.iterations, 12)
    ```

=== "Java"

    In the Java API, option keys are singleton instances of the `IOptionKey`
    interface that are declared as public static final fields of a publicly
    accessible class. To refer to an option key, you only need to import the
    class in which it is declared and refer to the field, e.g:

    ```java
    import com.analog.lyric.dimple.options.SolverOptions;
    ...
    graph.setOption(BPOptions.iterations, 12);
    ```

    The `IOptionKey` interface defines a number of methods that can be used to
    query the `name()`, `type()`, and `defaultValue()` as well as methods for
    converting and validating values for that option. Users should have
    little reason to invoke any of these directly. Details may be found in
    the [Java API documentation](../java-doc/).

=== "MATLAB"

    In the MATLAB API, option keys are represented by unique strings of the
    form '*OptionClass.optionName*' (a complete list of supported option
    keys is returned by the dimpleOptions() function). For instance:

    ```matlab
    graph.setOption('BPOptions.iterations', 12);
    ```

    Specifying a string that does not correspond to a known option key will
    result in a runtime error when key is used to set or look up an option
    value.[^1]

## Setting Options

Options may be set on any `FactorGraph`, `Factor`, or `Variable` object or
their solver-specific counterparts. Options may also be set on the
`DimpleEnvironment` object, which is described in more detail below.
(Options may in fact be set on any object that implements the local
option methods of the IOptionHolder interface, but this should not
matter to most Dimple users.) Options set on graphs will be applied to
all factors, variables, and subgraphs contained in the graph unless
overridden on one of those members. Likewise options set on a model
object will be applied to an associated Solver object to it unless
overridden directly in the Solver object. In most cases, it should not
be necessary to set options directly on Solver objects.

=== "Python"

    Options can be set either using the `set_option` method (defined in the
    `OptionHolder` interface) or through the set method of the option key
    itself. For example:

    ```python
    # These both do the same thing:
    graph.set_option(BPOptions.damping, .9);
    BPOptions.damping.set(graph, .9);
    ```

    The `set_option` method will also accept a string such
    as `'BPOptions.damping'`, but we recommend using the 
    option key constants for better type checking.

=== "Java"

    Options can be set either using the setOption method (defined in the
    IOptionHolder interface) or through the set method of the option key
    itself. For example:

    ```java
    // These both do the same thing:
    graph.setOption(BPOptions.damping, .9);
    BPOptions.damping.set(graph, .9);
    ```

    Some option keys may define additional set methods that may be more
    convenient to use than the setOption method:

    ```python    
    // These both do the same thing:
    factor.setOption(BPOptions.nodeSpecificDamping,
        new OptionDoubleList(.7, .8, .9));
    BPOptions.nodeSpecificDamping.set(factor, .7, .8, .9);
    ```

=== "MATLAB"

    Options can be set using the setOption method of the Node class. For
    example:

    ```matlab
    graph.setOption('BPOptions.damping', .9);
    ```

    When applied to an array object the option will be set on each. For
    example:

    ```matlab
    vars = Real(2,2);
    vars.setOption('BPOptions.damping', .9);
    ```

    In this case, to apply a distinct value to each element of the array,
    the values are specified in a cell-array of the same dimensions as the
    array object:

    ```matlab
    vars = Real(2,2);
    vars.setOption('BPOptions.damping', {.7 .8; .6 .9});
    ```

    Multiple options may be set at the same time using the setOptions
    method. This takes arguments in one of the following forms:

    -  A comma-separated list containing alternating option keys and
        values.

        ```matlab
        nodes.setOptions('BPOptions.iterations', 10, ...
                          'BPOptions.damping' , .9);
        ```

    -  A vector cell array containing alternating option keys and values.

        ```matlab
        nodes.setOptions({'BPOptions.iterations', 10, ...
                          'BPOptions.damping' , .9});
        ```

    -   A nx2 cell array where each row contains a key and value.

        ```matlab
        nodes.setOptions({'BPOptions.iterations', 10; ...
                          'BPOptions.damping' , .9});
        ```

    -   A cell array with dimensions matching the dimensions of the left
        hand side where each cell contains one of the above two forms.

        ```matlab
        options = cell(2,2);
        options{1,1} = {'BPOptions.iterations', 10; ...
                        'BPOptions.damping', .85};
        options{2,2} = {'BPOptions.iterations', 12};
        nodes.setOptions(options);
        ```

All of these methods will ensure that the type of the option values are
appropriate for that key and may also validate the value. For instance
when setting the `BPOptions.damping` option, the value must be a double in
the range from 0.0 to 1.0. If a value is not valid for its key an
`OptionValidationException` will be thrown.

Options may be unset on any object on which they were previously set
using the unset method:

=== "Python"

    ```python
    graph.unset_option(BPOptions.damping)
    ```

    or the unset method on the option key can be used:

    ```python
    BPOptions.damping.unset(graph);
    ```

=== "Java"

    ```java
    graph.unsetOption(BPOptions.damping);
    ```

    or the unset method on the option key can be used:

    ```java
    BPOptions.damping.unset(graph);
    ```

=== "MATLAB"

    ```matlab
    graph.unsetOption('BPOptions.damping');
    ```

All local options may be unset on an object at once using:

=== "Python"

    ```python
    graph.clear_local_options()
    ```

=== "Java"

    ```java
    graph.clearLocalOptions();
    ```

=== "MATLAB"

    ```matlab
    graph.clearLocalOptions();
    ```

## Looking up Option Values

There are a number of methods for retrieving option values from objects
on which they can be set. Most users will only need to use these to
debug their option settings.

The option value that applies to a given object is determined
hierarchically, based on an order that depends on the structure of the
graph. An option value specified at any level applies to all objects
below it in the hierarchy, unless specifically specified for an object
lower in the hierarchy. At any level, the option value overrides the
value specified at a higher level. When querying an object to determine
what option value will be used, the hierarchy is searched in the
following order[^2]:

1.  Search the object itself.

2.  If the object is a solver object, next look at the corresponding
    model object.

3.  If the object has a parent graph, then recursively search that
    graph, otherwise the `DimpleEnvironment` for that object will be
    searched (there is usually only one environment).

=== "Python"

    There are a number of methods defined on the `OptionKey` and
    `DimpleOptionHolder` classes that can be used to lookup the value of the
    option for that object using the lookup algorithm that was just
    described:

    -   `OptionKey.get(obj)` and `DimpleOptionHolder.get_option(key)` - both return
        the value of the option for the given object. There is a `default`
        keyword option that controls whether to return a default value or None.
        If not specified, a default value will be returned if available.

        ```python
        # These are equivalent to each other
        damping1 = graph.get_option(BPOptions.damping, default=False)
        damping2 = BPOptions.damping.get(graph, default=False)

        # as are are these:
        damping3 = graph.getOptionOrDefault(BPOptions.damping)
        damping4 = BPOptions.damping.getOrDefault(graph)
        ```

    There are also methods for querying the values of only those options
    that are set directly on an object:

    -   `DimpleOptionHolder.get_local_option(key)` - returns the value of the option
        or else null if not set directly on the object.

        ```python
        damping = graph.get_local_option(BPOptions.damping)
        ```

    -   `DimpleOptionHolder.get_local_options()` - returns a dictionary
        that provides a copy of the option settings for that object:

        ```python
        # Print options set on node.
        for key, val in node.get_local_options().items():
            print(f"{key} = {value}")
        ```

=== "Java"

    There are a number of methods defined on the `OptionKey` and
    `IOptionHolder` interfaces that can be used to lookup the value of the
    option for that object using the lookup algorithm that was just
    described:

    -   `IOptionKey.get(obj)` and `IOptionHolder.getOption(key)` - both return
        the value of the option for the given object or else null if not
        set.

        ```java
        // These are equivalent:
        var damping1 = graph.getOption(BPOptions.damping);
        var damping2 = BPOptions.damping.get(graph);
        ```

    -   `IOptionKey.getOrDefault(obj)` and
        `IOptionholder.getOptionOrDefault(key)` - return the default value
        defined by the key instead of null when the option has not been set
        anywhere.

        ```java
        // These are equivalent:
        var damping1 = graph.getOptionOrDefault(BPOptions.damping);
        var damping2 = BPOptions.damping.getOrDefault(graph);
        ```

    There are also methods for querying the values of only those options
    that are set directly on an object:

    -   `IOptionHolder.getLocalOption(key)` - returns the value of the option
        or else null if not set directly on the object.

        ```java
        Double damping = graph.getLocalOption(BPOptions.damping);
        ```

    -   `IOptionHolder.getLocalOptions()` - returns a read-only collection
        that provides a view of the option settings for that object in the
        form of `IOption` objects.

        ```java
        // Print options set on node.
        for (IOption<?> option : node.getLocalOptions())
        {
            System.out.format("%s = %s\n", option.key(), option.value());
        }
        ```

=== "MATLAB"

    There are two methods for looking up option values:

    -   `getOption(key)` - returns the values of the specified option as
        determined by the above lookup rules or else the option's default
        value if not set. If invoked on an object array then this will
        return a cell array containing the values. For example:

        ```matlab
        >>> graph.getOption('BPOptions.damping')
        ans =
            0.9000
            
        >>> vars.getOption('BPOptions.damping')
        ans =
            [0.7000]    [0.6000]
            [0.8000]    [0.9000]
        ```

    -   `getLocalOptions()` - returns a cell array containing keys and values
        of options that are set directly on that object in the same format
        accepted by the setOptions method described in the previous section.
        For example:
    
        ```matlab
        >>> graph.getLocalOptions()
        ans =
            'BPOptions.damping'    [0.9000]
            'BPOptions.iterations'     10
        ```

## Option Initialization

While option values are visible as soon as they are set on an object,
they may not take effect until later because internal objects that are
affected by the change may have cached state based on the previous
settings, or may not yet exist. The documentation for individual options
should indicate when changes to the settings are incorporated, but in
most cases that will happen when the initialize() method is called on
the affected object. Since this happens automatically when invoking the
FactorGraph.solve() method, users will often not have to be concerned
with this detail. But if you performing other operations, such as
directly calling FactorGraph.iterate(), then you will probably need to
invoke FactorGraph.initialize() for modified option settings to take
effect.

## Setting Defaults on the Dimple Environment

Sometimes you may want to apply the same default option settings across
multiple graphs. While you can simply set the options on all of the
graphs individually, another choice is to set it on the
DimpleEnvironment object. The DimpleEnvironment holds shared state for a
Dimple session. Typically there will be only one instance of this class.
Because the environment is the last place searched for option lookup,
you can use it as a place to set default values of options to override
those defined by the option keys.

You can obtain a reference to the active global environment using the
static `DimpleEnvironment.active()` method: and set default option values
on it. For instance, to globally enable multithreading for all graphs,
you could write:

=== "Python"

    ```python
    env = DimpleEnvironment.active()
    env.set_option(SolverOptions.enableMultithreading, True)
    ```

=== "Java"

    ```java
    var env = DimpleEnvironment.active();
    env.setOption(SolverOptions.enableMultithreading, true);
    ```

=== "MATLAB"

    ```matlab
    env = DimpleEnvironment.active();
    env.setOption('SolverOptions.enableMultithreading', true);
    ```

[^1]: Internally option keys are represented using singleton Java
    objects, which are looked up by the string keys. If you have a
    reference to the Java IOptionKey object, you can use it in place of
    the string throughout this interface. Consult the Java version of
    this manual for further details.

[^2]: The algorithm is actually slightly more complicated than this but
    the details should only matter to those implementing custom factors
    or solvers. For details see the documentation for
    EventSourceIterator in the HTML Java API documentation.
