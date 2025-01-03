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
Options and interfaces specific to Gibbs solvers.
"""

from __future__ import annotations

from com.analog.lyric.dimple.solvers.gibbs import GibbsOptions as JGibbsOptions

from .base import OptionKey, SolverOptions

__all__ = [
    "GibbsOptions",
]

class GibbsOptions(SolverOptions):
    # Stick with the Java camelcase style names because option lookup
    # will use those names.

    annealingHalfLife = OptionKey.for_jobj(JGibbsOptions.annealingHalfLife)

    burnInScans = OptionKey.for_jobj(JGibbsOptions.burnInScans)

    computeRealJointBeliefMoments = OptionKey.for_jobj(
        JGibbsOptions.computeRealJointBeliefMoments
    )

    customFactors = OptionKey.for_jobj(JGibbsOptions.customFactors)

    discreteSampler = OptionKey.for_jobj(JGibbsOptions.discreteSampler)

    enableAnnealing = OptionKey.for_jobj(JGibbsOptions.enableAnnealing)

    enableAutomaticConjugateSampling = OptionKey.for_jobj(
        JGibbsOptions.enableAutomaticConjugateSampling
    )

    enableMultithreading = OptionKey.for_jobj(JGibbsOptions.enableMultithreading)

    initialTemperature = OptionKey.for_jobj(JGibbsOptions.initialTemperature)

    numRandomRestarts = OptionKey.for_jobj(JGibbsOptions.numRandomRestarts)

    numSamples = OptionKey.for_jobj(JGibbsOptions.numSamples)

    realSampler = OptionKey.for_jobj(JGibbsOptions.realSampler)

    saveAllSamples = OptionKey.for_jobj(JGibbsOptions.saveAllSamples)

    saveAllScores = OptionKey.for_jobj(JGibbsOptions.saveAllScores)

    scansPerSample = OptionKey.for_jobj(JGibbsOptions.scansPerSample)

    scheduler = OptionKey.for_jobj(JGibbsOptions.scheduler)

    scheduleValidator = OptionKey.for_jobj(JGibbsOptions.scheduleValidator)
