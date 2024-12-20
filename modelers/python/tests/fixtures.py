from __future__ import annotations

from typing import Generator

import jpype
import pytest

from garpy.dimple.factorgraph import FactorGraph
from garpy.dimple.variable import Variable

@pytest.fixture(
    scope="session",
    autouse=True,
)
def dimple_started() -> None:
    import garpy.dimple.java
    assert jpype.isJVMStarted()

class FactorGraphTester:
    def check_graph(self, graph: FactorGraph) -> None:
        for var in graph.get_variables_list():
            assert isinstance(var, Variable)
            assert graph.get_variable(var.uuid) == var
            assert graph.get_variable(var.global_id) == var
            assert graph.get_variable(var.name) == var
            if var.graph == graph:
                assert graph.get_variable(var.local_id) == var

            for factor in var.siblings:
                assert var in factor.siblings

@pytest.fixture
def graph_tester() -> Generator[FactorGraphTester, None, None]:
    yield FactorGraphTester()
