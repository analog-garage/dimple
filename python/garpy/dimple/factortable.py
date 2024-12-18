from __future__ import annotations

import math
from typing import Any, Sequence, TypeVar

import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.domains import DiscreteDomain as JDiscreteDomain
from com.analog.lyric.dimple.model.domains import DomainList as JDomainList
from com.analog.lyric.dimple.factorfunctions.core import FactorTable as JFactorTable
from com.analog.lyric.dimple.factorfunctions.core import TableFactorFunction as JTableFactorFunction
from com.analog.lyric.dimple.model.values import Value as JValue

from .base import jpype_wrapper, _register_wrapper
from .factor import FactorFuncSignature, FFuncArg, FFuncType
from .variable import DiscreteDomain

__all__ = [
    "FactorTable",
    "JFactorTable",
    "JTableFactorFunction",
]


@jpype_wrapper(JFactorTable)
class FactorTable:
    __slots__ = ["_jtable", "__weakref__"]

    _jtable: JFactorTable

    #
    # Construction
    #

    def __init__(self, *, jtable: JFactorTable) -> None:
        self._jtable = jtable
        _register_wrapper(self, jtable)

    @classmethod
    def create(
        cls,
        func: FactorFuncSignature,
        *domains: DiscreteDomain,
        func_type: FFuncArg = FFuncType.energy,
    ) -> FactorTable:
        """
        Create a factor table by evaluating function with all possible combinations
        of values from given discrete domains.

        Arguments:
            func: factor function to evaluate
            *domains: one or more domains
        """
        func_type = FFuncType(func_type)  # convert from string
        jdomains = [d._jdomain for d in domains]
        indexer = JDomainList.create(list(jdomains)).asJointDomainIndexer()
        jtable = JFactorTable.create(indexer)
        cls._populate_factor_table_from_factor_function(jtable, func, func_type=func_type)
        return cls(jtable=jtable)

    @classmethod
    def _populate_factor_table_from_factor_function(
        cls,
        table: JFactorTable,
        func: FactorFuncSignature,
        func_type: FFuncType = FFuncType.energy,
        *,
        dense: bool = False,
    ) -> None:
        # Translated from FactorTableBase.populateFromFunction()
        domains = table.getDomainIndexer()
        values: Sequence[JValue] = JValue.createFromDomains(domains)
        indexes: list[int] = []
        results: list[float] = []

        max_joint = domains.getCardinality()
        # TODO maybe have parameter to decided when to use dense/sparse rep?

        if func_type is FFuncType.energy:
            for ji in range(max_joint):
                domains.jointIndexToValues(ji, values)
                result = func(*[v.getObject() for v in values])
                if dense or math.isfinite(result):
                    indexes.append(ji)
                    results.append(result)

            if (len(indexes) == max_joint):
                table.setEnergiesDense(results)
            else:
                table.setEnergiesSparse(indexes, results)
        else:
            for ji in range(max_joint):
                domains.jointIndexToValues(ji, values)
                result = func(*[v.getObject() for v in values])
                if dense or result > 0.0:
                    indexes.append(ji)
                    results.append(result)

            if (len(indexes) == max_joint):
                table.setWeightsDense(results)
            else:
                table.setWeightsSparse(indexes, results)

        # TODO - add properties and methods
