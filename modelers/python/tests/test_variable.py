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
Test garpy.dimple.variable module
"""

from __future__ import annotations

import pytest

import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.variables import Discrete as JDiscrete
from garpy.dimple import NormalParameters

from garpy.dimple.domain import DiscreteDomain
from garpy.dimple.message import DiscreteMessage
from garpy.dimple.variable import Discrete

def check_discrete(d: Discrete) -> None:
    """
    Check invariants on Discrete variable
    """
    assert isinstance(d.jvariable, JDiscrete)
    assert d.jvariable is d.jobj

    belief = d.belief
    assert len(belief) == len(d.domain)
    assert sum(belief) == pytest.approx(1.0, abs=0.02)

def test_discrete() -> None:
    """
    Test Discrete class
    """

    D6 = DiscreteDomain.range(6)

    d6 = Discrete(D6)
    check_discrete(d6)
    assert d6.domain == D6

    d6.prior = [.1,.1,.2,0,.3,.3]
    check_discrete(d6)
    assert isinstance(d6.prior, DiscreteMessage)
    assert d6.prior.stores_weights
    assert d6.belief == pytest.approx((.1,.1,.2,0.0,.3,.3))

    d6.prior = NormalParameters(mean=3.5, std=1)
    assert isinstance(d6.prior, DiscreteMessage)
    assert not d6.prior.stores_weights
    check_discrete(d6)
