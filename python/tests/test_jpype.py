"""
Test basic Jpype interface
"""

from __future__ import annotations

import enum

import jpype

from garpy.dimple.base import ValueType
from garpy.dimple.factorgraph import FactorGraph, JFactorGraph
from garpy.dimple.factortable import FactorTable
from garpy.dimple.factor import FFuncType, FactorFunction
from garpy.dimple.variable import Domain, DiscreteDomain, Discrete

from .fixtures import * # noqa

def test_jpype() -> None:
    assert jpype.isJVMStarted()


def test_getting_started() -> None:
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

    days[Day.monday].prior = (0.7, 0.3)

    # HMM.setOption(JBPOptions.iterations, JObject(JInt(20)))
    # HMM.setSolverFactory(jdimple.solvers.minsum.MinSumSolver())
    HMM.solve()

    for day, var in days.items():
        print(f"{day: >10}:", end="")
        for w,p in zip(Weather, var.belief):
            print(f" {w.name} {p*100:.1f}%", end="") # type: ignore[attr-defined]
        print("")
