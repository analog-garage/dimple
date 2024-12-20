from __future__ import annotations

import enum
from typing import Iterator, cast
from uuid import UUID

from jpype import JObject

import garpy.dimple.java # noqa

from java.util import UUID as JUUID # type: ignore
from com.analog.lyric.dimple.model.core import FactorGraph as JFactorGraph
from com.analog.lyric.dimple.factorfunctions.core import TableFactorFunction as JTableFactorFunction
from com.analog.lyric.dimple.model.values import Value as JValue

from com.analog.lyric.dimple.solvers.gibbs import GibbsSolver as JGibbsSolver
from com.analog.lyric.dimple.solvers.junctiontree import JunctionTreeSolver as JJunctionTreeSolver
from com.analog.lyric.dimple.solvers.junctiontreemap import JunctionTreeMAPSolver as JJunctionTreeMAPSolver
from com.analog.lyric.dimple.solvers.minsum import MinSumSolver as JMinSumSolver
from com.analog.lyric.dimple.solvers.sumproduct import SumProductSolver as JSumProductSolver

from .base import (
    Node, ValueType, jpype_wrapper, JWrapper
)
from .factor import Factor
from .variable import JVariable, Variable

from .factor import FactorFunction, FactorFuncSignature, FFuncType, JFactorFunction
from .factortable import FactorTable

__all__ = [
    "FactorGraph",
    "JFactorGraph",
]

class SolverType(enum.StrEnum):
    sum_product = "sum_product"
    min_sum = "min_sum"
    gibbs = "gibbs"
    junction_tree = "junction_tree"
    junction_tree_MAP = "junction_tree_MAP"

_solver_factory_table: dict[SolverType, type] = {
    SolverType.gibbs : JGibbsSolver,
    SolverType.junction_tree : JJunctionTreeSolver,
    SolverType.junction_tree_MAP : JJunctionTreeMAPSolver,
    SolverType.min_sum : JMinSumSolver,
    SolverType.sum_product : JSumProductSolver,
}

@jpype_wrapper(JFactorGraph)
class FactorGraph(Node, JWrapper[JFactorGraph]):
    def __init__(self, *, jobj:JFactorGraph|None = None):
        super().__init__(jobj=jobj or JFactorGraph())

    @property
    def is_forest(self) -> bool:
        """
        True if graph is comprised of a set of one or more disjoint trees.

        That is, given any two nodes in the graph there is at most one unique
        path between them.
        """
        return self.jgraph.isForest()

    @property
    def is_tree(self) -> bool:
        """
        True if the graph is singly connected and not disjoint.

        That is, given any two nodes in the graph there is exactly one unique path
        between them.
        """
        return self.jgraph.isTree()

    @property
    def jgraph(self) -> JFactorGraph:
        return cast(JFactorGraph, self._jobj)

    @property
    def num_iterations(self) -> int:
        return self.jgraph.getSolver().getNumIterations()

    @num_iterations.setter
    def num_iterations(self, n: int) -> None:
        self.jgraph.getSolver().setNumIterations(n)

    @property
    def solver_type(self) -> SolverType:
        solver_factory = self.jgraph.getFactorGraphFactory()
        for k,v in _solver_factory_table.items():
            if isinstance(solver_factory, v):
                return k
        raise LookupError

    @solver_type.setter
    def solver_type(self, type: SolverType) -> None:
        solver_factory_cls = _solver_factory_table[type]
        self.jgraph.setSolverFactory(solver_factory_cls())

    @property
    def structure_version(self) -> int:
        """Counter incremented whenever graph structure changes"""
        return self.jgraph.structureVersion()

    def add_factor(
        self,
        func: FactorFunction|FactorFuncSignature|FactorTable,
        *vars_and_vals: Variable|ValueType,
        energy:bool = True,
    ) -> Factor:
        if isinstance(func, FactorTable):
            jfunc: JFactorFunction = JTableFactorFunction("", func.jtable) # TODO choose name
        elif isinstance(func, FactorFunction):
            jfunc = func.jfunction
        elif isinstance(func, FactorFuncSignature):
            func_type = FFuncType.energy if energy else FFuncType.weight
            ff = FactorFunction(func, func_type=func_type)
            jfunc = ff.jfunction
        else:
            raise NotImplementedError()
        jvars_and_vals: list[JVariable|JValue] = []
        for v in vars_and_vals:
            if isinstance(v, Variable):
                jvars_and_vals.append(v.jvariable)
            else:
                jvars_and_vals.append(JValue.create(v))
        jfactor = self.jgraph.addFactor(jfunc, JObject[:]@jvars_and_vals)
        return Factor.for_jobj(jfactor)

    def add_variables(self, *variables: Variable) -> None:
        self.jgraph.addVariables(*[v.jvariable for v in variables])

    def get_node(self, id_or_name: int|str|UUID) -> Node:
        jgraph = self.jgraph
        if isinstance(id_or_name, int):
            if self.is_global_id(id_or_name):
                jnode = jgraph.getNodeByGlobalId(id_or_name)
            else:
                jnode = jgraph.getNodeByLocalId(id_or_name)
        elif isinstance(id_or_name, str):
            jnode = jgraph.getObjectByName(id_or_name)
        elif isinstance(id_or_name, UUID):
            jnode = jgraph.getObjectByUUID(JUUID.fromString(str(id_or_name)))
        else:
            raise TypeError(
                f"Expected int, str or UUID but got '{type(id_or_name)}'"
            )
        return Node.for_jobj(jnode)

    def get_factor(self, id_or_name: int|str|UUID) -> Factor:
        node = self.get_node(id_or_name)
        assert isinstance(node, Factor)
        return cast(Factor, node)

    def get_factors_list(self) -> list[Factor]:
        return list(Factor.for_jobj(jfactor) for jfactor in self.jgraph.getFactors())

    def get_variable(self, id_or_name: int|str|UUID) -> Variable:
        node = self.get_node(id_or_name)
        assert isinstance(node, Variable)
        return cast(Variable, node)

    def get_variables_list(self) -> list[Variable]:
        return list(Variable.for_jobj(jvar) for jvar in self.jgraph.getVariables())

    def remove(self, node: Factor|Variable) -> None:
        """
        Remove node from graph.

        If node is a factor, it will disconnect any sibling variables
        and leave them behind. If node is a variable, this will raise
        an exception if the factor is still connected to any factors
        (i.e., remove factors first).
        """
        self.jgraph.remove(node.jobj) # type: ignore

    def solve(self) -> None:
        self.jgraph.solve()

    def solve_more(self) -> None:
        self.jgraph.continueSolve()


