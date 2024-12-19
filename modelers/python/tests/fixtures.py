from __future__ import annotations

import jpype
import pytest

@pytest.fixture(
    scope="session",
    autouse=True,
)
def dimple_started() -> None:
    import garpy.dimple.java
    assert jpype.isJVMStarted()
