#  Copyright 2025 Analog Devices Inc.
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
Unit tests for JWrapper base class
"""

from __future__ import annotations

import gc
from typing import cast

from garpy.dimple.base import JWrapper, jpype_wrapper
from garpy.dimple.factorgraph import FactorGraph
from garpy.dimple.value import Value

from com.analog.lyric.dimple.model.core import FactorGraph as JFactorGraph
from com.analog.lyric.dimple.model.values import DiscreteValue as JDiscreteValue
from com.analog.lyric.dimple.matlabproxy import PLogger, PDimpleEventLogger

@jpype_wrapper(PDimpleEventLogger)
class TestDimpleEventLogger(JWrapper[PDimpleEventLogger]):
    pass


def test_wrapper_type_for() -> None:
    """
    Unit test for JWrapper.wrapper_type_for
    """
    assert JWrapper.wrapper_type_for(str) == None # noqa: E711
    assert JWrapper.wrapper_type_for(JFactorGraph) == FactorGraph
    assert JWrapper.wrapper_type_for(JDiscreteValue) == Value


def test_for_jobj() -> None:
    """
    Unit test for JWrapper.for_jobj constructor
    """
    jfg = JFactorGraph()
    fg = cast(FactorGraph, JWrapper.for_jobj(jfg))
    assert isinstance(fg, FactorGraph)
    assert fg.jobj is jfg
    assert JWrapper.for_jobj(jfg) is fg

    # Remove old object and force GC to clear the weak cache
    old_id = id(fg)
    del fg
    gc.collect()

    fg2 = JWrapper.for_jobj(jfg)
    assert id(fg2) != old_id

    # We don't provide python wrappers for MATLAB implementation
    # classes. This should just fall back to generic JWrapper
    jplogger = PLogger.INSTANCE
    plogger = JWrapper.for_jobj(jplogger)
    assert plogger.__class__ == JWrapper
    assert plogger.jobj is jplogger

    plogger = PDimpleEventLogger() # type: ignore
    logger = JWrapper.for_jobj(plogger)
    assert isinstance(logger, TestDimpleEventLogger)
    assert logger.jobj is plogger
