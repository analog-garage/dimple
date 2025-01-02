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
Unit tests for garpy.dimple.domain module
"""

from __future__ import annotations

import math
from typing import cast

import pytest

from garpy.dimple.domain import (
    Domain,
    DiscreteDomain,
    JDomain,
    RealDomain,
)

def check_domain(domain: Domain) -> None:
    """
    Check invariants on domain
    """
    assert domain.jobj is domain.jdomain
    assert isinstance(domain.jdomain, JDomain)
    assert domain == domain  # noqa: PLR0124
    assert domain != "hi"
    assert domain.is_discrete != domain.is_real

    assert domain.is_discrete == isinstance(domain, DiscreteDomain)

    if isinstance(domain, DiscreteDomain):
        discrete = cast(DiscreteDomain, domain)
        assert len(discrete) > 0
        assert len(discrete) == discrete.size
        n = 0
        for i, elt in enumerate(discrete):
            n += 1
            assert elt in discrete
            assert elt == discrete[i] # noqa: PLR1736
        assert n == discrete.size

    if isinstance(domain, RealDomain):
        real = cast(RealDomain, domain)
        assert real.lower_bound <= real.upper_bound
        assert real in real # noqa: PLR0124
        assert real.subset_of(real)
        assert real.upper_bound in real
        assert real.lower_bound in real
        assert "foo" not in real

def test_discrete_domain() -> None:
    """Unit test for DiscreteDomain"""
    bitd = DiscreteDomain.bit()
    check_domain(bitd)

    assert 0 in bitd
    assert 1 in bitd

    assert bitd == DiscreteDomain.bit()
    assert bitd.size == 2
    check_domain(bitd)

    boold = DiscreteDomain.bool()
    check_domain(boold)
    assert boold != bitd
    assert boold.size == 2
    assert hash(boold) != hash(bitd)
    assert True in boold
    assert False in boold
    check_domain(boold)

    bitd2 = DiscreteDomain.range(2)
    assert bitd2.size == 2
    assert bitd2 == bitd
    check_domain(bitd2)

    bitd3 = DiscreteDomain.create(0,1)
    assert bitd3.size == 2
    assert list(bitd3) == [0,1]
    assert bitd3 != bitd # different underlying implementation despite same values
    check_domain(bitd3)

    evend = DiscreteDomain.range(2,11,2)
    assert list(evend) == [2,4,6,8,10]
    check_domain(evend)
    assert evend.is_discrete

def test_real_domain() -> None:
    """
    Unit tests for RealDomain
    """
    R = RealDomain.unbounded()
    check_domain(R)
    assert not R.is_bounded
    assert R.upper_bound == math.inf
    assert R.lower_bound == -math.inf

    Rplus = RealDomain.non_negative()
    check_domain(Rplus)
    assert Rplus.lower_bound == 0.0
    assert Rplus.upper_bound == math.inf
    assert Rplus.is_bounded
    assert Rplus != R
    assert Rplus.subset_of(R)
    assert hash(Rplus) != hash(R)
    assert 100 in Rplus
    assert -100 not in Rplus

    Rneg = RealDomain.non_positive()
    check_domain(Rneg)
    assert Rneg.is_bounded
    assert Rneg.lower_bound == -math.inf
    assert Rneg.upper_bound == 0.0
    assert Rplus != Rneg
    assert Rplus not in Rneg
    assert Rneg in R
    assert R not in Rneg
    assert -100 in Rneg

    R1 = RealDomain.create(-1,1)
    check_domain(R1)
    assert R1.is_bounded
    assert R1 in R
    assert R1 not in Rplus
    assert R1.lower_bound == -1.0
    assert R1.upper_bound == 1.0
    assert 0.5 in R1

    R2 = RealDomain.create(-2,2)
    check_domain(R2)
    assert R1 in R2
    assert R2 not in R1
    assert R1 != R2
    assert hash(R1) != hash(R2)
