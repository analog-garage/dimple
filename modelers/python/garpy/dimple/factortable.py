from __future__ import annotations

import math
from typing import Self, Sequence, Sized

from jpype import JDouble

import garpy.dimple.java # noqa

from com.analog.lyric.dimple.model.domains import DiscreteDomain as JDiscreteDomain
from com.analog.lyric.dimple.model.domains import DomainList as JDomainList
from com.analog.lyric.dimple.factorfunctions.core import FactorTable as JFactorTable
from com.analog.lyric.dimple.factorfunctions.core import TableFactorFunction as JTableFactorFunction
from com.analog.lyric.dimple.model.values import Value as JValue

from .base import JWrapper, jpype_wrapper
from .factor import FactorFuncSignature, FFuncArg, FFuncType
from .domain import DiscreteDomain

__all__ = [
    "FactorTable",
    "JFactorTable",
    "JTableFactorFunction",
]

class FactorTableView(JWrapper[JFactorTable]):
    """
    Base class
    """
    @property
    def is_sparse(self) -> bool:
        """
        True if factor table has a sparse representation.
        """
        return self.jobj.hasSparseRepresentation()

    @property
    def ndim(self) -> int:
        return self.jobj.getDimensions()

    @property
    def shape(self) -> tuple[int,...]:
        idx = self.jobj.getDomainIndexer()
        return tuple(idx.getDomainSize(i) for i in range(idx.size()))

    @property
    def size(self) -> int:
        return self.jobj.getDomainIndexer().getCardinality()

    @property
    def sparse_size(self) -> int:
        return self.jobj.sparseSize()

    def joint_to_sparse_index(self, i: int) -> int:
        """
        Converts a joint index to sparse index.

        If table is dense, this just returns the same index.
        """
        return self.jobj.sparseIndexFromJointIndex(i)

    def joint_index(self, *indexes: int) -> int:
        """
        Returns joint index corresponding to given per-domain indexes.
        """
        return self.jobj.getDomainIndexer().jointIndexFromIndices(*indexes)

    def unravel_joint_index(self, i: int) -> tuple[int,...]:
        """Convert joint index to tuple of domain indexes"""
        return tuple(self.jobj.getDomainIndexer().jointIndexToIndices(i))

    def unravel_sparse_index(self, i: int) -> tuple[int,...]:
        """Convert sparse index to tuple of domain indexes"""
        return tuple(self.jobj.sparseIndexToIndices(i))

    def sparse_index(self, *indexes: int) -> int:
        """
        Returns sparse index corresponding to given per-domain indexes.

        Returns a negative value if there is no corresponding entry.
        """
        return self.jobj.sparseIndexFromIndices(*indexes)

    def sparse_to_joint_index(self, i: int) -> int:
        """
        Converts a sparse index to a joint index.
        """
        return self.jobj.sparseIndexToJointIndex(i)


@jpype_wrapper(JFactorTable)
class FactorTable(FactorTableView, JWrapper[JFactorTable]):
    __slots__ = ["_energies", "_weights"]

    _energies: FactorTableEnergies|None
    _weights: FactorTableWeights|None

    #
    # Construction
    #

    def __init__(self, *, jobj: JFactorTable) -> None:
        self._energies = None
        self._weights = None
        super().__init__(jobj=jobj)

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
        jdomains = [d.jdomain for d in domains]
        indexer = JDomainList.create(list(jdomains)).asJointDomainIndexer()
        jtable = JFactorTable.create(indexer)
        cls._populate_factor_table_from_factor_function(jtable, func, func_type=func_type)
        return cls(jobj=jtable)

    @classmethod
    def create_sparse(
        cls,
        *domains: DiscreteDomain,
    ) -> Self:
        """
        Creates an empty sparse factor table over given domains.
        """
        jdomains = [d.jdomain for d in domains]
        indexer = JDomainList.create(list(jdomains)).asJointDomainIndexer()
        jtable = JFactorTable.create(indexer)
        return cls(jobj=jtable)

    def clone(self) -> Self:
        return self.__class__(jobj=self.jobj.clone())

    def set_dense_energies(
        self,
        energies: Sequence[float]
    ) -> None:
        """
        Set all the energy values in the table densely.

        Args:
            energies: energy values listed in order of
                joint index.
        """
        self.jtable.setEnergiesDense(energies)

    def set_dense_weights(
        self,
        weights: Sequence[float]
    ) -> None:
        """
        Set all the weight values in the table densely.

        Args:
            weights: weight values listed in order of
                joint index.
        """
        self.jtable.setWeightsDense(weights)

    def set_sparse_energies(
        self,
        indices: Sequence[Sequence[int]],
        energies: Sequence[float]
    ) -> None:
        """
        Sets the weights of the table sparsely

        Args:
            indices: sequence of indexes tuples
            energies: corresponding energy values
                (same length as indices)
        """
        self.jobj.setEnergiesSparse(indices, energies)

    def set_sparse_weights(
        self,
        indices: Sequence[Sequence[int]],
        weights: Sequence[float]
    ) -> None:
        """
        Sets the weights of the table sparsely

        Args:
            indices: sequence of indexes tuples
            weights: corresponding weight values
                (same length as indices)
        """
        self.jobj.setWeightsSparse(indices, weights)

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

        # TODO - add more properties and methods

    @property
    def energies(self) -> FactorTableEnergies:
        if self._energies is None:
            self._energies = FactorTableEnergies(jobj=self.jobj)
        return self._energies

    @property
    def jtable(self) -> JFactorTable:
        return self._jobj

    @property
    def weights(self) -> FactorTableWeights:
        if self._weights is None:
            self._weights = FactorTableWeights(jobj=self.jobj)
        return self._weights


class FactorTableEnergies(FactorTableView):
    def __getitem__(self, index: int|Sequence[int]) -> float:
        # TODO support slices
        if isinstance(index, Sized):
            e = self.jobj.getEnergyForIndices(*index)
        else:
            e = self.jobj.getEnergyForJointIndex(index)
        return e

    def __setitem__(self, index: int|Sequence[int], energy: float) -> None:
        if isinstance(index, Sized):
            self.jobj.setEnergyForIndices(energy, *index)
        else:
            self.jobj.setEnergyForJointIndex(energy, index)

class FactorTableWeights(FactorTableView):
    def __getitem__(self, index: int|Sequence[int]) -> float:
        # TODO support slices
        if isinstance(index, Sized):
            w = self.jobj.getWeightForIndices(*index)
        else:
            w = self.jobj.getWeightForJointIndex(index)
        return w

    def __setitem__(self, index: int|Sequence[int], weight: float) -> None:
        if isinstance(index, Sized):
            self.jobj.setWeightForIndices(weight, *index)
        else:
            self.jobj.setWeightForJointIndex(weight, index)
