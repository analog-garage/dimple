# Python API

## Overview

This section contains reference documentation for the Python API generated
from its doc-strings. The documentation is organized by the declaring
Python module, but most interfaces can be imported through the top level
`garpy.dimple` package, e.g.:

```python
from garpy.dimple import FactorGraph, DiscreteDomain, Discrete
```

## Using the Java API directly

This API is built on top of a wrapper API based on the [JPype] library,
which directly corresponds to the underlying [Java API](../java-doc). It is possible to
use that API directly if necessary, when this Python API does not expose
the desired functionality, but is otherwise not recommended. Use of the
[JPype] based API may require conversions between Python and Java types
in some cases. In order to use the [JPype] based API, you should import
`garpy.dimple.java` prior to importing the Java wrapper classes using their
qualified Java names, in order to configure the required Java classpath, e.g.:

```python
import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.values import Value as JValue
```

You can also use the `start` function from `garpy.dimple.config` to
explicitly configure JVM arguments and classpath prior to importing.
Note this can only be done once per process and must be done before
importing any other dimple packages.

[JPype]: https://jpype.readthedocs.io/en/latest/index.html

