# Development Note

## Background

Dimple was the key piece of software in use in the early days of Lyric Labs
and served as the front-end to the DARPA-funded custom *GP5* processor 
for accelerating belief propagation algorithms on discrete factor graphs.
Also at that time, MATLAB was the prevailing tool used by researchers
rather than Python, and therefore Dimple emphasized its MATLAB interface
and much of the documentation was written from the perspective of that
API. The original documentation was written in LaTeX and published in the
form of a User Manual in PDF format.

With the success of deep neural networks, our researchers mostly moved away 
from working with graphical models and we therefore stopped actively working on and 
supporting Dimple since 2015.

Also, in the intervening time our research code has almost entirely moved
away from MATLAB and to Python.

## Current effort

The current support effort will provide a minimal python interface based on the 
[JPype] library so that we can use Dimple's existing capabilities from Python code.

Current goals include:

* The ability to use Dimple from python 3.12 or later
* Easy installation via conda (including Java JVM)
* Easy to navigate mkdocs-based documentation.
* Support for discrete BP on simple factor graphs.

Current non-goals include:

* MATLAB support: We have not tried using Dimple with recent versions of
    MATLAB and have no intention of spending time trying to make it work
    lacking a compelling use case. It may still work, so we will leave
    the existing documentation in place for now.

* Support for nested and rolled-up graphs.

* Support for parameter learning.

* Support for existing LP solver

## Note on JPype implementation

[JPype] can automatically generate Python wrapper classes for
Java objects. After JPype has been configured for Dimple, these 
can be loaded by importing using the fully qualified java names.
The necessary configuration (involving setting the appropriate
Java classpaths) can be done simply by importing `garpy.dimple.java`,
e.g.:

```python
import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.values import Value as JValue
```

These wrapper objects may be used directly in many cases, and it is
possible to use most of the Java Dimple API through this mechanism,
but there are some subtle type conversion issues and the resulting
API also does not follow the usual python idioms. Therefore, we add
an additional layer of wrapper class. This not only provides a more
natural experience for python users, but also allows us to potentially
provide a pure python implementation for some solvers if we should
want to.

Most of our Python API classes are derived from the `JWrapper`
class, and typically just contain a single reference to the
JPype wrapper class. Given an existing JPype object, we can
construct the corresponding wrapper type using:

```python
obj = JWrapper.for_jobj(jobj)
```

This constructor will walk through the superclasses of jobj
until it finds the closes corresponding wrapper class and will
return that. This constructor also memoizes the result and will
return the same wrapper object for a previously encountered jobj.
(*Note that we have observed that JPype may return multiple wrapper
instances for the same underlying Java object so you cannot use
`is` comparisons to test for the same Java object*)

Our wrapper classes are all declared using the pattern:

```python
@jpype_wrapper(JClass)
class OurClass([Superclass], JWrapper[JClass]):
    ...
```

The `@jpype_wrapper` decorator registers the association so that
`JWrapper.for_jobj` can return the appropriate wrapper type.

These wrapper classes generally do not contain any extra state
other than the jobj pointer and only provide properties and
methods for working with the state.

## Note on the documentation

We converted the existing documentation to Markdown format
using the pandoc program and have arranged the resulting 
files in a mkdocs site. Pandoc did a decent job in the conversion
but additional manual tweaking is still required and is still
in the process of being done.

The original documentation contained conditional blocks for
MATLAB or Java examples. These were originally processed by
LaTeX compiler to generate separate manuals for Java and MATLAB.
For this documentation, we instead turn those blocks into
tabbed content sections so that users can easily toggle between viewing
Python, Java and MATLAB content.

Until the conversion is done, the text may contain MATLAB and Java
content mixed without clear labeling. And we also may not have
Python examples for all cases, either because we have not yet provided
support in python or because we have simply not written the python
example yet.

[JPype]: https://jpype.readthedocs.io/en/latest/index.html
