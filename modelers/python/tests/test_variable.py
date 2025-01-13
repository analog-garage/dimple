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

import timeit
from functools import partial

import pytest

import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.variables import Discrete as JDiscrete
from garpy.dimple import NormalParameters

from garpy.dimple.domain import DiscreteDomain
from garpy.dimple.factorgraph import FactorGraph
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

    normal = NormalParameters(mean=3.5, std=1)
    d6.prior = normal
    assert isinstance(d6.prior, DiscreteMessage)
    assert not d6.prior.stores_weights
    energies = d6.prior_energies
    for i, val in enumerate(D6):
        assert energies[i] == pytest.approx(normal.eval_energy(val))
    check_discrete(d6)

@pytest.mark.skip
def test_discrete_creation_cost() -> None:
    D6 = DiscreteDomain.range(6)

    def _create_discretes(n: int) -> None:
        discretes = [Discrete(D6) for _ in range(n)]

    for n in (1,10,100,1000):
        s = timeit.timeit(partial(_create_discretes,n), number=100)
        print(f"create {n}: {s/100}/s")

    xxx = [0]
    def _create_and_add(fg: FactorGraph, n: int) -> None:
        discretes = [Discrete(D6) for _ in range(n)]
        for d in discretes:
            i = xxx[0]
            xxx[0] = i+1
            d.name = f"foo{i}"
        fg.add_variables(*discretes)

    for n in (1,10,100,1000):
        fg = FactorGraph()
        s = timeit.timeit(partial(_create_and_add,fg,n), number=100)
        print(f"create and add {n}: {s/100}/s")

