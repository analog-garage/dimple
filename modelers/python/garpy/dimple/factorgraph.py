from __future__ import annotations

import enum
from typing import cast

from jpype import JObject

import garpy.dimple.java # noqa

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

    def add_factor(
        self,
        func: FactorFunction|FactorFuncSignature|FactorTable,
        *vars_and_vals: Variable|ValueType,
        energy:bool = True,
    ) -> None:
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
        # TODO return Factor

    def add_variables(self, *variables: Variable) -> None:
        self.jgraph.addVariables(*[v.jvariable for v in variables])

    # TODO set_option
    # TODO set_solver

    def get_variable(self, id: int) -> Variable:
        return Variable.for_jobj(self.jgraph.getVariable(id))

    def solve(self) -> None:
        self.jgraph.solve()

    def solve_more(self) -> None:
        self.jgraph.continueSolve()


