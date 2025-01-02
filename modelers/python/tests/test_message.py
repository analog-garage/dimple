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
Unit tests for garpy.dimple.message module
"""

from __future__ import annotations

import math

import pytest

from garpy.dimple.message import ParameterizedMessageBase, NormalParameters
from garpy.dimple.value import DataRepresentationType


def check_message(msg: ParameterizedMessageBase) -> None:
    assert msg.representation_type is DataRepresentationType.MESSAGE
    assert msg == msg # noqa: PLR0124
    assert not msg != msg  # noqa: PLR0124
    assert msg != "not a message"
    assert isinstance(str(msg), str)

    # just test to make sure we pass through values from Java imple
    assert msg.has_deterministic_value == msg.jobj.hasDeterministicValue()
    assert msg.normalization_energy == msg.jobj.getNormalizationEnergy()

    assert msg.compute_KL_divergence(msg) == 0.0

    msg2 = msg.clone()
    assert isinstance(msg, msg.__class__)
    assert msg2 is not msg
    assert msg == msg2

    msg2.set_null()
    assert msg2.is_null
    if not msg.is_null:
        assert msg != msg2

    msg2.set_from(msg)
    msg2.normalization_energy = msg.normalization_energy
    assert msg2 == msg


def check_normal_parameters(msg: NormalParameters) -> None:
    """Test invariants on NormalParmeters"""
    check_message(msg)
    assert msg.variance == pytest.approx(msg.standard_deviation ** 2)
    if msg.precision != 0.0:
        assert msg.variance == pytest.approx(1.0 / msg.precision)
    else:
        assert msg.variance == math.inf

    assert repr(msg) == f"NormalParameters({msg.mean}, {msg.precision})"

    msg2 = msg.clone()
    msg2.mean = 1.42
    assert msg2.mean == 1.42

    msg2.precision = .25
    assert msg2.precision == 0.25
    assert msg2.variance == 4.0

    msg2.variance = 2.0
    assert msg2.precision == 0.5

    msg2.standard_deviation = 0.5
    assert msg2.variance == 0.25

    msg2.set_uniform()
    assert msg2.mean == 0.0
    assert msg2.precision == 0.0



def test_normal_parameters() -> None:
    """
    Unit test for NormalParameters
    """
    np = NormalParameters()
    check_normal_parameters(np)
    assert np.mean == 0.0
    assert np.precision == 1.0

    np1 = NormalParameters(0.0, 1.0)
    assert np1.mean == np.mean
    assert np1.precision == np.precision
    assert np1.normalization_energy == np.normalization_energy
    assert np1 == np
    assert np1.precision == 1.0
    assert np1.mean == 0.0

    np2 = NormalParameters(mean=1.0, precision=2.0)
    check_normal_parameters(np2)
    assert np2 != np1
    assert np2.precision == 2.0
    assert np2.variance == 0.5

    np3 = NormalParameters(mu=1.0, sigma=2.0)
    check_normal_parameters(np3)
    assert np3.mean == 1.0
    assert np3.standard_deviation == 2.0

    np3a = NormalParameters(mean=1.0, std=2.0)
    assert np3a == np3

    np4 = NormalParameters(mean=-1, variance=2.0)
    check_normal_parameters(np4)
    assert np4.mean == -1.0
    assert np4.variance == 2.0
