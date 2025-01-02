"""
Test basic Jpype interface
"""

from __future__ import annotations

import enum
import math

import jpype

from garpy.dimple.base import ValueType
from garpy.dimple.factorgraph import FactorGraph, JFactorGraph
from garpy.dimple.factortable import FactorTable
from garpy.dimple.factor import FFuncType, FactorFunction
from garpy.dimple.variable import Bit, Discrete
from garpy.dimple.domain import Domain, DiscreteDomain

from .fixtures import * # noqa
# ruff: noqa: F405

def test_jpype() -> None:
    assert jpype.isJVMStarted()


def test_weather_example(graph_tester: FactorGraphTester) -> None:
    """
    Test HMM model from Getting Started section of User Guide
    """
    class Day(enum.StrEnum):
        monday = 'monday'
        tuesday = 'tuesday'
        wednesday = 'wednesday'
        thursday = 'thursday'
        friday = 'friday'
        saturday = 'saturday'
        sunday = 'sunday'

    class Weather(enum.StrEnum):
        sunny = "sunny"
        rainy = "rainy"

    class Activity(enum.StrEnum):
        walk = "walk"
        book = "book"
        cook = "cook"

    def transition_p(w1: str, w2: str) -> float:
        if w1 == Weather.sunny:
            p = 0.8 if w2 == Weather.sunny else 0.2
        else:
            p = 0.5
        return p

    def observation_p(state: str, observation: str) -> float:
        p = 0.0
        match state:
            case Weather.sunny:
                match observation:
                    case Activity.walk:
                        p = 0.7
                    case Activity.book:
                        p = 0.1
                    case Activity.cook:
                        p = 0.2
            case Weather.rainy:
                match observation:
                    case Activity.walk:
                        p = 0.2
                    case Activity.book:
                        p = 0.4
                    case Activity.cook:
                        p = 0.4
        return p

    weather = DiscreteDomain.create(*Weather)

    assert Domain.for_jobj(weather.jdomain) == weather

    days: dict[Day,Discrete] = { day : Discrete(weather) for day in Day }

    HMM = FactorGraph()
    assert FactorGraph.wrapper_type_for(JFactorGraph) == FactorGraph

    HMM.add_variables(*days.values())

    # add transition factors
    trans_func = FactorFunction(transition_p, func_type=FFuncType.weight)  # type: ignore
    prev_var = None
    for day,var in days.items():
        if prev_var:
            HMM.add_factor(transition_p, prev_var,var, energy=False)
        prev_var = var

    # add observation factors
    observations = {
        Day.monday : Activity.walk,
        Day.tuesday : Activity.book,
        Day.wednesday : Activity.cook,
        Day.thursday : Activity.walk,
        Day.friday: Activity.cook,
        Day.saturday: Activity.book,
        Day.sunday: Activity.book,
    }
    for day, var in days.items():
        HMM.add_factor(observation_p, var, observations[day]) # type: ignore

    days[Day.monday].prior_weights = (0.7, 0.3)

    graph_tester.check_graph(HMM)

    # HMM.setOption(JBPOptions.iterations, JObject(JInt(20)))
    # HMM.setSolverFactory(jdimple.solvers.minsum.MinSumSolver())
    HMM.num_iterations = 20
    HMM.solve()

    for day, var in days.items():
        print(f"{day: >10}:", end="")
        for w,p in zip(Weather, var.belief):
            print(f" {w.name} {p*100:.1f}%", end="") # type: ignore[attr-defined]
        print("")

def test_4_bit_xor_example() -> None:
    def xor_energy(*args: int) -> float:
        return math.inf if (sum(args) & 1) else 0.0

    fg = FactorGraph()
    bit_vars: list[Bit] = [Bit() for _ in range(4)]
    fg.add_factor(xor_energy, *bit_vars)  # type: ignore
    for i in range(3):
        bit_vars[i].prior_weights = (0.2,0.8)
    bit_vars[3].prior_weights = (0.5,0.5)
    fg.solve()
    print("4-bit solution:")
    for var in bit_vars:
        print(f"belief={var.belief}, value={var.value}")

    def three_bit_xor_energy(b1: int, b2: int, b3: int) -> float:
        return math.inf if (b1^b2^b3) else 0.0

    three_bit_func = FactorFunction(three_bit_xor_energy)  # type: ignore

    fg2 = FactorGraph()
    b = [Bit() for _ in range(4)]
    c = Bit()
    fg2.add_factor(three_bit_func, *b[:2], c)
    fg2.add_factor(three_bit_func, *b[2:], c)
    for var,prior in zip(b, [0.8,0.8,0.8,0.5]):
        var.p1_prior = prior
    fg2.solve()
    print("3-bit solution:")
    for var in b:
        print(f"belief={var.belief}, value={var.value}")



