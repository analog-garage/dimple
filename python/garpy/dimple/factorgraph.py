from __future__ import annotations

from typing import cast

from jpype import JObject

import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.core import FactorGraph as JFactorGraph
from com.analog.lyric.dimple.factorfunctions.core import TableFactorFunction as JTableFactorFunction
from com.analog.lyric.dimple.model.values import Value as JValue

from .base import (
    Node, ValueType, jpype_wrapper
)
from .variable import JVariable, Variable

from .factor import FactorFunction, FactorFuncSignature, FFuncType, JFactorFunction
from .factortable import FactorTable

__all__ = [
    "FactorGraph",
    "JFactorGraph",
]

@jpype_wrapper(JFactorGraph)
class FactorGraph(Node):
    def __init__(self, jgraph: JFactorGraph|None = None):
        super().__init__(jnode=(jgraph or JFactorGraph()))

    @property
    def jgraph(self) -> JFactorGraph:
        return cast(JFactorGraph, self._jnode)

    def add_factor(
        self,
        func: FactorFunction|FactorFuncSignature|FactorTable,
        *vars_and_vals: Variable|ValueType,
        energy:bool = True,
    ) -> None:
        if isinstance(func, FactorTable):
            jfunc: JFactorFunction = JTableFactorFunction("", func._jtable) # TODO choose name
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
        return Variable.for_jnode(self.jgraph.getVariable(id))

    def solve(self) -> None:
        self.jgraph.solve()

