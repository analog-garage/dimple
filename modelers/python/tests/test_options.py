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
Test option classes
"""

from __future__ import annotations

from garpy.dimple import (
    BPOptions,
    DimpleEnvironment,
    DimpleOptions,
    FactorGraph,
    OptionKey,
    SolverOptions
)

from com.analog.lyric.options import OptionKey as JOptionKey
from com.analog.lyric.dimple.environment import DimpleEnvironment as JDimpleEnvironment

def check_option_key(key: OptionKey) -> None:
    """Test invariants on OptionKey"""
    assert isinstance(key.jkey, JOptionKey)
    assert hash(key) == key.jkey.hashCode()
    assert key == key # noqa: PLR0124
    assert repr(key) == f"<OptionKey {key.name}>"
    assert str(key) == key.name

    fg = FactorGraph()
    assert fg.get_option(key, default=False) is None
    assert key.get(fg, default=False) is None
    val = key.get(fg)
    if val is not None:
        key.set(fg, val)
        assert fg.get_option(key, default=False) == val
        key.unset(fg)
        assert fg.get_option(key, default=False) is None

def test_option_keys() -> None:
    for cls in [DimpleOptions, SolverOptions]:
        for name, val in vars(cls).items():
            if not name.startswith("_"):
                assert isinstance(val, OptionKey)
                check_option_key(val)

def test_environment() -> None:
    """
    Test DimpleEnvironment
    """
    assert isinstance(DimpleEnvironment.active(), DimpleEnvironment)
    assert isinstance(DimpleEnvironment.active().jenvironment, JDimpleEnvironment)
    env = DimpleEnvironment.active()
    assert env.environment == env
    assert env.option_parent is None
    assert env.supports_local_options
    assert len(env.option_keys) > 0

    for i, key in env.option_keys.items():
        assert key == env.option_keys[i]
        assert key == env.option_keys.get(key.name)
        assert env.option_key(key.name) == key
        assert env.option_key(key) is key

    env2 = DimpleEnvironment()
    assert env2 != env

def test_option_holder() -> None:
    fg = FactorGraph()
    assert fg.environment == DimpleEnvironment.active()
    assert fg.option_parent == DimpleEnvironment.active()

    env = DimpleEnvironment()
    prev = DimpleEnvironment.set_active(env)
    try:
        fg2 = FactorGraph()
        assert fg2.environment == env
        assert fg2.option_parent == env
        assert fg2.supports_local_options
        assert fg2.get_local_options() == {}

        assert fg2.get_option_and_source(BPOptions.damping) is None

        env.set_option(BPOptions.damping, 0.75)
        assert prev.get_option(BPOptions.damping, default=False) is None
        assert fg2.get_option(BPOptions.damping) == 0.75
        assert fg2.get_option_and_source("BPOptions.damping") == (0.75, env)

        fg2.set_option(BPOptions.damping, 0.5)
        assert fg2.get_option(BPOptions.damping) == 0.5
        assert fg2.get_option_and_source("BPOptions.damping") == (0.5, fg2)

        assert fg2.get_local_option(BPOptions.damping) == 0.5
        assert fg2.get_local_options() == {BPOptions.damping: 0.5}

        fg2.unset_option(BPOptions.damping)
        assert fg2.get_local_options() == {}

        env.clear_local_options()
        assert env.get_local_options() == {}

    finally:
        DimpleEnvironment.set_active(prev)


