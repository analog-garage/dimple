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
Unit tests for garpy.dimple.factortable module
"""

from __future__ import annotations

import math

import pytest

from garpy.dimple.domain import DiscreteDomain
from garpy.dimple.factortable import FactorTable, FFuncType

def test_factor_table() -> None:
    """
    Unit test for FactorTable class
    """
    d4 = DiscreteDomain.range(4)
    d6 = DiscreteDomain.range(6)

    sparse = FactorTable.create_sparse(d4,d6)
    assert sparse.is_sparse
    assert sparse.sparse_size == 0
    assert sparse.ndim == 2
    assert sparse.shape == (4,6)
    assert sparse.domains == (d4,d6)

    assert sparse.energies[0,1] == math.inf
    assert sparse.weights[2,4] == 0.0

    sparse.weights[1,4] = .12
    sparse.weights[2,2] = 0.5
    assert sparse.sparse_size == 2
    assert sparse.weights[1,4] == pytest.approx(0.12)
    assert sparse.unravel_sparse_index(0) == (2,2)
    assert sparse.unravel_sparse_index(1) == (1,4)
    ji33 = sparse.joint_index(3,3)
    assert sparse.unravel_joint_index(ji33) == (3,3)
    sparse.energies[ji33] = 0.25
    assert sparse.energies[3,3] == 0.25
    assert sparse.sparse_size == 3

    sparse.set_sparse_weights(
        [[1,2],[3,5]],
        [0.2, 0.7]
    )
    assert sparse.sparse_size == 2
    assert sparse.weights[1,2] == 0.2
    assert sparse.weights[3,5] == 0.7
    assert sparse.weights[1,4] == 0.0

