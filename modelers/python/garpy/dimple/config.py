#  Copyright 2024 Analog Devices Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
Starts the JVM with appropiate classpath and binds import system
"""

from __future__ import annotations

import atexit
import os
from pathlib import Path
from typing import Sequence

import jpype
import jpype.imports

# for now just run from source
this_dir = Path(__file__).parent
python_root = this_dir.parent.parent
proj_root = python_root.parent.parent

dimple_jar = proj_root / "build/libs/dimple.jar"
dimple_src = proj_root / "solvers/java/src/main/java"
solvers_lib = proj_root / "solvers/lib"
dependency_jars = tuple(solvers_lib.glob("*.jar"))
conda_prefix = Path(os.environ.get("CONDA_PREFIX", "/usr/local"))
lib_jars = conda_prefix / "lib/jars"

default_jvm_args: Sequence[str] = ()
if dependency_jars and dimple_src.exists():
    # Use local jars from source if running from source
    default_classpath: Sequence[Path] = (dimple_src,) + dependency_jars
else:
    # Otherwise use installed jars (from conda package)
    default_classpath = tuple(lib_jars.glob("*.jar"))

__all__ = [
    "start",
]

def start(
    jvmargs: Sequence[str] = default_jvm_args,
    classpath: Sequence[str|Path] = default_classpath,
) -> None:
    """
    Start JVM used by jpype.

    This may only be done once!

    Arguments:
        jvmargs: command line arguments to pass to the JVM instance
        classpath: Java classpath to use (i.e. a list of jars).
            The classpath may not be changed once started!
    """
    if not jpype.isJVMStarted():
        jpype.startJVM(
            *jvmargs,
            classpath=classpath,
        )
        assert jpype.isJVMStarted()

        atexit.register(_shutdown)

def _shutdown() -> None:
    """
    Shuts down the JVM used by jpype classes.

    This will invalidate all existing jpype class instances and could
    lead to segfaults if you try to use them!

    This function is automatically invoked prior to process exit,
    so there is no reason to call it explicitly.
    """
    jpype.shutdownJVM()

