function testTurnOnOff()
    fg = FactorGraph();
    fg.Solver.useMultithreading(true);
    assertTrue(fg.Solver.useMultithreading());
    fg.Solver.useMultithreading(false);
    assertFalse(fg.Solver.useMultithreading());
end