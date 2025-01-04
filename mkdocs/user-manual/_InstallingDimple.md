# Installing Dimple

Dimple is no longer supported externally, but can be installed internally
using conda or built from source.

## Installing from conda

Note that the current conda package is configured to depend on a Java JDK
installed in the same conda environment and includes it as a dependency.
It includes the Java solver and the python interface.

```bash
conda install -c garage-conda-local garpy-dimple
```

## Building from source

The build process is managed by the gradle build tool, this an other
dependencies can be pulled in by creating a development environment
from the provided `environment.yml` file:

```bash
conda env create -f environment.yml
```

then use gradle tasks to build the system. To simply build the system:

```bash
gradle assemble
```

to build and test:

```bash
gradle build
```

to see what build tasks are available:

```bash
gradle tasks
```

