# API Guides

The following section describes the functions, classes, properties,
options and methods that comprise the Dimple API for Python, Java and MATLAB. 
In some cases, API documentation may also be obtained using either the MATLAB
'help' or 'doc' command with the name of the element of interest.

The following section describes the classes, properties, options and
methods that comprise the Dimple API for Java. Standard HTML-based Java
API documentation is also available from the [Java API][javadoc] tab
above. (Note that you will need to use browser back to navigate back
to this documentation.)

## Language-specific notes

=== "Python"

    The original Java and MATLAB APIs of Dimple use camel-case
    naming conventions and the MATLAB API uses capitalized property
    names. The python API, on the other hand, sticks to standard
    python naming conventions with lower-case words separated by
    underscores for variable and method names. Camel case is only
    used for class names:

    ```python
    fg.add_factor(my_func, var1, var2)
    fg.num_iterations = 4
    ```

    However, if you use the underlying [JPype] wrapper objects,
    then those will retain the Java method names:

    ```python
    if fg.jtable.isDirected():
        ...
    ```

=== "Java"

    Note that in order to maintain compatibility with the Python and MATLAB 
    APIs, the term "property" is used to refer to elements of the
    API even though the Java language does not support the concept of
    implicit getter/setter method calls using field access syntax. When this
    document refers to a "property" `Foo` with a given `Type`, it implies
    there is a setter and getter method of the form:

    ```java
    Type getFoo();
    void setFoo(Type value);
    ```

[javadoc]: ../java-doc