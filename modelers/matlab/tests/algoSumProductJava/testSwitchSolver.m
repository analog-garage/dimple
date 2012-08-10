function testSwitchSolver( )

    setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
    fg = FactorGraph();
    b = Bit(3,1);
    fg.addFactor(@xorDelta,b);
    b.Input = [.8 .8 .6];
    fg.solve();
    expected = [0.7586; 0.7586; 0.4138];
    assertTrue(all((b.Belief-expected)<.0001));
    
    fg.Solver = com.analog.lyric.dimple.solvers.minsum.Solver();
    fg.solve();
    
    expected = [0.7273; 0.7273; 0.2727];
    assertTrue(all((b.Belief-expected)<.0001));
    
    setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
    
    % float_to_fixed() from SolverSwitchTest.java
    
    %b2 = Bit(3,1);
    %setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
    %fg2 = FactorGraph();
    %fg.addFactor(@xorDelta,b);
    %setSolver(com.analog.lyric.dimple.solvers.sumproductfixedpoint.Solver());

    % fixed_to_float() from SolverSwitchTest.java
    
    %b2 = Bit(3,1);
    %setSolver(com.analog.lyric.dimple.solvers.sumproductfixedpoint.Solver());
    %fg2 = FactorGraph();
    %fg.addFactor(@xorDelta,b);
    %setSolver(com.analog.lyric.dimple.solvers.sumproduct.Solver());
end

