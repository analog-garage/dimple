#  Copyright 2024 Analog Devices Inc.
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
Options and interfaces specific to belief-propagation (BP) based solvers.
"""

from __future__ import annotations

from com.analog.lyric.dimple.options import BPOptions as JBOptions

from .base import OptionKey, SolverOptions

__all__ = [
    "BPOptions",
]

class BPOptions(SolverOptions):
    # Stick with the Java camelcase style names because option lookup
    # will use those names.

    automaticExecutionTimeScalingFactor = OptionKey.for_jobj(
        JBOptions.automaticExecutionTimeScalingFactor
    )

    automaticMemoryAllocationScalingFactor = OptionKey.for_jobj(
        JBOptions.automaticMemoryAllocationScalingFactor
    )

    damping = OptionKey.for_jobj(JBOptions.damping)

    iterations = OptionKey.for_jobj(JBOptions.iterations)

    maxMessageSize = OptionKey.for_jobj(JBOptions.maxMessageSize)

    nodeSpecificDamping = OptionKey.for_jobj(JBOptions.nodeSpecificDamping)

    optimizedUpdateSparseThreshold = OptionKey.for_jobj(
        JBOptions.optimizedUpdateSparseThreshold
    )

    updateApproach = OptionKey.for_jobj(JBOptions.updateApproach)

    scheduler = OptionKey.for_jobj(JBOptions.scheduler)

    scheduleValidator = OptionKey.for_jobj(JBOptions.scheduleValidator)

